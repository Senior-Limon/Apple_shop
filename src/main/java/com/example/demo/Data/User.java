package com.example.demo.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true, nullable = false, length = 100)
    private String fullName;          // ← здесь храним ФИО (в БД колонка login)

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(unique = true, nullable = false)
    private String phone;

    private String role = "USER";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User(Long userId) {
        this.id = userId;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}