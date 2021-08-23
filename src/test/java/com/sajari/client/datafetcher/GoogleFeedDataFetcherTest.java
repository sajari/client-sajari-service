package com.sajari.client.datafetcher;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
        Iterable<Map<String, String>> results = dataFetcher.fetch(getURL("classpath:cue-small.xml"));

        assertNotNull(results);
        assertEquals(2, size(results));
    }
}