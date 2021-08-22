package com.sajari.client.datafetcher;

import com.sajari.client.model.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleFeedDataFetcherTest {


    private GoogleFeedDataFetcher dataFetcher;

    @BeforeEach
    void setUp() {
        dataFetcher = new GoogleFeedDataFetcher();
    }

    @Test
    void fetch() throws Exception {


        URL url = ResourceUtils.getURL("classpath:cue.xml");

        Iterable<Record> records = dataFetcher.fetch(url.openStream());

        assertNotNull(records);
        assertTrue(records.iterator().hasNext());
    }
}