package com.example.checklistapp.subitem.controller;

import com.example.checklistapp.subitem.dto.CreateSubitemDTO;
import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.subitem.dto.UpdateSubitemDTO;
import com.example.checklistapp.subitem.service.SubitemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{idTarea}/subitems")
public class SubitemController {

    private final SubitemService subitemService;

    public SubitemController(SubitemService subitemService) {
        this.subitemService = subitemService;
    }

    @PostMapping
    public ResponseEntity<SubitemResponseDTO> create(
            @PathVariable Integer idTarea,
            @Valid @RequestBody CreateSubitemDTO dto
    ) {
        SubitemResponseDTO created = subitemService.create(idTarea, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SubitemResponseDTO>> findByIdTarea(@PathVariable Integer idTarea) {
        List<SubitemResponseDTO> subitems = subitemService.findByIdTarea(idTarea);
        return ResponseEntity.ok(subitems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubitemResponseDTO> findById(@PathVariable Integer id) {
        SubitemResponseDTO subitem = subitemService.findById(id);
        return ResponseEntity.ok(subitem);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubitemResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSubitemDTO dto
    ) {
        SubitemResponseDTO updated = subitemService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        subitemService.delete(id);
    }
}
