package com.example.checklistapp.subitem.repository;

import com.example.checklistapp.subitem.model.Subitem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubitemRepository extends JpaRepository<Subitem, Integer> {

    @Query("SELECT s FROM Subitem s WHERE s.tarea.id = :idTarea")
    List<Subitem> findByIdTarea(@Param("idTarea") Integer idTarea);

    @Modifying
    @Query("DELETE FROM Subitem s WHERE s.tarea.id = :idTarea")
    void deleteByIdTarea(@Param("idTarea") Integer idTarea);
}
