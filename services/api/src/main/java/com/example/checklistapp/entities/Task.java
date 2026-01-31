package com.example.checklistapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity // marca esta clase como entidad JPA que se mapea a una tabla de base de datos
@Table(name = "tareas", schema = "gestor_tareas") // especifica nombre de tabla y esquema en PostgreSQL
public class Task {
    
    @Id // indica que este campo es la clave primaria de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la base de datos genera el ID automáticamente (serial/auto_increment)
    private Integer id;
    
    @Column(name = "id_usuario", nullable = false) // mapea a columna id_usuario, NOT NULL en BD
    private Integer idUsuario; // clave foránea que referencia al usuario propietario de la tarea
    
    @Column(name = "id_tipo", nullable = false) // mapea a columna id_tipo, NOT NULL en BD
    private Integer idTipo; // clave foránea que referencia al tipo/categoría de la tarea
    
    @NotBlank(message = "Name is required") // valida que el campo no sea nulo ni vacío antes de persistir
    @Size(max = 150) // limita la longitud máxima a 150 caracteres
    @Column(nullable = false, length = 150) // columna obligatoria con longitud de 150 caracteres
    private String nombre; // nombre/descripción corta de la tarea
    
    @Column(columnDefinition = "TEXT") // usa tipo TEXT de PostgreSQL (sin límite de longitud)
    private String descripcion; // descripción detallada opcional de la tarea
    
    @Column(nullable = false) // columna obligatoria en BD
    private Boolean completada = false; // estado de completitud de la tarea, por defecto false
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false) // fecha de creación, no se puede modificar
    private LocalDateTime fechaCreacion; // timestamp automático cuando se crea la tarea
    
    @Column(name = "fecha_programacion") // fecha en la que está programada la tarea (puede ser null)
    private LocalDate fechaProgramacion; // fecha objetivo para completar la tarea
    
    @Column(name = "fecha_realizacion") // fecha en que realmente se completó la tarea
    private LocalDate fechaRealizacion; // fecha efectiva de completitud
    
    @ManyToOne(fetch = FetchType.LAZY) // relación muchos-a-uno con User, carga diferida para mejor rendimiento
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false) // especifica la columna FK, solo lectura
    private User usuario; // entidad User asociada (cargada bajo demanda)
    
    @ManyToOne(fetch = FetchType.LAZY) // relación muchos-a-uno con Type, carga diferida
    @JoinColumn(name = "id_tipo", insertable = false, updatable = false) // especifica la columna FK, solo lectura
    private Type tipo; // entidad Type asociada (cargada bajo demanda)
    
    @PrePersist // callback JPA que se ejecuta automáticamente antes de insertar la entidad en BD
    protected void onCreate() {
        // Inicializa automáticamente los campos de fecha y estado al crear la tarea
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now(); // establece la fecha/hora actual
        }
        if (completada == null) {
            completada = false; // garantiza que el estado sea false por defecto
        }
    }
    
    public Task() {} // constructor vacío requerido por JPA/Hibernate
    
    public Task(Integer idUsuario, Integer idTipo, String nombre) {
        // constructor para crear nuevas tareas con los campos obligatorios
        this.idUsuario = idUsuario;
        this.idTipo = idTipo;
        this.nombre = nombre;
    }
    
    // === Getters y Setters (JavaBean pattern para acceso encapsulado) ===
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    
    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) { this.idTipo = idTipo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public Boolean getCompletada() { return completada; }
    public void setCompletada(Boolean completada) { this.completada = completada; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDate getFechaProgramacion() { return fechaProgramacion; }
    public void setFechaProgramacion(LocalDate fechaProgramacion) { this.fechaProgramacion = fechaProgramacion; }
    
    public LocalDate getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDate fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }
    
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }
    
    public Type getTipo() { return tipo; }
    public void setTipo(Type tipo) { this.tipo = tipo; }
}