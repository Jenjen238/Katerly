package com.katerly.catering.service;

import com.katerly.catering.dto.response.SubscriptionResponse;
import com.katerly.catering.entity.Subscription;
import com.katerly.catering.entity.User;
import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.SubscriptionRepository;
import com.katerly.catering.repository.UserRepository;
import com.midtrans.Config;
import com.midtrans.ConfigFactory;
import com.midtrans.service.MidtransSnapApi;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${MIDTRANS_SERVER_KEY:${midtrans.server-key:}}")
    private String serverKey;

    @Value("${MIDTRANS_CLIENT_KEY:${midtrans.client-key:}}")
    private String clientKey;

    @Value("${MIDTRANS_IS_PRODUCTION:${midtrans.is-production:false}}")
    private boolean isProduction;

    private static final BigDecimal HARGA_PREMIUM = new BigDecimal("39999");

    // ─── BUAT TRANSAKSI BARU ──────────────────────────────────────────────────────
    @Transactional
    public SubscriptionResponse createTransaction(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        if (user.isPremium()) {
            throw new BadRequestException("Akun kamu sudah Premium!");
        }

        subscriptionRepository.findByUserUserIdAndStatus(userId, Subscription.Status.PENDING)
                .ifPresent(s -> {
                    throw new BadRequestException("Kamu masih memiliki transaksi yang belum dibayar. Order ID: " + s.getMidtransOrderId());
                });

        String orderId = "KTL-" + userId + "-" + System.currentTimeMillis();

        Config config = new ConfigFactory(new Config(serverKey, clientKey, isProduction)).getConfig();
        MidtransSnapApi snapApi = new ConfigFactory(config).getSnapApi();

        Map<String, Object> params = new HashMap<>();

        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", orderId);
        transactionDetails.put("gross_amount", HARGA_PREMIUM.intValue());
        params.put("transaction_details", transactionDetails);

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("first_name", user.getNamaPemilik());
        customerDetails.put("email", user.getEmail());
        params.put("customer_details", customerDetails);

        Map<String, Object> itemDetail = new HashMap<>();
        itemDetail.put("id", "PREMIUM-MONTHLY");
        itemDetail.put("price", HARGA_PREMIUM.intValue());
        itemDetail.put("quantity", 1);
        itemDetail.put("name", "Katerly Premium - 1 Bulan");
        params.put("item_details", List.of(itemDetail));

        try {
            JSONObject result = snapApi.createTransaction(params);
            String snapToken = result.getString("token");
            String paymentUrl = result.getString("redirect_url");

            Subscription subscription = Subscription.builder()
                    .user(user)
                    .midtransOrderId(orderId)
                    .status(Subscription.Status.PENDING)
                    .amount(HARGA_PREMIUM)
                    .build();
            subscription = subscriptionRepository.save(subscription);

            return SubscriptionResponse.builder()
                    .subscriptionId(subscription.getSubscriptionId())
                    .midtransOrderId(orderId)
                    .status(Subscription.Status.PENDING.name())
                    .amount(HARGA_PREMIUM)
                    .snapToken(snapToken)
                    .paymentUrl(paymentUrl)
                    .createdAt(subscription.getCreatedAt())
                    .build();

        } catch (Exception e) {
            throw new BadRequestException("Gagal membuat transaksi: " + e.getMessage());
        }
    }

    // ─── HANDLE WEBHOOK DARI MIDTRANS ─────────────────────────────────────────────
    @Transactional
    public void handleNotification(Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");
        String transactionStatus = (String) payload.get("transaction_status");
        String transactionId = (String) payload.get("transaction_id");
        String fraudStatus = (String) payload.getOrDefault("fraud_status", "accept");

        Subscription subscription = subscriptionRepository.findByMidtransOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaksi tidak ditemukan: " + orderId));

        if ("capture".equals(transactionStatus) || "settlement".equals(transactionStatus)) {
            if ("accept".equals(fraudStatus)) {
                subscription.setStatus(Subscription.Status.SUCCESS);
                subscription.setMidtransTransactionId(transactionId);
                subscription.setStartDate(LocalDate.now());
                subscription.setEndDate(LocalDate.now().plusMonths(1));
                subscriptionRepository.save(subscription);

                User user = subscription.getUser();
                user.setPremium(true);
                userRepository.save(user);
            }
        } else if ("expire".equals(transactionStatus)) {
            subscription.setStatus(Subscription.Status.EXPIRED);
            subscriptionRepository.save(subscription);
        } else if ("cancel".equals(transactionStatus) || "deny".equals(transactionStatus)) {
            subscription.setStatus(Subscription.Status.FAILED);
            subscriptionRepository.save(subscription);
        }
    }

    // ─── CEK STATUS LANGGANAN ─────────────────────────────────────────────────────
    public SubscriptionResponse getActiveSubscription(Long userId) {
        return subscriptionRepository.findByUserUserIdAndStatus(userId, Subscription.Status.SUCCESS)
                .map(s -> SubscriptionResponse.builder()
                        .subscriptionId(s.getSubscriptionId())
                        .midtransOrderId(s.getMidtransOrderId())
                        .midtransTransactionId(s.getMidtransTransactionId())
                        .status(s.getStatus().name())
                        .amount(s.getAmount())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .createdAt(s.getCreatedAt())
                        .build())
                .orElse(null);
    }

    // ─── RIWAYAT TRANSAKSI ────────────────────────────────────────────────────────
    public List<SubscriptionResponse> getHistory(Long userId) {
        return subscriptionRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(s -> SubscriptionResponse.builder()
                        .subscriptionId(s.getSubscriptionId())
                        .midtransOrderId(s.getMidtransOrderId())
                        .midtransTransactionId(s.getMidtransTransactionId())
                        .status(s.getStatus().name())
                        .amount(s.getAmount())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .createdAt(s.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}