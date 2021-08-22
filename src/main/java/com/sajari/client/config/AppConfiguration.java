package com.sajari.client.config;

import com.sajari.client.ApiClient;
import com.sajari.client.datafetcher.GoogleFeedDataFetcher;
import com.sajari.client.publisher.SajariClientPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Value("${sajari.user}")
    private String sajariUser;

    @Value("${sajari.password}")
    private String sajariPassword;

    @Value("${sajari.url}")
    private String sajariUrl;

    @Value("${sajari.collection_id}")
    private String sajariCollectionId;

    @Bean
    public GoogleFeedDataFetcher googleFeedDataFetcher() {
        return new GoogleFeedDataFetcher();
    }

    @Bean
    public SajariClientPublisher sajariClientPublisher() {
        return new SajariClientPublisher(apiClient(), this);
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
}
