package com.example.checklistapp.user.service;

import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.task.repository.TaskRepository;
import com.example.checklistapp.user.dto.CreateUserDTO;
import com.example.checklistapp.user.dto.UpdateUserDTO;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.mapper.UserMapper;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            TaskRepository taskRepository,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDTO create(CreateUserDTO dto) {
        if (userRepository.existsByCorreo(dto.correo())) {
            throw new BusinessRuleException("Ya existe un usuario con el correo: " + dto.correo());
        }

        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toSummaryDTO)
                .toList();
    }

    @Transactional
    public UserResponseDTO update(Integer id, UpdateUserDTO dto) {
        if (!dto.tieneCambios()) {
            throw new BusinessRuleException("No se proporcionaron cambios para actualizar");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        if (dto.correo() != null && !dto.correo().equals(user.getCorreo())) {
            if (userRepository.existsByCorreo(dto.correo())) {
                throw new BusinessRuleException("Ya existe un usuario con el correo: " + dto.correo());
            }
        }

        userMapper.updateEntityFromDTO(dto, user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Transactional
    public void delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", id);
        }

        long tareasCount = taskRepository.countByIdUsuario(id);
        if (tareasCount > 0) {
            throw new BusinessRuleException(
                    "No se puede eliminar el usuario porque tiene " + tareasCount + " tarea(s) asociada(s)"
            );
        }

        userRepository.deleteById(id);
    }
}
