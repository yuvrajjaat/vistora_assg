package com.example.crawler.controller;

import org.springframework.web.bind.annotation.*;
import com.example.crawler.service.*;
import com.example.crawler.model.TableInfo;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/models")
public class ModelController {
    private final DatabaseCrawlerService crawler;
    private final ModelGeneratorService generator;

    public ModelController(DatabaseCrawlerService crawler, ModelGeneratorService generator) {
        this.crawler = crawler;
        this.generator = generator;
    }

    @PostMapping("/generate")
    public String generate(@RequestParam(defaultValue="%") String schema) throws Exception {
        List<TableInfo> metadata = crawler.crawlSchema(schema);
        generator.generateModels(metadata);
        return "Models generated: " + metadata.size();
    }

    @GetMapping("/list")
public List<String> listModels() {
    return generator.getGeneratedModels()
        .stream()
        .map(Class::getName)
        .collect(Collectors.toList());
}

}