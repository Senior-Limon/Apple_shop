package com.example.demo.Repositories;

import com.example.demo.Data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);           // для входа
    Optional<User> findByFullName(String fullName);     // поиск по ФИО
    boolean existsByPhone(String phone);
    boolean existsByFullName(String fullName);
    List<User> findByRole(String role);
}