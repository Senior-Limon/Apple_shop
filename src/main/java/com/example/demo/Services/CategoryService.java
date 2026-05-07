package com.example.demo.Services;

import com.example.demo.Data.Category;
import com.example.demo.Repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

//    public Category findByNameIgnoreCase(String name) {
//        return categoryRepository.findByNameIgnoreCase(name)
//                .orElseThrow(() -> new RuntimeException("Категория не найдена: " + name));
//    }

    //
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Категория не найдена: " + slug));
    }
}