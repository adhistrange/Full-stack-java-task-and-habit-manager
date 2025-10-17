package com.example.taskhabitmanager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "habits")
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    // dayOfWeek can be "MONDAY", "TUESDAY", or a comma-separated list
    private String dayOfWeek;

    public Habit() {}

    public Habit(String name, String dayOfWeek) {
        this.name = name;
        this.dayOfWeek = dayOfWeek;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
}
