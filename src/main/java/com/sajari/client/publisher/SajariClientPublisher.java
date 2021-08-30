package com.sajari.client.publisher;

import com.rometools.rome.io.FeedException;
import com.sajari.client.ApiClient;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.datafetcher.DataFetcher;
import com.sajari.client.model.Record;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.batch.core.job.Job;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.reader.StreamRecordReader;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.Map;
import java.util.stream.StreamSupport;

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

        Job job = new JobBuilder<Record, Map<String, String>>()
                .reader(new StreamRecordReader<>(StreamSupport.stream(records.spliterator(), false)))
                .mapper(new JacksonRecordMapper<>(Map.class))
                .writer(new SajariClientRecordWriter(apiClient, appConfiguration))
                .batchSize(5)
                .build();

        JobExecutor jobExecutor = new JobExecutor();
        jobExecutor.execute(job);
        jobExecutor.shutdown();
    }

    @Scheduled(cron = "@hourly")
    public void sendToSajari() throws FeedException, IOException {
        sendToSajari(dataFetcher.fetch(appConfiguration.getGoogleProductFeedUrl()));
    }
}