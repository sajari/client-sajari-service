package com.sajari.client.config;

import com.sajari.client.ApiClient;
import com.sajari.client.datafetcher.GoogleFeedDataFetcher;
import com.sajari.client.publisher.SajariClientPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfiguration {

    @Value("${sajari.key-id}")
    private String sajariUser;

    @Value("${sajari.key-secret}")
    private String sajariPassword;

    @Value("${sajari.api-url}")
    private String sajariUrl;

    @Value("${sajari.collection-id}")
    private String sajariCollectionId;

    @Value("${customer.google-product-feed-url}")
    private String customerUrl;

    @Bean
    public GoogleFeedDataFetcher googleFeedDataFetcher() {
        return new GoogleFeedDataFetcher();
    }

    @Bean
    public SajariClientPublisher sajariClientPublisher() {
        return new SajariClientPublisher(apiClient(), this, googleFeedDataFetcher());
    }

    @Bean
    public ApiClient apiClient() {
        return new ApiClient();
    }

    public String getSajariUser() {
        return sajariUser;
    }

    public String getSajariPassword() {
        return sajariPassword;
    }

    public String getSajariUrl() {
        return sajariUrl;
    }

    public String getSajariCollectionId() {
        return sajariCollectionId;
    }

    public String getCustomerUrl() {
        return customerUrl;
    }
}
