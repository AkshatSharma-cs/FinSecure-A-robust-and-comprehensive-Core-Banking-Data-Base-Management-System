package com.finsecure.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private String role;
    private Long userId;
    private String username;
    private String email;
    private boolean otpRequired;
}
