
package com.schemaguard.repository;

import com.schemaguard.entity.ValidationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationHistoryRepository
        extends JpaRepository<ValidationHistory, Long> {

    List<ValidationHistory> findByContractId(Long contractId);
}
