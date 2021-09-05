package com.sajari.client.config;

import com.rometools.rome.io.FeedException;
import com.sajari.client.ApiException;
import com.sajari.client.publisher.SajariClientPublisher;
import com.sajari.client.setup.CreateSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class AppInitialisation {

    private final CreateSchema createSchema;
    private final SajariClientPublisher clientPublisher;

    public AppInitialisation(CreateSchema createSchema, SajariClientPublisher clientPublisher) {
        this.createSchema = createSchema;
        this.clientPublisher = clientPublisher;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {

        log.info("Initialising schema");
        try {
            createSchema.create();
        } catch (ApiException e) {
            log.error("Failed to initialise schema", e);
        }
        log.info("Initialising schema complete");

        log.info("Perform initial data-sync");
        try {
            clientPublisher.runSajariSync();
        } catch (FeedException | IOException e) {
            log.error("Failed to parse feed", e);
        }
        log.info("Initial data-sync completed");
    }
}
