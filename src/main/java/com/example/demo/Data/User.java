package com.example.demo.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String phone;
    private String role = "USER";

    // Свой метод (Lombok не трогает)
    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }
}

