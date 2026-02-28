package com.microapp.orderservice.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA entity representing a placed order stored in the H2 database.
 * <p>
 * Each order references a product by the product's ID (a Long) obtained
 * from the product-service. Orders are independent of the product entity —
 * there is no {@code @ManyToOne} join because these are separate microservices
 * with separate databases.
 */
@Entity
@Table(name = "orders")
public class Order {

    /**
     * Primary key – UUID string assigned in the service layer before persisting.
     */
    @Id
    private String id;

    /**
     * ID of the product that was ordered, as returned by the product-service.
     */
    @Column(nullable = false)
    private String productId;

    /**
     * Number of units ordered.
     */
    @Column(nullable = false)
    private int quantity;

    /**
     * Current order lifecycle status (e.g. "CREATED").
     */
    @Column(nullable = false)
    private String status;

    /**
     * UTC timestamp of when this order was created.
     */
    @Column(nullable = false)
    private Instant createdAt;

    // ------------------------------------------------------------------
    // JPA requires a no-arg constructor
    // ------------------------------------------------------------------

    protected Order() {
    }

    /**
     * Convenience all-args constructor.
     *
     * @param id        Pre-generated UUID string.
     * @param productId ID of the ordered product.
     * @param quantity  Number of units.
     * @param status    Initial status (e.g. "CREATED").
     * @param createdAt Creation timestamp.
     */
    public Order(String id, String productId, int quantity, String status, Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ------------------------------------------------------------------
    // Getters and setters
    // ------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String pid) {
        this.productId = pid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant ts) {
        this.createdAt = ts;
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', productId='" + productId +
                "', quantity=" + quantity + ", status='" + status + "', createdAt=" + createdAt + '}';
    }
}
