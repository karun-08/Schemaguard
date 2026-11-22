
package com.schemaguard.controller;

import com.schemaguard.Service.CsvValidationService;
import com.schemaguard.dto.CsvValidationResult;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/csv-validation")
public class CsvValidationController {

    private final CsvValidationService validationService;

    public CsvValidationController(
            CsvValidationService validationService) {

        this.validationService = validationService;
    }

    @PostMapping(
            value = "/{contractId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CsvValidationResult validateCsv(
            @PathVariable Long contractId,
            @RequestParam("file") MultipartFile file) {

        return validationService.validateCsv(file, contractId);
    }
}
