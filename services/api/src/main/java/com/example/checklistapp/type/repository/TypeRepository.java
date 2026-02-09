package com.example.checklistapp.type.repository;

import com.example.checklistapp.type.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeRepository extends JpaRepository<Type, Integer> {
    List<Type> findByIdUsuario(Integer idUsuario);
}
