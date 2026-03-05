package com.example.checklistapp.task.model;

import com.example.checklistapp.subitem.model.Subitem;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.user.model.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tareas")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    // Usuario opcional para soportar tareas anónimas
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false)
    private User usuario;

    @Column(name = "id_tipo")
    private Integer idTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", insertable = false, updatable = false)
    private Type tipo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_programacion")
    private LocalDate fechaProgramacion;

    @Column(name = "fecha_realizacion")
    private LocalDate fechaRealizacion;

    @Column(nullable = false)
    private Boolean completada = false;

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subitem> subitems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (completada == null) {
            completada = false;
        }
    }

    public void addSubitem(Subitem subitem) {
        subitems.add(subitem);
        subitem.setTarea(this);
    }

    public void removeSubitem(Subitem subitem) {
        subitems.remove(subitem);
        subitem.setTarea(null);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }

    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) { this.idTipo = idTipo; }

    public Type getTipo() { return tipo; }
    public void setTipo(Type tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDate getFechaProgramacion() { return fechaProgramacion; }
    public void setFechaProgramacion(LocalDate fechaProgramacion) { this.fechaProgramacion = fechaProgramacion; }

    public LocalDate getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDate fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }

    public Boolean getCompletada() { return completada; }
    public void setCompletada(Boolean completada) { this.completada = completada; }

    public List<Subitem> getSubitems() { return subitems; }
    public void setSubitems(List<Subitem> subitems) { this.subitems = subitems; }
}
