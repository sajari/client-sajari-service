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
import com.sajari.client.setup.CreateSchema;
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

    private static final int RESULTS_PER_PAGE_SIZE = 100;
    private static final String UNIQUE_RECORD_ID = "_id";
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

        Set<String> sentRecordIds = sendToSajari(records);

        // Sweep stale data
        if (appConfiguration.isSweepStaleData()) {

            oldRecordIds.removeAll(sentRecordIds);
            deleteOldRecords(oldRecordIds);
        }
    }

    protected Set<String> determineCurrentRecords() {
        Set<String> recordIndexes = newHashSet();

        try {
            QueryCollectionRequest queryCollectionRequest = getQueryCollectionRequest();
            QueryCollectionResponse queryCollectionResponse = new CollectionsApi(apiClient).queryCollection(appConfiguration.getSajariCollectionId(), queryCollectionRequest);

            double totalSize = NumberUtils.toDouble(queryCollectionResponse.getTotalSize());
            int numOfPages = (int) Math.ceil(totalSize / RESULTS_PER_PAGE_SIZE);

            log.info("Current record count {} across {} number of pages", totalSize, numOfPages);

            List<QueryResult> queryResults = queryCollectionResponse.getResults();

            int currentPage = 1;
            while (currentPage <= numOfPages) {

                for (QueryResult queryResult : queryResults) {
                    Map<String, String> queryResultRecord = (Map<String, String>) queryResult.getRecord();
                    recordIndexes.add(queryResultRecord.get(UNIQUE_RECORD_ID));
                }

                // Increment the page to the next page of results
                currentPage++;

                queryResults = new CollectionsApi(apiClient).queryCollection(appConfiguration.getSajariCollectionId(), getQueryCollectionRequest(Map.of("page", Integer.toString(currentPage)))).getResults();
            }

        } catch (ApiException e) {
            log.error("Failed to query results wth query {} due to response code {}, response body {}", getQueryCollectionRequest(), e.getCode(), e.getResponseBody());
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

            RecordKey recordKey = new RecordKey().field(UNIQUE_RECORD_ID).value(recordIndex);
            DeleteRecordRequest deleteRecordRequest = new DeleteRecordRequest().key(recordKey);

            try {
                new RecordsApi(apiClient).deleteRecord(appConfiguration.getSajariCollectionId(), deleteRecordRequest);
                log.info("Deleting record: {},{}", recordKey.getField(), recordKey.getValue());
            } catch (ApiException e) {
                log.error("Failed to delete record {} due to response code {}, response body {}", recordKey, e.getCode(), e.getResponseBody());
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

        return new QueryCollectionRequest().pipeline(new QueryCollectionRequestPipeline().name(CreateSchema.APP_QUERY_PIPELINE_NAME)).variables(buildBaseVariablesMap());
    }

    @NotNull
    private Map<String, String> buildBaseVariablesMap() {

        return Map.of(
                "q", "",
                "filter", "_id != ''",
                "fields", UNIQUE_RECORD_ID + ",id,record_creation_date,title",
                "page", "1",
                "resultsPerPage", Integer.toString(RESULTS_PER_PAGE_SIZE)
        );
    }

    @NotNull
    private Map<String, String> buildVariablesMap(Map<String, String> additionalVariables) {

        HashMap<String, String> variablesMap = new HashMap<>(additionalVariables);
        variablesMap.putAll(buildBaseVariablesMap());

        return variablesMap;
    }
}