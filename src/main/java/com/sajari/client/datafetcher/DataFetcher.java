package com.sajari.client.datafetcher;

import com.rometools.rome.io.FeedException;
import com.sajari.client.model.Record;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public interface DataFetcher {

    Iterable<Map<String, String>> fetch(URL url) throws IOException, FeedException;

    Iterable<Map<String, String>> fetch(String url) throws IOException, FeedException;
}
