package com.example.crawler.controller;

import org.springframework.web.bind.annotation.*;
import com.example.crawler.service.DatabaseCrawlerService;
import com.example.crawler.model.TableInfo;
import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {
    private final DatabaseCrawlerService crawler;
    public MetadataController(DatabaseCrawlerService crawler) {
        this.crawler = crawler;
    }

    @GetMapping
    public List<TableInfo> getMetadata(@RequestParam(defaultValue="%") String schema) throws Exception {
        return crawler.crawlSchema(schema);
    }
}