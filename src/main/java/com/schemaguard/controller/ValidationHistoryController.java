package com.schemaguard.controller;

import com.schemaguard.entity.ValidationHistory;
import com.schemaguard.repository.ValidationHistoryRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validation-history")
public class ValidationHistoryController {

    private final ValidationHistoryRepository repository;

    public ValidationHistoryController(
            ValidationHistoryRepository repository) {

        this.repository = repository;
    }

    // Get all validation history records

    @GetMapping
    public List<ValidationHistory> getAllHistory() {

        return repository.findAll();
    }

    // Get validation history by contract ID

    @GetMapping("/contract/{contractId}")
    public List<ValidationHistory> getHistoryByContractId(
            @PathVariable Long contractId) {

        return repository.findByContractId(contractId);
    }
}