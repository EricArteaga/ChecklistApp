package com.example.checklistapp.type.service;

import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.task.repository.TaskRepository;
import com.example.checklistapp.type.dto.CreateTypeDTO;
import com.example.checklistapp.type.dto.TypeResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.dto.UpdateTypeDTO;
import com.example.checklistapp.type.mapper.TypeMapper;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.type.repository.TypeRepository;
import com.example.checklistapp.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TypeService {

    private final TypeRepository typeRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TypeMapper typeMapper;

    public TypeService(
            TypeRepository typeRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            TypeMapper typeMapper
    ) {
        this.typeRepository = typeRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.typeMapper = typeMapper;
    }

    @Transactional
    public TypeResponseDTO create(CreateTypeDTO dto) {
        if (!userRepository.existsById(dto.idUsuario())) {
            throw new ResourceNotFoundException("Usuario", dto.idUsuario());
        }

        Type type = typeMapper.toEntity(dto);
        Type savedType = typeRepository.save(type);
        return typeMapper.toResponseDTO(savedType);
    }

    @Transactional(readOnly = true)
    public TypeResponseDTO findById(Integer id) {
        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo", id));
        return typeMapper.toResponseDTO(type);
    }

    @Transactional(readOnly = true)
    public List<TypeSummaryDTO> findByIdUsuario(Integer idUsuario) {
        if (!userRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario", idUsuario);
        }
        List<Type> types = typeRepository.findByIdUsuario(idUsuario);
        return types.stream()
                .map(typeMapper::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TypeSummaryDTO> findAll() {
        List<Type> types = typeRepository.findAll();
        return types.stream()
                .map(typeMapper::toSummaryDTO)
                .toList();
    }

    @Transactional
    public TypeResponseDTO update(Integer id, UpdateTypeDTO dto) {
        if (!dto.tieneCambios()) {
            throw new BusinessRuleException("No se proporcionaron cambios para actualizar");
        }

        Type type = typeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo", id));

        typeMapper.updateEntityFromDTO(dto, type);
        Type savedType = typeRepository.save(type);
        return typeMapper.toResponseDTO(savedType);
    }

    @Transactional
    public void delete(Integer id) {
        if (!typeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tipo", id);
        }

        long tareasCount = taskRepository.countByIdTipo(id);
        if (tareasCount > 0) {
            throw new BusinessRuleException(
                    "No se puede eliminar el tipo porque tiene " + tareasCount + " tarea(s) asociada(s)"
            );
        }

        typeRepository.deleteById(id);
    }
}
