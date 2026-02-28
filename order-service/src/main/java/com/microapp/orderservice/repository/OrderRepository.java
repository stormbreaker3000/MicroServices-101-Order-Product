package com.microapp.orderservice.repository;

import com.microapp.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Order} entities.
 * <p>
 * Provides CRUD operations out of the box (save, findById, findAll, delete,
 * etc.).
 * The H2 schema is auto-created from the entity mappings at startup
 * ({@code spring.jpa.hibernate.ddl-auto=create-drop}).
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Retrieve all orders for a particular product.
     *
     * @param productId The product identifier.
     * @return List of matching orders (may be empty).
     */
    List<Order> findByProductId(String productId);
}
