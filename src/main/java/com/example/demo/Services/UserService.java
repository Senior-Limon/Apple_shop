package com.example.demo.Services;

import com.example.demo.Data.User;
import com.example.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {  // ← ДОБАВЛЯЕМ implements

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int MIN_PASSWORD_LENGTH = 8;

    // ===== ЭТОТ МЕТОД НУЖЕН SPRING SECURITY =====
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPasswordHash(),
                Collections.emptyList()  // роли пока пустые
        );
    }

    // Регистрация
    @Transactional
    public User register(String login, String password, String phone) {
        // 1. Проверка логина
        if (login == null || login.trim().isEmpty()) {
            throw new RuntimeException("Логин не может быть пустым");
        }
        if (login.length() < 3) {
            throw new RuntimeException("Логин должен быть минимум 3 символа");
        }
        if (userRepository.existsByLogin(login)) {
            throw new RuntimeException("Логин уже занят");
        }

        // 2. Проверка пароля (длина 8+)
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Пароль должен быть минимум " + MIN_PASSWORD_LENGTH + " символов");
        }

        // 3. Проверка телефона
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Телефон не может быть пустым");
        }
        if (!phone.matches("\\+?[0-9]{10,15}")) {
            throw new RuntimeException("Неверный формат телефона (должен быть + и 10-15 цифр)");
        }

        // 4. Создание пользователя
        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole("USER");

        return userRepository.save(user);
    }

    // Вход (можно оставить для других нужд, но Spring Security теперь не использует его)
    public boolean login(String login, String password) {
        if (login == null || password == null) {
            return false;
        }

        User user = userRepository.findByLogin(login).orElse(null);
        if (user == null) {
            return false;
        }

        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    // Получить пользователя по логину
    public User getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // Получить пользователя по ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // Все пользователи (для админки)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Удалить пользователя
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }

    // Проверить существование логина
    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    // Проверить существование телефона
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}