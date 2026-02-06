package com.finsecure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Employee", indexes = {
    @Index(name = "idx_department", columnList = "department")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "employee_code", unique = true, nullable = false, length = 50)
    private String employeeCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @Column(length = 100)
    private String designation;

    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Department {
        KYC_VERIFICATION,
        CUSTOMER_SERVICE,
        LOAN_PROCESSING,
        COMPLIANCE,
        IT,
        MANAGEMENT
    }
}
