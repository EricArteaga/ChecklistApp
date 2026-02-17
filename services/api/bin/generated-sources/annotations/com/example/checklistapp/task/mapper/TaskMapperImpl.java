package com.example.checklistapp.task.mapper;

import com.example.checklistapp.task.dto.CreateTaskDTO;
import com.example.checklistapp.task.dto.TaskResponseDTO;
import com.example.checklistapp.task.dto.TaskSummaryDTO;
import com.example.checklistapp.task.dto.UpdateTaskDTO;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.mapper.TypeMapper;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.mapper.UserMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-17T13:47:37+0100",
    comments = "version: 1.6.0.Beta1, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TypeMapper typeMapper;

    @Override
    public Task toEntity(CreateTaskDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Task task = new Task();

        task.setNombre( dto.nombre() );
        task.setDescripcion( dto.descripcion() );
        task.setIdUsuario( dto.idUsuario() );
        task.setIdTipo( dto.idTipo() );
        task.setFechaProgramacion( dto.fechaProgramacion() );
        task.setFechaRealizacion( dto.fechaRealizacion() );
        task.setCompletada( dto.completada() );

        return task;
    }

    @Override
    public TaskResponseDTO toResponseDTO(Task task) {
        if ( task == null ) {
            return null;
        }

        UserSummaryDTO usuario = null;
        TypeSummaryDTO tipo = null;
        Integer id = null;
        String nombre = null;
        String descripcion = null;
        LocalDateTime fechaCreacion = null;
        LocalDate fechaProgramacion = null;
        LocalDate fechaRealizacion = null;
        Boolean completada = null;

        usuario = userMapper.toSummaryDTO( task.getUsuario() );
        tipo = typeMapper.toSummaryDTO( task.getTipo() );
        id = task.getId();
        nombre = task.getNombre();
        descripcion = task.getDescripcion();
        fechaCreacion = task.getFechaCreacion();
        fechaProgramacion = task.getFechaProgramacion();
        fechaRealizacion = task.getFechaRealizacion();
        completada = task.getCompletada();

        TaskResponseDTO taskResponseDTO = new TaskResponseDTO( id, nombre, descripcion, fechaCreacion, fechaProgramacion, fechaRealizacion, completada, usuario, tipo );

        return taskResponseDTO;
    }

    @Override
    public TaskSummaryDTO toSummaryDTO(Task task) {
        if ( task == null ) {
            return null;
        }

        TypeSummaryDTO tipo = null;
        Integer idUsuario = null;
        Integer id = null;
        String nombre = null;
        LocalDateTime fechaCreacion = null;
        LocalDate fechaProgramacion = null;
        LocalDate fechaRealizacion = null;
        Boolean completada = null;

        tipo = typeMapper.toSummaryDTO( task.getTipo() );
        idUsuario = task.getIdUsuario();
        id = task.getId();
        nombre = task.getNombre();
        fechaCreacion = task.getFechaCreacion();
        fechaProgramacion = task.getFechaProgramacion();
        fechaRealizacion = task.getFechaRealizacion();
        completada = task.getCompletada();

        TaskSummaryDTO taskSummaryDTO = new TaskSummaryDTO( id, nombre, fechaCreacion, fechaProgramacion, fechaRealizacion, completada, idUsuario, tipo );

        return taskSummaryDTO;
    }

    @Override
    public List<TaskSummaryDTO> toSummaryDTOList(List<Task> tasks) {
        if ( tasks == null ) {
            return null;
        }

        List<TaskSummaryDTO> list = new ArrayList<TaskSummaryDTO>( tasks.size() );
        for ( Task task : tasks ) {
            list.add( toSummaryDTO( task ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDTO(UpdateTaskDTO dto, Task entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.nombre() != null ) {
            entity.setNombre( dto.nombre() );
        }
        if ( dto.descripcion() != null ) {
            entity.setDescripcion( dto.descripcion() );
        }
        if ( dto.idTipo() != null ) {
            entity.setIdTipo( dto.idTipo() );
        }
        if ( dto.fechaProgramacion() != null ) {
            entity.setFechaProgramacion( dto.fechaProgramacion() );
        }
        if ( dto.fechaRealizacion() != null ) {
            entity.setFechaRealizacion( dto.fechaRealizacion() );
        }
        if ( dto.completada() != null ) {
            entity.setCompletada( dto.completada() );
        }
    }
}
