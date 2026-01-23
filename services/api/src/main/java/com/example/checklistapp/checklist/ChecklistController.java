package com.example.checklistapp.checklist;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController // marca la clase como controlador REST y serializa respuestas a JSON
@RequestMapping("api/checklists") // ruta base para los endpoints de checklist
public class ChecklistController {

    private final ChecklistService checklistService; // servicio de negocio inyectado por constructor

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService; // asigna el servicio inyectado para uso en los endpoints
    }
    
    // TODO: Inyectar servicio real que delegue la lógica de negocio y persistencia

    @GetMapping // endpoint HTTP GET para obtener todos los checklists
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        // Respuesta de ejemplo: devuelve una lista con un solo checklist mock
        // Pendiente: reemplazar por acceso a base de datos a través de servicio/repo
        Map<String,Object> sample = new HashMap<>();
        sample.put("id", 1); // identificador único del checklist
        sample.put("title", "Checklist de ejemplo"); // título descriptivo del checklist
        return ResponseEntity.ok(List.of(sample)); // envuelve la lista en ResponseEntity con status 200
    }

    @PostMapping // endpoint HTTP POST para crear nuevos checklists
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        // Simula creación asignando un id aleatorio; no hay persistencia real aún
        body.put("id", new Random().nextInt(1000) + 2); // genera ID aleatorio entre 2-1001
        return ResponseEntity.ok(body); // devuelve el objeto creado con status 200
    }
}
