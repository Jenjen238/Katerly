package com.katerly.catering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shopping_list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_list_item_id")
    private Long shoppingListItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "total_quantity", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalQuantity;

    @Column(nullable = false, length = 20)
    private String satuan;

    @Column(name = "total_harga", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHarga;

    // Untuk fitur centang sudah dibeli, disimpan di DB agar tidak hilang saat logout
    @Column(name = "is_bought", nullable = false)
    @Builder.Default
    private boolean isBought = false;
}