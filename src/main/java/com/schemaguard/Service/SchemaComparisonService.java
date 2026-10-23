package com.schemaguard.Service;

import com.schemaguard.entity.DataContract;
import com.schemaguard.repository.DataContractRepository;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SchemaComparisonService {

    private final DataContractRepository repository;

    public SchemaComparisonService(DataContractRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> compareSchemas(Long oldId, Long newId) {

        DataContract oldContract = repository.findById(oldId)
                .orElseThrow(() ->
                        new RuntimeException("Old contract not found: " + oldId));

        DataContract newContract = repository.findById(newId)
                .orElseThrow(() ->
                        new RuntimeException("New contract not found: " + newId));

        Map<String, String> oldSchema =
                parseSchema(oldContract.getSchemaDefinition());

        Map<String, String> newSchema =
                parseSchema(newContract.getSchemaDefinition());

        Set<String> addedColumns = new LinkedHashSet<>(newSchema.keySet());
        addedColumns.removeAll(oldSchema.keySet());

        Set<String> removedColumns = new LinkedHashSet<>(oldSchema.keySet());
        removedColumns.removeAll(newSchema.keySet());

        Map<String, String> changedTypes = new LinkedHashMap<>();

        for (String column : oldSchema.keySet()) {
            if (newSchema.containsKey(column)
                    && !oldSchema.get(column).equalsIgnoreCase(newSchema.get(column))) {

                changedTypes.put(
                        column,
                        oldSchema.get(column) + " -> " + newSchema.get(column)
                );
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("oldContractId", oldId);
        result.put("newContractId", newId);
        result.put("addedColumns", addedColumns);
        result.put("removedColumns", removedColumns);
        result.put("changedColumnTypes", changedTypes);

        return result;
    }

    private Map<String, String> parseSchema(String schemaDefinition) {

        Map<String, String> schema = new LinkedHashMap<>();

        String content = schemaDefinition.trim();

        if (!content.startsWith("{") || !content.endsWith("}")) {
            throw new IllegalArgumentException(
                    "Schema definition must be a JSON object"
            );
        }

        content = content.substring(1, content.length() - 1).trim();

        if (content.isEmpty()) {
            return schema;
        }

        String[] fields = content.split(",");

        for (String field : fields) {
            String[] parts = field.split(":", 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid schema field: " + field
                );
            }

            String column = clean(parts[0]);
            String type = clean(parts[1]);

            if (column.isEmpty() || type.isEmpty()) {
                throw new IllegalArgumentException(
                        "Column names and types cannot be empty"
                );
            }

            schema.put(column, type);
        }

        return schema;
    }

    private String clean(String value) {
        String result = value.trim();

        if (result.startsWith("\"") && result.endsWith("\"")
                && result.length() >= 2) {
            result = result.substring(1, result.length() - 1);
        }

        return result;
    }
}