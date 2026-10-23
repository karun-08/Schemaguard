package com.schemaguard.repository;

import com.schemaguard.entity.DataContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataContractRepository
        extends JpaRepository<DataContract, Long> {

    List<DataContract> findByName(String name);

    List<DataContract> findByNameOrderByVersionDesc(String name);
}