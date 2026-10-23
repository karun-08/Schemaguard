package com.schemaguard.controller;

import com.schemaguard.entity.DataContract;
import com.schemaguard.repository.DataContractRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data-contracts")
public class DataContractController {

    private final DataContractRepository repository;

    public DataContractController(DataContractRepository repository) {
        this.repository = repository;
    }

    // Create a new data contract version automatically
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataContract createContract(
            @RequestBody DataContract contract) {

        List<DataContract> existingContracts =
                repository.findByNameOrderByVersionDesc(
                        contract.getName()
                );

        int nextVersion;

        if (existingContracts.isEmpty()) {
            nextVersion = 1;
        } else {
            nextVersion =
                    existingContracts.get(0).getVersion() + 1;
        }

        contract.setVersion(nextVersion);

        return repository.save(contract);
    }

    // Get all data contracts
    @GetMapping
    public List<DataContract> getAllContracts() {
        return repository.findAll();
    }

    // Get a data contract by ID
    @GetMapping("/{id}")
    public DataContract getContractById(
            @PathVariable Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Contract not found: " + id
                        )
                );
    }
}