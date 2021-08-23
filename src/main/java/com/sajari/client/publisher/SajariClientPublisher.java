package com.sajari.client.publisher;

import com.google.common.collect.Lists;
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
import com.sajari.client.model.UpsertRecordRequest;
import com.sajari.client.model.UpsertRecordResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

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

    public void sendToSajari(Iterable<Map<String, String>> records) {

        apiClient.setBasePath(appConfiguration.getSajariUrl());

        // Configure HTTP basic authorization: BasicAuth
        HttpBasicAuth BasicAuth = (HttpBasicAuth) apiClient.getAuthentication("BasicAuth");
        BasicAuth.setUsername(appConfiguration.getSajariUser());
        BasicAuth.setPassword(appConfiguration.getSajariPassword());

        RecordsApi apiInstance = new RecordsApi(apiClient);

        BatchUpsertRecordsRequest upsertRecordRequest = new BatchUpsertRecordsRequest();
        upsertRecordRequest.records(newArrayList(records));

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
        Iterable<Map<String, String>> fetch = dataFetcher.fetch(appConfiguration.getCustomerUrl());

        // update sajari
        sendToSajari(fetch);
    }
}
