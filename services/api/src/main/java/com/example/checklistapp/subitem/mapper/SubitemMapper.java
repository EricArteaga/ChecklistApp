package com.example.checklistapp.subitem.mapper;

import com.example.checklistapp.subitem.dto.CreateSubitemDTO;
import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.subitem.dto.UpdateSubitemDTO;
import com.example.checklistapp.subitem.model.Subitem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SubitemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tarea", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Subitem toEntity(CreateSubitemDTO dto);

    @Mapping(source = "tarea.id", target = "idTarea")
    SubitemResponseDTO toResponseDTO(Subitem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tarea", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Subitem updateEntityFromDTO(UpdateSubitemDTO dto, @MappingTarget Subitem entity);
}
