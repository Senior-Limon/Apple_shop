package com.example.demo.Repositories;

import com.example.demo.Data.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Поиск категории по имени
    Optional<Category> findByName(String name);

    // Проверка существования категории
    boolean existsByName(String name);

    // Все категории, отсортированные по имени
    List<Category> findAllByOrderByNameAsc();
}