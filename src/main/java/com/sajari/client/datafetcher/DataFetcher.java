package com.sajari.client.datafetcher;

import com.rometools.rome.io.FeedException;
import com.sajari.client.model.Record;

import java.io.IOException;
import java.io.InputStream;

public interface DataFetcher {

    Iterable<Record> fetch(InputStream inputStream) throws IOException, FeedException;
}
