package com.example.checklistapp.type.controller;

import com.example.checklistapp.type.dto.CreateTypeDTO;
import com.example.checklistapp.type.dto.TypeResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.dto.UpdateTypeDTO;
import com.example.checklistapp.type.service.TypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos")
public class TypeController {

    private final TypeService typeService;

    public TypeController(TypeService typeService) {
        this.typeService = typeService;
    }

    @PostMapping
    public ResponseEntity<TypeResponseDTO> create(@Valid @RequestBody CreateTypeDTO dto) {
        TypeResponseDTO created = typeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(typeService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<TypeSummaryDTO>> findAll() {
        return ResponseEntity.ok(typeService.findAll());
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<TypeSummaryDTO>> findByIdUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(typeService.findByIdUsuario(idUsuario));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TypeResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateTypeDTO dto
    ) {
        return ResponseEntity.ok(typeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        typeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
