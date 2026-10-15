package io.github.ghoshsa.mule.inventory.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "InventoryItems")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {
    @Id
    private String productId;

    @Column (nullable = false)
    private Integer availableQuantity;

    @Column (nullable = false)
    private BigDecimal price;

    public InventoryItem(String productId, Integer availableQuantity, BigDecimal price) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.price = price;
    }
}