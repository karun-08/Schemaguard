package com.schemaguard.controller;

import com.schemaguard.Service.SchemaComparisonService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/schema-comparison")
public class SchemaComparisonController {

    private final SchemaComparisonService comparisonService;

    public SchemaComparisonController(
            SchemaComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping
    public Map<String, Object> compareSchemas(
            @RequestParam Long oldId,
            @RequestParam Long newId) {

        return comparisonService.compareSchemas(oldId, newId);
    }
}