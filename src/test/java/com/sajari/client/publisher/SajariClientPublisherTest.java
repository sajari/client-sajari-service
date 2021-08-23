package com.sajari.client.publisher;

import com.sajari.client.ApiClient;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.datafetcher.DataFetcher;
import com.sajari.client.model.Record;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.net.URL;
import java.util.Map;

import static org.springframework.util.ResourceUtils.getURL;

@RunWith(SpringRunner.class)
@SpringBootTest
class SajariClientPublisherTest {

    @Autowired
    private SajariClientPublisher clientPublisher;

    @Autowired
    private DataFetcher dataFetcher;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void sendToSajari() throws Exception {

        Iterable<Map<String, String>> records = dataFetcher.fetch(getURL("classpath:cue-small.xml"));

        clientPublisher.sendToSajari(records);
    }
}