package com.schemaguard.repository;

import com.schemaguard.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeId(Integer employeeId);
}