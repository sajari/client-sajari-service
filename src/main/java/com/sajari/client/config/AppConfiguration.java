package com.sajari.client.config;

import com.sajari.client.ApiClient;
import com.sajari.client.datafetcher.GoogleFeedDataFetcher;
import com.sajari.client.publisher.SajariClientPublisher;
import com.sajari.client.setup.CreateSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfiguration {

    @Value("${sajari.key-id}")
    private String sajariKeyId;

    @Value("${sajari.key-secret}")
    private String sajariKeySecret;

    @Value("${sajari.api-url}")
    private String sajariApiUrl;

    @Value("${sajari.collection-id}")
    private String sajariCollectionId;

    @Value("${customer.google-product-feed-url}")
    private String googleProductFeedUrl;

    @Value("${customer.sweep-stale-data}")
    private boolean sweepStaleData = true;

    @Bean
    public GoogleFeedDataFetcher googleFeedDataFetcher() {
        return new GoogleFeedDataFetcher();
    }

    @Bean
    public SajariClientPublisher sajariClientPublisher() {
        return new SajariClientPublisher(apiClient(), this, googleFeedDataFetcher());
    }

    @Bean
    public CreateSchema createSchema() {
        return new CreateSchema(apiClient(), this);
    }

    @Bean
    public ApiClient apiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setUsername(getSajariKeyId());
        apiClient.setPassword(getSajariKeySecret());
        apiClient.setBasePath(getSajariApiUrl());
        return apiClient;
    }

    public String getSajariKeyId() {
        return sajariKeyId;
    }

    public String getSajariKeySecret() {
        return sajariKeySecret;
    }

    public String getSajariApiUrl() {
        return sajariApiUrl;
    }

    public String getSajariCollectionId() {
        return sajariCollectionId;
    }

    public String getGoogleProductFeedUrl() {
        return googleProductFeedUrl;
    }

    public boolean isSweepStaleData() {
        return sweepStaleData;
    }
}
