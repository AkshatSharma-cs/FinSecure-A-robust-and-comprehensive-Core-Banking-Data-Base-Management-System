package com.finsecure.repository;

import com.finsecure.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByUserIdAndOtpCodeAndOtpTypeAndIsVerifiedFalse(
        Long userId,
        String otpCode,
        Otp.OtpType otpType
    );

    List<Otp> findByUserIdAndIsVerifiedFalse(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
