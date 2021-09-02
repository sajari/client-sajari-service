package com.sajari.client.publisher;

import com.google.common.collect.Sets;
import com.rometools.rome.io.FeedException;
import com.sajari.client.ApiClient;
import com.sajari.client.ApiException;
import com.sajari.client.api.CollectionsApi;
import com.sajari.client.api.RecordsApi;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.datafetcher.DataFetcher;
import com.sajari.client.model.DeleteRecordRequest;
import com.sajari.client.model.QueryCollectionRequest;
import com.sajari.client.model.QueryCollectionRequestPipeline;
import com.sajari.client.model.QueryCollectionResponse;
import com.sajari.client.model.QueryResult;
import com.sajari.client.model.Record;
import com.sajari.client.model.RecordKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.jeasy.batch.core.job.Job;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.reader.StreamRecordReader;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Sets.newHashSet;

@Slf4j
public class SajariClientPublisher {

    private static final String PIPELINE_NAME = "app";
    private static final String PIPELINE_VERSION = "1";
    private static final int RESULTS_PER_PAGE_SIZE = 100;
    private final ApiClient apiClient;
    private final AppConfiguration appConfiguration;
    private final DataFetcher dataFetcher;

    public SajariClientPublisher(ApiClient apiClient, AppConfiguration appConfiguration, DataFetcher dataFetcher) {
        this.apiClient = apiClient;
        this.appConfiguration = appConfiguration;
        this.dataFetcher = dataFetcher;
    }

    @Scheduled(cron = "@hourly")
    public void runSajariSync() throws FeedException, IOException {

        // Sweep stale data
        Set<String> oldRecordIds = Sets.newHashSet();
        if (appConfiguration.isSweepStaleData()) {
            // Determine current state
            oldRecordIds.addAll(determineCurrentRecords());
        }

        // Update records
        Iterable<Record> records = dataFetcher.fetch(appConfiguration.getGoogleProductFeedUrl());
//        Iterable<Record> records = dataFetcher.fetch(getURL("classpath:cue-small-smaller.xml"));

        Set<String> sentRecordIds = sendToSajari(records);

        // Sweep stale data
        if (appConfiguration.isSweepStaleData()) {

            oldRecordIds.removeAll(sentRecordIds);
            deleteOldRecords(oldRecordIds);
        }
    }

    private Set<String> determineCurrentRecords() {
        Set<String> recordIndexes = newHashSet();

        try {
            QueryCollectionResponse queryCollectionResponse = new CollectionsApi(apiClient).queryCollection(appConfiguration.getSajariCollectionId(), getQueryCollectionRequest());

            double totalSize = NumberUtils.toDouble(queryCollectionResponse.getTotalSize());
            int numOfPages = (int) Math.ceil(totalSize / RESULTS_PER_PAGE_SIZE);

            List<QueryResult> queryResults = queryCollectionResponse.getResults();

            int currentPage = 1;
            while (currentPage <= numOfPages) {

                for (QueryResult queryResult : queryResults) {
                    Map<String, String> queryResultRecord = (Map<String, String>) queryResult.getRecord();
                    recordIndexes.add(queryResultRecord.get("id"));
                }

                // Increment the page to the next page of results
                currentPage++;

                queryResults = new CollectionsApi(apiClient).queryCollection(appConfiguration.getSajariCollectionId(), getQueryCollectionRequest(Map.of("page", Integer.toString(currentPage)))).getResults();
            }

        } catch (ApiException e) {
            log.error("Failed to query results: " + e.getMessage(), e);
        }
        return recordIndexes;
    }

    public Set<String> sendToSajari(Iterable<Record> records) {

        SajariClientRecordWriter recordWriter = new SajariClientRecordWriter(apiClient, appConfiguration);
        Job job = new JobBuilder<Record, Map<String, String>>()
                .reader(new StreamRecordReader<>(StreamSupport.stream(records.spliterator(), false)))
                .mapper(new JacksonRecordMapper<>(Map.class))
                .writer(recordWriter)
                .batchSize(50)
                .build();

        JobExecutor jobExecutor = new JobExecutor();
        jobExecutor.execute(job);
        jobExecutor.shutdown();

        return recordWriter.getSentRecordIds();
    }

    private void deleteOldRecords(Set<String> recordIndexes) {

        for (String recordIndex : recordIndexes) {
            DeleteRecordRequest deleteRecordRequest = new DeleteRecordRequest();
            RecordKey recordKey = new RecordKey();
            recordKey.setField("id");
            recordKey.setValue(recordIndex);
            deleteRecordRequest.setKey(recordKey);

            try {
                new RecordsApi(apiClient).deleteRecord(appConfiguration.getSajariCollectionId(), deleteRecordRequest);
            } catch (ApiException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @NotNull
    private QueryCollectionRequest getQueryCollectionRequest(Map<String, String> additionalSearchVariables) {

        QueryCollectionRequest queryCollectionRequest = getQueryCollectionRequest();
        queryCollectionRequest.setVariables(buildVariablesMap(additionalSearchVariables));
        return queryCollectionRequest;
    }

    @NotNull
    private QueryCollectionRequest getQueryCollectionRequest() {

        QueryCollectionRequestPipeline queryCollectionRequestPipeline = new QueryCollectionRequestPipeline();
        queryCollectionRequestPipeline.setName(PIPELINE_NAME);
        queryCollectionRequestPipeline.setVersion(PIPELINE_VERSION);

        QueryCollectionRequest queryCollectionRequest = new QueryCollectionRequest();
        queryCollectionRequest.setPipeline(queryCollectionRequestPipeline);
        queryCollectionRequest.setVariables(buildBaseVariablesMap());
        return queryCollectionRequest;
    }

    @NotNull
    private Map<String, String> buildBaseVariablesMap() {

        Map<String, String> elements = new HashMap<>();
        elements.put("q", "");
//        elements.put("filter", "SINCE_NOW(record_creation_date, '" + appConfiguration.getSweepInterval() + "')");
        elements.put("fields", "_id,record_creation_date,title");
        elements.put("page", "1");
        elements.put("resultsPerPage", Integer.toString(RESULTS_PER_PAGE_SIZE));
        return elements;
    }

    @NotNull
    private Map<String, String> buildVariablesMap(Map<String, String> additionalVariables) {

        Map<String, String> elements = buildBaseVariablesMap();
        elements.putAll(additionalVariables);
        return elements;
    }
}