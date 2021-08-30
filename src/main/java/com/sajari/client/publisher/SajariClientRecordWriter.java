package com.sajari.client.publisher;

import com.sajari.client.ApiClient;
import com.sajari.client.ApiException;
import com.sajari.client.api.RecordsApi;
import com.sajari.client.auth.HttpBasicAuth;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.model.BatchUpsertRecordsRequest;
import com.sajari.client.model.BatchUpsertRecordsResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.batch.core.record.Batch;
import org.jeasy.batch.core.writer.RecordWriter;

import java.util.Map;

@Slf4j
public final class SajariClientRecordWriter implements RecordWriter<Map<String, String>> {

    private final RecordsApi apiInstance;
    private final AppConfiguration appConfiguration;

    public SajariClientRecordWriter(ApiClient apiClient, AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
        this.apiInstance = new RecordsApi(apiClient);
    }

    @Override
    public void open() {
        // no-op
    }

    @Override
    public void writeRecords(Batch<Map<String, String>> batch) {

        try {

            BatchUpsertRecordsRequest upsertRecordRequest = new BatchUpsertRecordsRequest();

            for (org.jeasy.batch.core.record.Record<Map<String, String>> pRecord : batch) {
                upsertRecordRequest.addRecordsItem(pRecord.getPayload());
            }

            BatchUpsertRecordsResponse result = apiInstance.batchUpsertRecords(appConfiguration.getSajariCollectionId(), upsertRecordRequest);

            log.info(result.toString());
        } catch (ApiException e) {
            log.error("Failed to upsert record", e);
        }
    }

    @Override
    public void close() throws Exception {
        // no-op
    }
}
