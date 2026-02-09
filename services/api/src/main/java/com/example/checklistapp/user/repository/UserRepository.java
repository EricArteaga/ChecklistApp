package com.example.checklistapp.user.repository;

import com.example.checklistapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
