package com.example.checklistapp.entities; // paquete de entidades del dominio

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity // marca esta clase como entidad JPA que se mapea a una tabla de base de datos
@Table(name = "usuarios", schema = "gestor_tareas") // tabla de usuarios en el esquema gestor_tareas
public class User {
    
    @Id // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-generado por la BD
    private Integer id;
    
    @NotBlank(message = "Email is required") // validación: email es obligatorio
    @Email(message = "Email should be valid") // validación: formato de email válido
    @Column(unique = true, nullable = false, length = 255) // columna única, NOT NULL, hasta 255 caracteres
    private String correo; // dirección de email del usuario (usada como identificador de login)
    
    @NotBlank(message = "Name is required") // validación: nombre es obligatorio
    @Size(max = 100) // longitud máxima de 100 caracteres
    @Column(nullable = false, length = 100) // columna NOT NULL con longitud 100
    private String nombre; // nombre completo del usuario
    
    @NotBlank(message = "Password is required") // validación: contraseña es obligatoria
    @Column(nullable = false, length = 255) // columna NOT NULL con longitud 255 (para hashes bcrypt)
    private String hashContrasena; // hash de la contraseña (nunca almacenar la contraseña en texto plano)
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false) // fecha de creación inmutable
    private LocalDateTime fechaCreacion; // timestamp automático de registro del usuario
    
    @PrePersist // callback JPA ejecutado antes de insertar el usuario en BD
    protected void onCreate() {
        // Establece automáticamente la fecha de creación si no ha sido asignada
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
    
    public User() {} // constructor vacío requerido por JPA
    
    public User(String correo, String nombre, String hashContrasena) {
        // constructor para registrar nuevos usuarios
        this.correo = correo;
        this.nombre = nombre;
        this.hashContrasena = hashContrasena;
    }
    
    // === Getters y Setters (JavaBean pattern para acceso encapsulado) ===
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getHashContrasena() { return hashContrasena; }
    public void setHashContrasena(String hashContrasena) { this.hashContrasena = hashContrasena; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}