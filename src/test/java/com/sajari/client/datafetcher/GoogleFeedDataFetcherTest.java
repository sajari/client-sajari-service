package com.sajari.client.datafetcher;

import com.sajari.client.model.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Iterables.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.util.ResourceUtils.getURL;

class GoogleFeedDataFetcherTest {

    private GoogleFeedDataFetcher dataFetcher;

    @BeforeEach
    void setUp() {
        dataFetcher = new GoogleFeedDataFetcher();
    }

    @Test
    void fetch() throws Exception {
        Iterable<Record> results = dataFetcher.fetch(getURL("classpath:cue-small.xml"));

        assertNotNull(results);
        assertEquals(76, size(results));
    }
}