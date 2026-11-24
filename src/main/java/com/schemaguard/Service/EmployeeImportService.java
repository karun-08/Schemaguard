package com.schemaguard.Service;

import com.schemaguard.entity.Employee;
import com.schemaguard.dto.CsvValidationResult;
import com.schemaguard.repository.EmployeeRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EmployeeImportService {

    private final EmployeeRepository employeeRepository;
    private final CsvValidationService validationService;

    public EmployeeImportService(
            EmployeeRepository employeeRepository,
            CsvValidationService validationService) {

        this.employeeRepository = employeeRepository;
        this.validationService = validationService;
    }

    @Transactional
    public Map<String, Object> importEmployees(
            MultipartFile file,
            Long contractId) {

        Map<String, Object> response = new LinkedHashMap<>();

        // 1. Validate CSV before importing

        CsvValidationResult validationResult =
                validationService.validateCsv(file, contractId);

        if (!validationResult.isValid()) {

            response.put("message",
                    "Import rejected. CSV validation failed.");

            response.put("validationErrors",
                    validationResult.getErrors());

            response.put("missingColumns",
                    validationResult.getMissingColumns());

            response.put("unexpectedColumns",
                    validationResult.getUnexpectedColumns());

            response.put("importedCount", 0);

            return response;
        }

        int importedCount = 0;
        int duplicateCount = 0;

        // 2. Read and parse CSV

        try (
                Reader reader = new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8
                );

                CSVParser parser = CSVParser.parse(
                        reader,
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .build()
                )
        ) {

            // 3. Process each employee record

            for (CSVRecord record : parser) {

                Integer employeeId = Integer.parseInt(
                        record.get("employee_id").trim()
                );

                // 4. Prevent duplicate employee IDs

                if (employeeRepository.existsByEmployeeId(employeeId)) {

                    duplicateCount++;
                    continue;
                }

                // 5. Create employee entity

                Employee employee = new Employee();

                employee.setEmployeeId(employeeId);

                employee.setName(
                        record.get("name").trim()
                );

                employee.setSalary(
                        Integer.parseInt(
                                record.get("salary").trim()
                        )
                );

                employee.setDepartment(
                        record.get("department").trim()
                );

                // 6. Save employee in PostgreSQL

                employeeRepository.save(employee);

                importedCount++;
            }

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Employee import failed: " + e.getMessage(),
                    e
            );
        }

        // 7. Prepare response

        response.put("message",
                "Employee import completed successfully.");

        response.put("importedCount", importedCount);

        response.put("duplicateCount", duplicateCount);

        response.put("contractId", contractId);

        return response;
    }
}