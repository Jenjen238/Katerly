package com.katerly.catering.repository;

import com.katerly.catering.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findByShoppingListShoppingListId(Long shoppingListId);
    void deleteByShoppingListShoppingListId(Long shoppingListId);
}