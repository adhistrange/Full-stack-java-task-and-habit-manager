package com.example.taskhabitmanager.controller;

import com.example.taskhabitmanager.model.Habit;
import com.example.taskhabitmanager.repository.HabitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(origins = "*")
public class HabitController {
    private final HabitRepository repo;

    public HabitController(HabitRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Habit> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getById(@PathVariable Long id) {
        Optional<Habit> h = repo.findById(id);
        return h.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Habit create(@RequestBody Habit habit) {
        habit.setId(null);
        return repo.save(habit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> update(@PathVariable Long id, @RequestBody Habit updated) {
        return repo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDayOfWeek(updated.getDayOfWeek());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repo.findById(id).map(existing -> {
            repo.delete(existing);
            return ResponseEntity.ok().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
