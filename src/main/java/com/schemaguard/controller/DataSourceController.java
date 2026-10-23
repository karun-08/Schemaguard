package com.schemaguard.controller;

import com.schemaguard.entity.DataSource;
import com.schemaguard.repository.DataSourceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data-sources")
public class DataSourceController {

    private final DataSourceRepository repository;

    public DataSourceController(DataSourceRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataSource createDataSource(@RequestBody DataSource dataSource) {
        return repository.save(dataSource);
    }

    @GetMapping
    public List<DataSource> getAllDataSources() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public DataSource getDataSourceById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Data source not found: " + id));
    }
}