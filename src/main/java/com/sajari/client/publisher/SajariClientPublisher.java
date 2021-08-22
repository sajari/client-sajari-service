package com.sajari.client.publisher;

import com.sajari.client.ApiClient;
import com.sajari.client.ApiException;
import com.sajari.client.api.RecordsApi;
import com.sajari.client.auth.HttpBasicAuth;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.model.UpsertRecordRequest;
import com.sajari.client.model.UpsertRecordResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SajariClientPublisher {

    private final ApiClient apiClient;
    private final AppConfiguration appConfiguration;

    public SajariClientPublisher(ApiClient apiClient, AppConfiguration appConfiguration) {
        this.apiClient = apiClient;
        this.appConfiguration = appConfiguration;
    }

    public void sendToSajari() {

        apiClient.setBasePath(appConfiguration.getSajariUrl());

        // Configure HTTP basic authorization: BasicAuth
        HttpBasicAuth BasicAuth = (HttpBasicAuth) apiClient.getAuthentication("BasicAuth");
        BasicAuth.setUsername(appConfiguration.getSajariUser());
        BasicAuth.setPassword(appConfiguration.getSajariPassword());

        RecordsApi apiInstance = new RecordsApi(apiClient);
        UpsertRecordRequest upsertRecordRequest = new UpsertRecordRequest();
        try {
            UpsertRecordResponse result = apiInstance.upsertRecord(appConfiguration.getSajariCollectionId(), upsertRecordRequest);
            log.info(result.toString());
        } catch (ApiException e) {
            log.error("Failed to upsert record", e);
        }
    }


}
