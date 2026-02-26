package com.example.checklistapp.task.service;

import com.example.checklistapp.common.exception.BusinessRuleException;
import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.task.dto.CreateTaskDTO;
import com.example.checklistapp.task.dto.TaskResponseDTO;
import com.example.checklistapp.task.dto.TaskSummaryDTO;
import com.example.checklistapp.task.dto.UpdateTaskDTO;
import com.example.checklistapp.task.mapper.TaskMapper;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.task.repository.TaskRepository;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.type.repository.TypeRepository;
import com.example.checklistapp.user.model.User;
import com.example.checklistapp.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TypeRepository typeRepository;
    private final TaskMapper taskMapper;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            TypeRepository typeRepository,
            TaskMapper taskMapper
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.typeRepository = typeRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponseDTO create(CreateTaskDTO dto) {
        // Soporte para tareas anónimas (sin usuario)
        User usuario = null;
        if (dto.idUsuario() != null) {
            usuario = userRepository.findById(dto.idUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idUsuario()));
        }

        Type tipo = null;
        if (dto.idTipo() != null) {
            tipo = typeRepository.findById(dto.idTipo())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo", dto.idTipo()));
        }

        Task task = taskMapper.toEntity(dto);
        task.setUsuario(usuario);
        task.setTipo(tipo);

        if (dto.estaMarcadaComoCompletada() && !dto.tieneFechaRealizacionExplicita()) {
            task.setFechaRealizacion(LocalDate.now());
        }

        validarFechaRealizacion(task);

        Task savedTask = taskRepository.save(task);
        Task taskWithRelations = taskRepository.findByIdWithUsuarioAndTipo(savedTask.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", savedTask.getId()));

        return taskMapper.toResponseDTO(taskWithRelations);
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO findById(Integer id) {
        Task task = taskRepository.findByIdWithUsuarioAndTipo(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
        return taskMapper.toResponseDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryDTO> findByIdUsuario(Integer idUsuario) {
        if (!userRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario", idUsuario);
        }
        List<Task> tasks = taskRepository.findByIdUsuarioWithUsuarioAndTipo(idUsuario);
        return taskMapper.toSummaryDTOList(tasks);
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryDTO> findAll() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryDTO> findPendingTasks(Integer idUsuario) {
        if (!userRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario", idUsuario);
        }
        List<Task> tasks = taskRepository.findPendingTasksWithUsuarioAndTipo(idUsuario);
        return taskMapper.toSummaryDTOList(tasks);
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryDTO> findCompletedTasks(Integer idUsuario) {
        if (!userRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario", idUsuario);
        }
        List<Task> tasks = taskRepository.findByIdUsuarioAndCompletada(idUsuario, true);
        return tasks.stream()
                .map(taskMapper::toSummaryDTO)
                .toList();
    }

    @Transactional
    public TaskResponseDTO update(Integer id, UpdateTaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));

        if (dto.seMarcaComoCompletada() && dto.fechaRealizacion() == null) {
            task.setFechaRealizacion(LocalDate.now());
        }

        taskMapper.updateEntityFromDTO(dto, task);

        if (dto.idTipo() != null) {
            if (dto.idTipo() == null) {
                task.setTipo(null);
            } else {
                Type tipo = typeRepository.findById(dto.idTipo())
                        .orElseThrow(() -> new ResourceNotFoundException("Tipo", dto.idTipo()));
                task.setTipo(tipo);
            }
        }

        validarFechaRealizacion(task);

        Task savedTask = taskRepository.save(task);
        Task taskWithRelations = taskRepository.findByIdWithUsuarioAndTipo(savedTask.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", savedTask.getId()));

        return taskMapper.toResponseDTO(taskWithRelations);
    }

    @Transactional
    public void delete(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea", id);
        }
        taskRepository.deleteById(id);
    }

    private void validarFechaRealizacion(Task task) {
        LocalDate fechaRealizacion = task.getFechaRealizacion();
        LocalDate fechaProgramacion = task.getFechaProgramacion();

        if (fechaRealizacion != null && fechaProgramacion != null) {
            if (fechaRealizacion.isBefore(fechaProgramacion)) {
                throw new BusinessRuleException(
                        "La fecha de realización no puede ser anterior a la fecha de programación"
                );
            }
        }
    }
}
