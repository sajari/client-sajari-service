package com.sajari.client.publisher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.io.FeedException;
import com.sajari.client.ApiClient;
import com.sajari.client.ApiException;
import com.sajari.client.api.RecordsApi;
import com.sajari.client.auth.HttpBasicAuth;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.datafetcher.DataFetcher;
import com.sajari.client.model.BatchUpsertRecordsRequest;
import com.sajari.client.model.BatchUpsertRecordsResponse;
import com.sajari.client.model.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class SajariClientPublisher {

    private final ApiClient apiClient;
    private final AppConfiguration appConfiguration;
    private final DataFetcher dataFetcher;

    public SajariClientPublisher(ApiClient apiClient, AppConfiguration appConfiguration, DataFetcher dataFetcher) {
        this.apiClient = apiClient;
        this.appConfiguration = appConfiguration;
        this.dataFetcher = dataFetcher;
    }

    public void sendToSajari(Iterable<Record> records) {

        apiClient.setBasePath(appConfiguration.getSajariApiUrl());

        // Configure HTTP basic authorization: BasicAuth
        HttpBasicAuth BasicAuth = (HttpBasicAuth) apiClient.getAuthentication("BasicAuth");
        BasicAuth.setUsername(appConfiguration.getSajariKeyId());
        BasicAuth.setPassword(appConfiguration.getSajariKeySecret());

        RecordsApi apiInstance = new RecordsApi(apiClient);

        BatchUpsertRecordsRequest upsertRecordRequest = new BatchUpsertRecordsRequest();
        final ObjectMapper mapper = new ObjectMapper();
        for (Record record : records) {
            Map<String, String> recordAsMap = mapper.convertValue(record, new TypeReference<>() {
            });
            upsertRecordRequest.addRecordsItem(recordAsMap);
        }

        try {
            BatchUpsertRecordsResponse result = apiInstance.batchUpsertRecords(appConfiguration.getSajariCollectionId(), upsertRecordRequest);
            log.info(result.toString());
        } catch (ApiException e) {
            log.error("Failed to upsert record", e);
        }
    }

    @Scheduled(cron = "@hourly")
    public void updateRecords() throws IOException, FeedException {

        // fetch data
        Iterable<Record> fetch = dataFetcher.fetch(appConfiguration.getGoogleProductFeedUrl());

        // update sajari
        sendToSajari(fetch);
    }
}
