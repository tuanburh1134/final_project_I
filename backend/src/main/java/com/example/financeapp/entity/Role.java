package com.example.financeapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleID;

    // Tên vai trò (USER, ADMIN)
    @Column(nullable = false, unique = true, length = 256)
    private String roleName;

    @Column(length = 200)
    private String description;
}