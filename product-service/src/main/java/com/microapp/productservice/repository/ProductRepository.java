package com.microapp.productservice.repository;

import com.microapp.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Product} entities.
 * <p>
 * Provides CRUD operations out of the box (save, findById, findAll, delete,
 * etc.).
 * The H2 schema is auto-created from the entity mappings at startup
 * ({@code spring.jpa.hibernate.ddl-auto=create-drop}).
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // No custom queries needed at this stage.
    // Add @Query methods here as the service grows (e.g. findByName,
    // searchByPriceRange).
}
