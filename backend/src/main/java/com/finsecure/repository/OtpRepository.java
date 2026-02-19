package com.finsecure.repository;

import com.finsecure.entity.Otp;
import com.finsecure.entity.Otp.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    @Query("SELECT o FROM Otp o WHERE o.email = :email AND o.purpose = :purpose AND o.used = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<Otp> findValidOtp(String email, OtpPurpose purpose, LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE Otp o SET o.used = true WHERE o.email = :email AND o.purpose = :purpose")
    void invalidatePreviousOtps(String email, OtpPurpose purpose);

    @Modifying
    @Transactional
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(LocalDateTime now);
}
