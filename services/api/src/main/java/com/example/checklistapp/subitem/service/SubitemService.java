package com.example.checklistapp.subitem.service;

import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.subitem.dto.CreateSubitemDTO;
import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.subitem.dto.UpdateSubitemDTO;
import com.example.checklistapp.subitem.mapper.SubitemMapper;
import com.example.checklistapp.subitem.model.Subitem;
import com.example.checklistapp.subitem.repository.SubitemRepository;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubitemService {

    private final SubitemRepository subitemRepository;
    private final TaskRepository taskRepository;
    private final SubitemMapper subitemMapper;

    public SubitemService(
            SubitemRepository subitemRepository,
            TaskRepository taskRepository,
            SubitemMapper subitemMapper
    ) {
        this.subitemRepository = subitemRepository;
        this.taskRepository = taskRepository;
        this.subitemMapper = subitemMapper;
    }

    @Transactional
    public SubitemResponseDTO create(Integer idTarea, CreateSubitemDTO dto) {
        Task tarea = taskRepository.findById(idTarea)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", idTarea));

        Subitem subitem = subitemMapper.toEntity(dto);
        subitem.setTarea(tarea);

        Subitem savedSubitem = subitemRepository.save(subitem);
        return subitemMapper.toResponseDTO(savedSubitem);
    }

    @Transactional(readOnly = true)
    public List<SubitemResponseDTO> findByIdTarea(Integer idTarea) {
        if (!taskRepository.existsById(idTarea)) {
            throw new ResourceNotFoundException("Tarea", idTarea);
        }

        List<Subitem> subitems = subitemRepository.findByIdTarea(idTarea);
        return subitems.stream()
                .map(subitemMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubitemResponseDTO findById(Integer id) {
        Subitem subitem = subitemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subitem", id));
        return subitemMapper.toResponseDTO(subitem);
    }

    @Transactional
    public SubitemResponseDTO update(Integer id, UpdateSubitemDTO dto) {
        Subitem subitem = subitemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subitem", id));

        if (!dto.tieneCambios()) {
            return subitemMapper.toResponseDTO(subitem);
        }

        subitemMapper.updateEntityFromDTO(dto, subitem);
        Subitem updatedSubitem = subitemRepository.save(subitem);
        return subitemMapper.toResponseDTO(updatedSubitem);
    }

    @Transactional
    public void delete(Integer id) {
        if (!subitemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subitem", id);
        }
        subitemRepository.deleteById(id);
    }

    @Transactional
    public void deleteByIdTarea(Integer idTarea) {
        if (!taskRepository.existsById(idTarea)) {
            throw new ResourceNotFoundException("Tarea", idTarea);
        }
        subitemRepository.deleteByIdTarea(idTarea);
    }
}
