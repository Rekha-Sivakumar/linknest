package com.linknest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linknest.model.ShortUrl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataStorageService {

    @Value("${linknest.data-file:./data/linknest-urls.json}")
    private String dataFilePath = "./data/linknest-urls.json";

    private final ObjectMapper mapper;
    private final Map<String, ShortUrl> shortUrls = new ConcurrentHashMap<>();

    public DataStorageService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostConstruct
    public void init() {
        loadData();
        if (shortUrls.isEmpty()) {
            seedDemoData();
            saveData();
        }
    }

    public synchronized void saveData() {
        try {
            Path path = Paths.get(dataFilePath);
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            mapper.writeValue(new File(dataFilePath), shortUrls);
        } catch (IOException e) {
            System.err.println("Warning: Could not save LinkNest data: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            return;
        }

        try {
            Map<String, ShortUrl> data = mapper.readValue(file, new TypeReference<>() {});
            if (data != null) {
                for (Map.Entry<String, ShortUrl> entry : data.entrySet()) {
                    shortUrls.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not parse existing data file: " + e.getMessage());
        }
    }

    public Collection<ShortUrl> getAllShortUrls() {
        return shortUrls.values().stream()
                .sorted(Comparator.comparing(ShortUrl::getCreatedAt).reversed())
                .toList();
    }

    public ShortUrl getShortUrl(String code) {
        if (code == null) return null;
        return shortUrls.get(code.toLowerCase().trim());
    }

    public synchronized void saveShortUrl(ShortUrl shortUrl) {
        if (shortUrl == null || shortUrl.getCode() == null) return;
        shortUrls.put(shortUrl.getCode().toLowerCase().trim(), shortUrl);
        saveData();
    }

    public synchronized boolean deleteShortUrl(String code) {
        if (code == null) return false;
        boolean removed = shortUrls.remove(code.toLowerCase().trim()) != null;
        if (removed) {
            saveData();
        }
        return removed;
    }

    public synchronized void incrementClicks(String code) {
        ShortUrl url = getShortUrl(code);
        if (url != null) {
            url.setTotalClicks(url.getTotalClicks() + 1);
            saveData();
        }
    }

    private void seedDemoData() {
        ShortUrl s1 = new ShortUrl("github", "https://github.com", "GitHub Official");
        s1.setTotalClicks(28);

        ShortUrl s2 = new ShortUrl("portfolio", "https://rekha-sivakumar.github.io/Portfolio", "Developer Portfolio");
        s2.setTotalClicks(64);

        shortUrls.put("github", s1);
        shortUrls.put("portfolio", s2);
    }
}
