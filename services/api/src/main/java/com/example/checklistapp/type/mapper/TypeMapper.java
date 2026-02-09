package com.example.checklistapp.type.mapper;

import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.model.Type;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TypeMapper {
    TypeSummaryDTO toSummaryDTO(Type type);
}
