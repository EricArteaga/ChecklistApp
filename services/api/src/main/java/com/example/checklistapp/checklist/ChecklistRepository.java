package com.example.checklistapp.checklist;

import org.springframework.stereotype.Repository;

@Repository // marcador para componentes que acceden a la capa de persistencia
public interface ChecklistRepository {
    // TODO: Implementar como interfaz JPA (p.ej. extends JpaRepository<Checklist, Long>)
    // aquí se definirían consultas CRUD y métodos personalizados para checklists/items
}
