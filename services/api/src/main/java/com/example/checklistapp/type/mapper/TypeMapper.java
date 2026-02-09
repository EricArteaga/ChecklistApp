package com.example.checklistapp.type.mapper;

import com.example.checklistapp.type.dto.CreateTypeDTO;
import com.example.checklistapp.type.dto.TypeResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.dto.UpdateTypeDTO;
import com.example.checklistapp.type.model.Type;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TypeMapper {
    Type toEntity(CreateTypeDTO dto);
    TypeResponseDTO toResponseDTO(Type type);
    TypeSummaryDTO toSummaryDTO(Type type);
    void updateEntityFromDTO(UpdateTypeDTO dto, @MappingTarget Type type);
}
