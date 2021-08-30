package com.sajari.client.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sajari.client.model.Record;
import org.jeasy.batch.core.mapper.RecordMapper;
import org.jeasy.batch.core.record.GenericRecord;

public class JacksonRecordMapper<T> implements RecordMapper<Record, T> {

    private final ObjectMapper mapper;
    private final Class<T> type;

    /**
     * Create a new {@link JacksonRecordMapper} with a default {@link ObjectMapper} instance.
     *
     * @param type of the target object
     */
    public JacksonRecordMapper(Class<T> type) {
        this.type = type;
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
    }

    @Override
    public org.jeasy.batch.core.record.Record<T> processRecord(final org.jeasy.batch.core.record.Record<Record> record) {
        return new GenericRecord<>(record.getHeader(), mapper.convertValue(record.getPayload(), type));
    }
}
