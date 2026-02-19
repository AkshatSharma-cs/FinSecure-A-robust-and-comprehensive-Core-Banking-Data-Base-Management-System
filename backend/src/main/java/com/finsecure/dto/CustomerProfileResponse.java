package com.finsecure.dto;

import com.finsecure.entity.Customer.KycStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerProfileResponse {

    private Long id;
    private Long userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String panNumber;
    private String aadharNumber;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private KycStatus kycStatus;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
}
