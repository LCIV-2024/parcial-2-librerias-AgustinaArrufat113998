package com.example.libreria.controller;

import com.example.libreria.dto.UserRequestDTO;
import com.example.libreria.dto.UserResponseDTO;
import com.example.libreria.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
       // TODO: Implementar la creación de un usuario LISTO
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(requestDTO));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        // TODO: Implementar la obtención de un usuario por su ID LISTO
        return ResponseEntity.ok(userService.getUserById(id));
    }

    
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        //TODO: Implementar la obtención de todos los usuarios LISTO
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO) {
        //TODO: Implementar la actualización de un usuario LISTO
        return ResponseEntity.ok(userService.updateUser(id, requestDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        //TODO: Implementar la eliminación de un usuario LISTO
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

