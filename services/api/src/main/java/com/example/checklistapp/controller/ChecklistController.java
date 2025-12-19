package com.example.checklistapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController // marca la clase como controlador REST y serializa respuestas a JSON
@RequestMapping("/checklists") // ruta base para los endpoints de checklist
public class ChecklistController {

    // TODO: Inyectar servicio real que delegue la lógica de negocio y persistencia

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        // Respuesta de ejemplo: devuelve una lista con un solo checklist mock
        // Pendiente: reemplazar por acceso a base de datos a través de servicio/repo
        Map<String,Object> sample = new HashMap<>();
        sample.put("id", 1);
        sample.put("title", "Checklist de ejemplo");
        return ResponseEntity.ok(List.of(sample));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        // Simula creación asignando un id aleatorio; no hay persistencia real aún
        body.put("id", new Random().nextInt(1000) + 2);
        return ResponseEntity.ok(body);
    }
}
