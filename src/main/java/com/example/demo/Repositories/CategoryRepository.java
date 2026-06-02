package com.example.demo.Repositories;

import com.example.demo.Data.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    Optional<Category> findByNameIgnoreCase(String name);
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);

    // Сортировка по имени (для админки)
    List<Category> findAllByOrderByNameAsc();

    // Сортировка по порядку (для пользовательской части)
    List<Category> findAllByOrderBySortOrderAsc();

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.sortOrder = c.sortOrder + 1 WHERE c.sortOrder >= :fromOrder")
    void incrementOrderFrom(@Param("fromOrder") int fromOrder);

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.sortOrder = c.sortOrder - 1 WHERE c.sortOrder BETWEEN :fromOrder AND :toOrder")
    void decrementOrderBetween(@Param("fromOrder") int fromOrder, @Param("toOrder") int toOrder);
}