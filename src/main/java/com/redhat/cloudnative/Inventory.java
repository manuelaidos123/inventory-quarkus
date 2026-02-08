package com.redhat.cloudnative;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Table(name = "INVENTORY")
public class Inventory extends PanacheEntity {

    @Column
    @Min(value = 0, message = "Quantity cannot be negative")
    public int quantity;

    @Override
    public String toString() {
        return "Inventory [Id='" + id + '\'' + ", quantity=" + quantity + ']';
    }
}