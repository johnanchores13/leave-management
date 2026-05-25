package com.exprivia.leave_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
@Data
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private String description;

    public Holiday() {

    }

    public Holiday(LocalDate date, String description) {
        this.date = date;
        this.description = description;
    }

}
