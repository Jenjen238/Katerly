package com.katerly.catering.repository;

import com.katerly.catering.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Subscription> findByMidtransOrderId(String midtransOrderId);
    Optional<Subscription> findByUserUserIdAndStatus(Long userId, Subscription.Status status);
}