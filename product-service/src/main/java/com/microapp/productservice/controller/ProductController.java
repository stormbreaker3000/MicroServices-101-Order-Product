package com.microapp.productservice.controller;

import com.microapp.productservice.dto.ProductResponse;
import com.microapp.productservice.model.Product;
import com.microapp.productservice.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing product endpoints.
 * <p>
 * Base path: /products
 * Consumed by order-service (synchronous REST call) and external clients.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieve all products.
     *
     * <pre>
     * GET /products → 200 OK with list (empty if no products)
     * </pre>
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    /**
     * Fetch a single product by its database ID.
     *
     * <pre>
     * GET /products/{id}
     * 200 OK  – product found
     * 404     – product not found
     * </pre>
     *
     * @param id Path variable: the product's numeric ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return productService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity
                        .status(404)
                        .body(Map.of("message", "Product not found")));
    }

    /**
     * Create and persist a new product.
     *
     * <pre>
     * POST /products
     * Content-Type: application/json
     * Body: { "name": "Keyboard", "price": 49.99, "stock": 10 }
     *
     * 201 Created – product saved
     * 400         – validation failure
     * </pre>
     *
     * @param request Validated product request body.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product entity = new Product(request.name(), request.price(), request.stock());
        ProductResponse saved = productService.save(entity);
        return ResponseEntity.status(201).body(saved);
    }

    // ------------------------------------------------------------------
    // Inner request DTO (kept here since it's controller-specific input)
    // ------------------------------------------------------------------

    /**
     * Request body for creating a product.
     *
     * @param name  Product name – must not be blank.
     * @param price Unit price – must be positive.
     * @param stock Initial stock – must be zero or more.
     */
    public record CreateProductRequest(
            @NotBlank(message = "name must not be blank") String name,

            @DecimalMin(value = "0.01", message = "price must be greater than 0") BigDecimal price,

            @Min(value = 0, message = "stock must not be negative") int stock) {
    }
}
