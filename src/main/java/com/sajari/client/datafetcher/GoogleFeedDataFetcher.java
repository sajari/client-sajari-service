package com.sajari.client.datafetcher;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sajari.client.model.Record;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

@Slf4j
public class GoogleFeedDataFetcher implements DataFetcher {

    @Override
    public Iterable<Record> fetch(InputStream inputStream) throws IOException, FeedException {

        Gson gson = new Gson();
        Set<Record> records = new HashSet<>();
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(inputStream));

        for (SyndEntry entry : feed.getEntries()) {

            Map<String, String> recordMap = Maps.newHashMap();

            for (Element element : entry.getForeignMarkup()) {
                recordMap.put(element.getName(), trimToEmpty(element.getText()));
            }

            JsonElement jsonElement = gson.toJsonTree(recordMap);
            Record record = gson.fromJson(jsonElement, Record.class);
            records.add(record);
        }
        return records;
    }
}
