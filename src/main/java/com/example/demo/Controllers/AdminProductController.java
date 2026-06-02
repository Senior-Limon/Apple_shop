package com.example.demo.Controllers;

import com.example.demo.Data.Category;
import com.example.demo.Data.Product;
import com.example.demo.Data.ProductImage;
import com.example.demo.Data.User;
import com.example.demo.Repositories.CategoryRepository;
import com.example.demo.Repositories.ProductImageRepository;
import com.example.demo.Repositories.ProductRepository;
import com.example.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByPhone(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public String listProducts(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String sort,
                               Model model) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }

        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.searchByNameWithImages("%" + search.trim().toLowerCase() + "%");
            model.addAttribute("searchQuery", search);
        } else {
            products = productRepository.findAllWithImages();
        }

        if (sort != null && products != null) {
            switch (sort) {
                case "price_asc": products.sort((a,b) -> a.getPrice().compareTo(b.getPrice())); break;
                case "price_desc": products.sort((a,b) -> b.getPrice().compareTo(a.getPrice())); break;
                case "name_asc": products.sort((a,b) -> a.getName().compareToIgnoreCase(b.getName())); break;
                case "name_desc": products.sort((a,b) -> b.getName().compareToIgnoreCase(a.getName())); break;
            }
            model.addAttribute("currentSort", sort);
        }

        model.addAttribute("products", products != null ? products : new ArrayList<>());
        model.addAttribute("currentUser", currentUser);
        return "admin/products";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/";
        }
        Product product = productRepository.findByIdWithImages(id).orElse(null);
        if (product == null) return "redirect:/admin/products";
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String saveProduct(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam double price,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(defaultValue = "0") Integer stockQuantity,
                              @RequestParam(required = false) String color,
                              @RequestParam(required = false) String memory,
                              @RequestParam(required = false) String chip,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) MultipartFile[] images,
                              RedirectAttributes redirectAttributes) {
        try {
            Product product;
            if (id != null && id > 0) {
                product = productRepository.findById(id).orElse(new Product());
            } else {
                product = new Product();
            }

            product.setName(name);
            product.setPrice(BigDecimal.valueOf(price));
            product.setStockQuantity(stockQuantity);
            product.setColor(color);
            product.setMemory(memory);
            product.setChip(chip);
            product.setDescription(description);
            product.setIsActive(true);

            if (categoryId != null && categoryId > 0) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                product.setCategory(category);
            }

            Product savedProduct = productRepository.save(product);

            if (images != null && images.length > 0) {
                for (int i = 0; i < images.length; i++) {
                    MultipartFile file = images[i];
                    if (file != null && !file.isEmpty()) {
                        List<ProductImage> existingImages = productImageRepository.findByProductId(savedProduct.getId());
                        boolean isMain = existingImages.isEmpty() && i == 0;
                        String imageUrl = saveImage(savedProduct.getId(), file);
                        ProductImage productImage = new ProductImage();
                        productImage.setProduct(savedProduct);
                        productImage.setImageUrl(imageUrl);
                        productImage.setMain(isMain);
                        productImageRepository.save(productImage);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("message", "Товар сохранён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deleteImageFolder(id);
            productImageRepository.deleteByProductId(id);
            productRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Товар удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка удаления");
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/image/set-main")
    @ResponseBody
    public String setMainImage(@RequestParam Long imageId) {
        try {
            ProductImage newMain = productImageRepository.findById(imageId).orElse(null);
            if (newMain == null) return "error";
            Long productId = newMain.getProduct().getId();
            List<ProductImage> allImages = productImageRepository.findByProductId(productId);
            for (ProductImage img : allImages) {
                img.setMain(img.getId().equals(imageId));
                productImageRepository.save(img);
            }
            return "ok";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/image/delete/{imageId}")
    @ResponseBody
    public String deleteImage(@PathVariable Long imageId) {
        try {
            ProductImage image = productImageRepository.findById(imageId).orElse(null);
            if (image == null) return "error";
            String imagePath = "src/main/resources/static" + image.getImageUrl();
            Files.deleteIfExists(Paths.get(imagePath));
            productImageRepository.deleteById(imageId);
            return "ok";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    private String saveImage(Long productId, MultipartFile file) throws IOException {
        String uploadDir = "src/main/resources/static/images/products/product_" + productId + "/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = "img_" + UUID.randomUUID() + extension;
        Files.write(uploadPath.resolve(filename), file.getBytes());
        return "/images/products/product_" + productId + "/" + filename;
    }

    private void deleteImageFolder(Long productId) throws IOException {
        Path uploadPath = Paths.get("src/main/resources/static/images/products/product_" + productId);
        if (Files.exists(uploadPath)) {
            Files.walk(uploadPath).sorted((a,b) -> b.compareTo(a)).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException e) { }
            });
        }
    }
}