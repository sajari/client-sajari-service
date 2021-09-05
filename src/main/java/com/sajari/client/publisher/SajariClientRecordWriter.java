package com.sajari.client.publisher;

import com.google.common.collect.Sets;
import com.sajari.client.ApiClient;
import com.sajari.client.ApiException;
import com.sajari.client.api.RecordsApi;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.model.BatchUpsertRecordsRequest;
import com.sajari.client.model.BatchUpsertRecordsRequestPipeline;
import com.sajari.client.model.BatchUpsertRecordsResponse;
import com.sajari.client.model.BatchUpsertRecordsResponseKey;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.batch.core.record.Batch;
import org.jeasy.batch.core.writer.RecordWriter;

import java.util.Map;
import java.util.Set;

import static com.sajari.client.setup.CreateSchema.APP_RECORD_PIPELINE_NAME;

@Slf4j
public final class SajariClientRecordWriter implements RecordWriter<Map<String, String>> {

    private final RecordsApi apiInstance;
    private final AppConfiguration appConfiguration;
    private final Set<String> writtenRecordIds;

    public SajariClientRecordWriter(ApiClient apiClient, AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
        this.apiInstance = new RecordsApi(apiClient);
        writtenRecordIds = Sets.newHashSet();
    }

    @Override
    public void open() {
        // no-op
    }

    @Override
    public void writeRecords(Batch<Map<String, String>> batch) {

        try {

            BatchUpsertRecordsRequest upsertRecordRequest = new BatchUpsertRecordsRequest().pipeline(new BatchUpsertRecordsRequestPipeline().name(APP_RECORD_PIPELINE_NAME));

            for (org.jeasy.batch.core.record.Record<Map<String, String>> pRecord : batch) {
                upsertRecordRequest.addRecordsItem(pRecord.getPayload());
            }

            BatchUpsertRecordsResponse result = apiInstance.batchUpsertRecords(appConfiguration.getSajariCollectionId(), upsertRecordRequest);

            for (BatchUpsertRecordsResponseKey responseKey : result.getKeys()) {
                writtenRecordIds.add(responseKey.getKey().getValue());
            }
            log.info("Stored {} records", writtenRecordIds.size());

        } catch (ApiException e) {
            log.error("Failed to upsert record. Response code: {}, Response body: {}", e.getCode(), e.getResponseBody());
        }
    }

    @Override
    public void close() throws Exception {
        // no-op
    }

    public Set<String> getSentRecordIds() {
        return writtenRecordIds;
    }
}
