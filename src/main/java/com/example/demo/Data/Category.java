package com.example.demo.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true)                // ← НОВОЕ ПОЛЕ
    private String slug;                  // ← НОВОЕ ПОЛЕ

    // геттеры и сеттеры (Lombok @Data уже генерирует)
}