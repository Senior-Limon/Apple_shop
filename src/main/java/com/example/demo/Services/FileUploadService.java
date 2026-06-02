package com.example.demo.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    public String saveProductImage(Long productId, MultipartFile file, boolean isMain) throws IOException {
        // Создаём папку для товара
        String productDir = UPLOAD_DIR + "product_" + productId + "/";
        Path productPath = Paths.get(productDir);

        if (!Files.exists(productPath)) {
            Files.createDirectories(productPath);
        }

        // Генерируем имя файла
        String filename = (isMain ? "main_" : "img_") + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = productPath.resolve(filename);

        // Сохраняем файл
        Files.write(filePath, file.getBytes());

        // Возвращаем URL для доступа
        return "/images/products/product_" + productId + "/" + filename;
    }

    public void deleteProductImages(Long productId) throws IOException {
        String productDir = UPLOAD_DIR + "product_" + productId + "/";
        Path productPath = Paths.get(productDir);

        if (Files.exists(productPath)) {
            Files.walk(productPath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}