package com.schemaguard.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvValidationResult {

    private boolean valid;

    private List<String> missingColumns = new ArrayList<>();
    private List<String> unexpectedColumns = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public CsvValidationResult() {
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getMissingColumns() {
        return missingColumns;
    }

    public void setMissingColumns(List<String> missingColumns) {
        this.missingColumns = missingColumns;
    }

    public List<String> getUnexpectedColumns() {
        return unexpectedColumns;
    }

    public void setUnexpectedColumns(List<String> unexpectedColumns) {
        this.unexpectedColumns = unexpectedColumns;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}

 
    

