package com.example.demo.Services;

import com.example.demo.Data.User;
import com.example.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int MIN_PASSWORD_LENGTH = 6;

    // ==================== ВАЛИДАЦИЯ ФИО ====================
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("ФИО не может быть пустым");
        }
        if (fullName.length() < 3) {
            throw new RuntimeException("ФИО должно быть минимум 3 символа");
        }
        if (fullName.length() > 100) {
            throw new RuntimeException("ФИО не должно превышать 100 символов");
        }
        if (!fullName.matches("^[a-zA-Zа-яА-ЯёЁ\\s\\-\\.]+$")) {
            throw new RuntimeException("ФИО может содержать только буквы, пробелы, дефисы и точки");
        }
        if (fullName.matches(".*\\s{3,}.*")) {
            throw new RuntimeException("ФИО не должно содержать более 2 пробелов подряд");
        }
        String[] parts = fullName.trim().split("\\s+");
        for (String part : parts) {
            if (part.length() > 0 && !Character.isUpperCase(part.charAt(0))) {
                throw new RuntimeException("Каждое слово в ФИО должно начинаться с заглавной буквы");
            }
        }
    }

    // ==================== ВАЛИДАЦИЯ ТЕЛЕФОНА ПО СТРАНЕ ====================
    private String getFullPhone(String countryCode, String phoneNumber) {
        String cleanPhone = phoneNumber.replaceAll("\\s", "");

        switch (countryCode) {
            case "BY": return "+375" + cleanPhone;
            case "RU": return "+7" + cleanPhone;
            case "PL": return "+48" + cleanPhone;
            case "LT": return "+370" + cleanPhone;
            case "LV": return "+371" + cleanPhone;
            case "KZ": return "+7" + cleanPhone;
            default: throw new RuntimeException("Неподдерживаемая страна");
        }
    }

    private void validatePhoneByCountry(String countryCode, String phoneNumber) {
        String cleanPhone = phoneNumber.replaceAll("\\s", "");

        switch (countryCode) {
            case "BY":
                if (!cleanPhone.matches("^(29|33|44|25)\\d{7}$")) {
                    throw new RuntimeException("Неверный формат белорусского номера. Пример: 291234567 (9 цифр после 375)");
                }
                break;
            case "RU":
                if (!cleanPhone.matches("^9\\d{9}$")) {
                    throw new RuntimeException("Неверный формат российского номера. Пример: 9123456789 (10 цифр, начинается с 9)");
                }
                break;
            case "PL":
                if (!cleanPhone.matches("^\\d{9}$")) {
                    throw new RuntimeException("Неверный формат польского номера. Пример: 123456789 (9 цифр)");
                }
                break;
            case "LT":
                if (!cleanPhone.matches("^6\\d{7}$")) {
                    throw new RuntimeException("Неверный формат литовского номера. Пример: 61234567 (8 цифр, начинается с 6)");
                }
                break;
            case "LV":
                if (!cleanPhone.matches("^2\\d{7}$")) {
                    throw new RuntimeException("Неверный формат латвийского номера. Пример: 21234567 (8 цифр, начинается с 2)");
                }
                break;
            case "KZ":
                if (!cleanPhone.matches("^7\\d{9}$")) {
                    throw new RuntimeException("Неверный формат казахстанского номера. Пример: 7771234567 (10 цифр, начинается с 7)");
                }
                break;
            default:
                throw new RuntimeException("Неподдерживаемая страна");
        }
    }

    // ==================== SPRING SECURITY ====================
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + phone));

        String role = "ROLE_" + user.getRole();

        return new org.springframework.security.core.userdetails.User(
                user.getPhone(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    // ==================== РЕГИСТРАЦИЯ ====================
    @Transactional
    public String register(String fullName, String password, String countryCode, String phoneNumber) {
        // Валидация ФИО
        validateFullName(fullName);

        // Валидация пароля
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Пароль должен быть минимум " + MIN_PASSWORD_LENGTH + " символов");
        }

        // Валидация телефона по стране
        validatePhoneByCountry(countryCode, phoneNumber);

        // Формируем полный номер
        String fullPhone = getFullPhone(countryCode, phoneNumber);

        // Проверка на уникальность
        if (userRepository.existsByPhone(fullPhone)) {
            throw new RuntimeException("Этот номер телефона уже зарегистрирован");
        }

        // Создание пользователя
        User user = new User();
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(fullPhone);
        user.setRole("USER");

        userRepository.save(user);
        return fullPhone;
    }

    @Transactional
    public User registerAdmin(String fullName, String password, String phone) {
        validateFullName(fullName);

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Пароль должен быть минимум " + MIN_PASSWORD_LENGTH + " символов");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Телефон не может быть пустым");
        }

        // Проверка формата телефона для админа (международный формат)
        if (!phone.matches("^\\+?[0-9]{10,15}$")) {
            throw new RuntimeException("Неверный формат телефона (должен быть + и 10-15 цифр)");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Телефон уже зарегистрирован");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole("ADMIN");

        return userRepository.save(user);
    }

    // ==================== ПОИСК ПОЛЬЗОВАТЕЛЕЙ ====================
    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public User getUserByLogin(String login) {
        return getUserByPhone(login);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // ==================== АУТЕНТИФИКАЦИЯ ====================
    public boolean login(String phone, String password) {
        User user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) return false;
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    // ==================== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ====================
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Пароль должен быть минимум " + MIN_PASSWORD_LENGTH + " символов");
        }
        User user = getUserById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changePasswordWithCheck(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Текущий пароль неверен");
        }

        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Пароль должен быть минимум " + MIN_PASSWORD_LENGTH + " символов");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ==================== ПОЛУЧЕНИЕ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ ====================
    public User getCurrentUser() {
        String phone = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return getUserByPhone(phone);
    }
}