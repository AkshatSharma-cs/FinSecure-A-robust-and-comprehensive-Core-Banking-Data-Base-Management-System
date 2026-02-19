package com.finsecure.repository;

import com.finsecure.entity.Employee;
import com.finsecure.entity.Employee.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByEmployeeId(String employeeId);

    List<Employee> findByDepartment(Department department);

    boolean existsByEmployeeId(String employeeId);
}
