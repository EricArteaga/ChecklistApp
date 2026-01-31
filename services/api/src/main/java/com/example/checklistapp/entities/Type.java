package com.example.checklistapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity // marca esta clase como entidad JPA para la tabla de tipos/categorías
@Table(name = "tipos", schema = "gestor_tareas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_usuario", "nombre"}) // asegura que un usuario no tenga tipos duplicados con el mismo nombre
})
public class Type {
    
    @Id // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID generado por la base de datos
    private Integer id;
    
    @Column(name = "id_usuario", nullable = false) // columna de clave foránea al usuario
    private Integer idUsuario; // usuario propietario de este tipo/categoría
    
    @NotBlank(message = "Name is required") // validación: el nombre es obligatorio
    @Size(max = 100) // longitud máxima de 100 caracteres
    @Column(nullable = false, length = 100) // columna NOT NULL con longitud 100
    private String nombre; // nombre del tipo/categoría (ej: "Trabajo", "Personal", "Salud")
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$|^#[0-9A-Fa-f]{3}$|^$", message = "Color must be a valid HEX color") // valida formato HEX
    @Column(length = 50) // columna de hasta 50 caracteres para el color
    private String color; // color en formato HEX (ej: "#FF5733") para visualización en UI
    
    @ManyToOne(fetch = FetchType.LAZY) // relación muchos-a-uno con User, carga diferida
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false) // FK de solo lectura
    private User usuario; // usuario propietario del tipo
    
    @OneToMany(mappedBy = "tipo", cascade = CascadeType.ALL, orphanRemoval = true) // relación uno-a-muchos bidireccional
    private List<Task> tareas = new ArrayList<>(); // lista de tareas asociadas a este tipo (se cargan bajo demanda)
    
    public Type() {} // constructor vacío requerido por JPA
    
    public Type(Integer idUsuario, String nombre, String color) {
        // constructor para crear nuevos tipos con los campos principales
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.color = color;
    }
    
    // === Getters y Setters ===
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    
    public List<Task> getTareas() { return tareas; }
    public void setTareas(List<Task> tareas) { this.tareas = tareas; }
}