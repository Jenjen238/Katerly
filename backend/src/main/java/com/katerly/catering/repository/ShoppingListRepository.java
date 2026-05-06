package com.katerly.catering.repository;

import com.katerly.catering.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ShoppingList> findByShoppingListIdAndUserUserId(Long shoppingListId, Long userId);
}