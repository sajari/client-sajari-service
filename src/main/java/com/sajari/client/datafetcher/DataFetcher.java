package com.sajari.client.datafetcher;

import com.rometools.rome.io.FeedException;
import com.sajari.client.model.Record;

import java.io.IOException;
import java.net.URL;

public interface DataFetcher {

    Iterable<Record> fetch(URL url) throws IOException, FeedException;

    Iterable<Record> fetch(String url) throws IOException, FeedException;
}
