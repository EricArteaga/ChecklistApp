package com.example.checklistapp.task.mapper;

import com.example.checklistapp.task.dto.*;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.type.model.Type;
import com.example.checklistapp.user.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {com.example.checklistapp.user.mapper.UserMapper.class, com.example.checklistapp.type.mapper.TypeMapper.class}
)
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    Task toEntity(CreateTaskDTO dto);

    @Mapping(source = "usuario", target = "usuario")
    @Mapping(source = "tipo", target = "tipo")
    TaskResponseDTO toResponseDTO(Task task);

    @Mapping(source = "tipo", target = "tipo")
    @Mapping(target = "idUsuario", source = "idUsuario")
    TaskSummaryDTO toSummaryDTO(Task task);

    List<TaskSummaryDTO> toSummaryDTOList(List<Task> tasks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateTaskDTO dto, @MappingTarget Task entity);
}
