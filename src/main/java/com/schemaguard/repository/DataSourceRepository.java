package com.schemaguard.repository;

import com.schemaguard.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceRepository
        extends JpaRepository<DataSource, Long>
         {
}