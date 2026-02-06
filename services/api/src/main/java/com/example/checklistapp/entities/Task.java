package com.example.checklistapp.entities; // Paquete que contiene entidades de base de datos

import jakarta.persistence.*; // Importa anotaciones JPA para mapeo objeto-relacional
import jakarta.validation.constraints.NotBlank; // Validación: campo no vacío ni nulo
import jakarta.validation.constraints.Size; // Validación: límite de longitud
import java.time.LocalDate; // Tipo para fechas sin hora
import java.time.LocalDateTime; // Tipo para fechas con hora

@Entity // Marca esta clase como entidad JPA que se mapea a tabla en base de datos
@Table(name = "tareas", schema = "gestor_tareas") // Especifica nombre de tabla y esquema en PostgreSQL
public class Task {
    
    @Id // Indica que este campo es la clave primaria de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // La base de datos genera el ID automáticamente (serial/auto_increment)
    private Integer id;
    
    @Column(name = "id_usuario", nullable = false) // Mapea a columna id_usuario, NOT NULL en BD
    private Integer idUsuario; // Clave foránea que referencia al usuario propietario de la tarea
    
    @Column(name = "id_tipo") // Mapea a columna id_tipo, NULL permitido en BD (tipo opcional)
    private Integer idTipo; // Clave foránea que referencia al tipo/categoría de la tarea (opcional)
    
    @NotBlank(message = "Name is required") // Validación: campo no vacío ni nulo antes de persistir
    @Size(max = 150) // Validación: límite de longitud máxima a 150 caracteres
    @Column(nullable = false, length = 150) // Columna obligatoria con longitud de 150 caracteres
    private String nombre; // Nombre/descripción corta de la tarea
    
    @Column(columnDefinition = "TEXT") // Usa tipo TEXT de PostgreSQL (sin límite de longitud)
    private String descripcion; // Descripción detallada opcional de la tarea
    
    @Column(nullable = false) // Columna obligatoria en BD
    private Boolean completada = false; // Estado de completitud de la tarea, por defecto false
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false) // Fecha de creación, no se puede modificar
    private LocalDateTime fechaCreacion; // Timestamp automático cuando se crea la tarea
    
    @Column(name = "fecha_programacion") // Fecha en la que está programada la tarea (puede ser null)
    private LocalDate fechaProgramacion; // Fecha objetivo para completar la tarea
    
    @Column(name = "fecha_realizacion") // Fecha en que realmente se completó la tarea
    private LocalDate fechaRealizacion; // Fecha efectiva de completitud
    
    @ManyToOne(fetch = FetchType.LAZY) // Relación muchos-a-uno con User, carga diferida para mejor rendimiento
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false) // Especifica la columna FK, solo lectura
    private User usuario; // Entidad User asociada (cargada bajo demanda)
    
    @ManyToOne(fetch = FetchType.LAZY) // Relación muchos-a-uno con Type, carga diferida
    @JoinColumn(name = "id_tipo", insertable = false, updatable = false) // Especifica la columna FK, solo lectura
    private Type tipo; // Entidad Type asociada (cargada bajo demanda)
    
    @PrePersist // Callback JPA que se ejecuta automáticamente antes de insertar la entidad en BD
    protected void onCreate() {
        // Inicializa automáticamente los campos de fecha y estado al crear la tarea
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now(); // Establece la fecha/hora actual
        }
        if (completada == null) {
            completada = false; // Garantiza que el estado sea false por defecto
        }
    }
    
    public Task() {} // Constructor vacío requerido por JPA/Hibernate
    
    public Task(Integer idUsuario, Integer idTipo, String nombre) {
        // Constructor para crear nuevas tareas con los campos obligatorios
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