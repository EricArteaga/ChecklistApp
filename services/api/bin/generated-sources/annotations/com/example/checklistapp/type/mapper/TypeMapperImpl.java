package com.example.checklistapp.type.mapper;

import com.example.checklistapp.type.dto.CreateTypeDTO;
import com.example.checklistapp.type.dto.TypeResponseDTO;
import com.example.checklistapp.type.dto.TypeSummaryDTO;
import com.example.checklistapp.type.dto.UpdateTypeDTO;
import com.example.checklistapp.type.model.Type;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-17T13:47:37+0100",
    comments = "version: 1.6.0.Beta1, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class TypeMapperImpl implements TypeMapper {

    @Override
    public Type toEntity(CreateTypeDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Type type = new Type();

        type.setNombre( dto.nombre() );
        type.setColor( dto.color() );
        type.setIdUsuario( dto.idUsuario() );

        return type;
    }

    @Override
    public TypeResponseDTO toResponseDTO(Type type) {
        if ( type == null ) {
            return null;
        }

        Integer id = null;
        Integer idUsuario = null;
        String nombre = null;
        String color = null;

        id = type.getId();
        idUsuario = type.getIdUsuario();
        nombre = type.getNombre();
        color = type.getColor();

        TypeResponseDTO typeResponseDTO = new TypeResponseDTO( id, idUsuario, nombre, color );

        return typeResponseDTO;
    }

    @Override
    public TypeSummaryDTO toSummaryDTO(Type type) {
        if ( type == null ) {
            return null;
        }

        Integer id = null;
        String nombre = null;
        String color = null;

        id = type.getId();
        nombre = type.getNombre();
        color = type.getColor();

        TypeSummaryDTO typeSummaryDTO = new TypeSummaryDTO( id, nombre, color );

        return typeSummaryDTO;
    }

    @Override
    public void updateEntityFromDTO(UpdateTypeDTO dto, Type type) {
        if ( dto == null ) {
            return;
        }

        if ( dto.nombre() != null ) {
            type.setNombre( dto.nombre() );
        }
        if ( dto.color() != null ) {
            type.setColor( dto.color() );
        }
    }
}
