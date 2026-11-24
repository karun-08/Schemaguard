package com.schemaguard.Service;

import com.schemaguard.dto.CsvValidationResult;
import com.schemaguard.entity.DataContract;
import com.schemaguard.entity.ValidationHistory;

import com.schemaguard.repository.DataContractRepository;
import com.schemaguard.repository.ValidationHistoryRepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CsvValidationService {

    private final DataContractRepository repository;
    private final ValidationHistoryRepository historyRepository;
    private final JsonMapper jsonMapper;

    public CsvValidationService(
            DataContractRepository repository,
            ValidationHistoryRepository historyRepository,
            JsonMapper jsonMapper) {

        this.repository = repository;
        this.historyRepository = historyRepository;
        this.jsonMapper = jsonMapper;
    }

    public CsvValidationResult validateCsv(
            MultipartFile file,
            Long contractId) {

        CsvValidationResult result = new CsvValidationResult();

        // 1. Check uploaded file

        if (file == null || file.isEmpty()) {

            result.getErrors().add("CSV file is empty.");
            result.setValid(false);

            return result;
        }

        // 2. Retrieve data contract

        DataContract contract = repository.findById(contractId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Data contract not found: " + contractId
                        ));

        try {

            // 3. Parse contract schema JSON

            Map<String, String> expectedSchema =
                    jsonMapper.readValue(
                            contract.getSchemaDefinition(),
                            new TypeReference<LinkedHashMap<String, String>>() {}
                    );

            if (expectedSchema == null || expectedSchema.isEmpty()) {

                result.getErrors().add(
                        "Data contract contains no schema columns."
                );

                result.setValid(false);

                saveValidationHistory(result, contractId, file);

                return result;
            }

            // 4. Read CSV file

            try (
                    Reader reader = new BufferedReader(
                            new InputStreamReader(
                                    file.getInputStream(),
                                    StandardCharsets.UTF_8
                            )
                    );

                    CSVParser parser = CSVParser.parse(
                            reader,
                            CSVFormat.DEFAULT.builder()
                                    .setHeader()
                                    .setSkipHeaderRecord(true)
                                    .build()
                    )
            ) {

                List<String> headers = parser.getHeaderNames();

                if (headers.isEmpty()) {

                    result.getErrors().add(
                            "CSV file does not contain headers."
                    );

                    result.setValid(false);

                    saveValidationHistory(result, contractId, file);

                    return result;
                }

                // 5. Normalize actual CSV headers

                Map<String, String> actualHeaders =
                        new LinkedHashMap<>();

                for (String header : headers) {

                    String normalized = normalize(header);

                    if (actualHeaders.containsKey(normalized)) {

                        result.getErrors().add(
                                "Duplicate CSV column: " + header
                        );

                    } else {

                        actualHeaders.put(normalized, header);
                    }
                }

                // 6. Detect missing columns

                for (String expectedColumn : expectedSchema.keySet()) {

                    if (!actualHeaders.containsKey(
                            normalize(expectedColumn))) {

                        result.getMissingColumns().add(expectedColumn);
                    }
                }

                // 7. Detect unexpected columns

                for (String actualColumn : headers) {

                    boolean exists = expectedSchema.keySet()
                            .stream()
                            .anyMatch(expected ->
                                    normalize(expected).equals(
                                            normalize(actualColumn)
                                    ));

                    if (!exists) {

                        result.getUnexpectedColumns().add(actualColumn);
                    }
                }

                // 8. Validate individual CSV records

                for (CSVRecord record : parser) {

                    if (!record.isConsistent()) {

                        result.getErrors().add(
                                "Incorrect number of values in row "
                                        + record.getRecordNumber()
                        );

                        continue;
                    }

                    for (Map.Entry<String, String> entry :
                            expectedSchema.entrySet()) {

                        String columnName = entry.getKey();

                        String expectedType = entry.getValue();

                        String actualHeader = actualHeaders.get(
                                normalize(columnName)
                        );

                        // Skip missing columns

                        if (actualHeader == null) {
                            continue;
                        }

                        String value = record.get(actualHeader).trim();

                        // Skip empty values for now

                        if (value.isEmpty()) {
                            continue;
                        }

                        // Check data type

                        if (!isValidType(value, expectedType)) {

                            result.getErrors().add(
                                    "Invalid value in row "
                                            + record.getRecordNumber()
                                            + ", column "
                                            + columnName
                                            + ". Expected "
                                            + expectedType
                                            + " but received: "
                                            + value
                            );
                        }
                    }
                }
            }

        } catch (IOException e) {

            result.getErrors().add(
                    "Unable to read CSV or schema: " + e.getMessage()
            );
        }

        // 9. Calculate final validation status

        result.setValid(
                result.getMissingColumns().isEmpty()
                        && result.getUnexpectedColumns().isEmpty()
                        && result.getErrors().isEmpty()
        );

        // 10. Save validation history in PostgreSQL

        saveValidationHistory(result, contractId, file);

        return result;
    }

    // 11. Save validation history

    private void saveValidationHistory(
            CsvValidationResult result,
            Long contractId,
            MultipartFile file) {

        ValidationHistory history = new ValidationHistory();

        history.setContractId(contractId);

        history.setFileName(
                file.getOriginalFilename() != null
                        ? file.getOriginalFilename()
                        : "unknown.csv"
        );

        history.setValid(result.isValid());

        history.setErrors(
                String.join("\n", result.getErrors())
        );

        history.setMissingColumns(
                String.join(", ", result.getMissingColumns())
        );

        history.setUnexpectedColumns(
                String.join(", ", result.getUnexpectedColumns())
        );

        historyRepository.save(history);
    }

    // 12. Normalize column names

    private String normalize(String value) {

        return value.trim().toLowerCase(Locale.ROOT);
    }

    // 13. Validate supported data types

    private boolean isValidType(
            String value,
            String expectedType) {

        try {

            switch (expectedType.trim().toUpperCase(Locale.ROOT)) {

                case "INTEGER":
                case "INT":

                    Integer.parseInt(value);
                    return true;

                case "LONG":
                case "BIGINT":

                    Long.parseLong(value);
                    return true;

                case "DECIMAL":
                case "NUMERIC":

                    new BigDecimal(value);
                    return true;

                case "DOUBLE":
                case "FLOAT":
                case "REAL":

                    double number = Double.parseDouble(value);

                    return Double.isFinite(number);

                case "BOOLEAN":

                    return value.equalsIgnoreCase("true")
                            || value.equalsIgnoreCase("false");

                case "STRING":
                case "VARCHAR":
                case "TEXT":

                    return true;

                case "DATE":

                    LocalDate.parse(value);
                    return true;

                default:

                    return false;
            }

        } catch (
                NumberFormatException
                | java.time.format.DateTimeParseException e) {

            return false;
        }
    }
}