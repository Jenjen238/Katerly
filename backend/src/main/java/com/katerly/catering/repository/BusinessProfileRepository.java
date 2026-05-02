package com.katerly.catering.repository;

import com.katerly.catering.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    Optional<BusinessProfile> findByUserUserId(Long userId);
    boolean existsByUserUserId(Long userId);
}