package com.schemaguard.controller;

import com.schemaguard.Service.EmployeeImportService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeImportController {

    private final EmployeeImportService importService;

    public EmployeeImportController(
            EmployeeImportService importService) {

        this.importService = importService;
    }

    @PostMapping(
            value = "/import/{contractId}",
            consumes = "multipart/form-data"
    )
    public Map<String, Object> importEmployees(
            @PathVariable Long contractId,
            @RequestParam("file") MultipartFile file) {

        return importService.importEmployees(file, contractId);
    }
}