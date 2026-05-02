package com.katerly.catering.repository;

import com.katerly.catering.entity.NotaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaItemRepository extends JpaRepository<NotaItem, Long> {
    List<NotaItem> findByNotaNotaId(Long notaId);
    List<NotaItem> findByRecipeRecipeId(Long recipeId);
}