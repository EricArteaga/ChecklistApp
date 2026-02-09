package com.example.checklistapp.user.controller;

import com.example.checklistapp.user.dto.CreateUserDTO;
import com.example.checklistapp.user.dto.UpdateUserDTO;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody CreateUserDTO dto) {
        UserResponseDTO created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserDTO dto
    ) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
