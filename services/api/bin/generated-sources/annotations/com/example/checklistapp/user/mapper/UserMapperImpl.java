package com.example.checklistapp.user.mapper;

import com.example.checklistapp.user.dto.CreateUserDTO;
import com.example.checklistapp.user.dto.UpdateUserDTO;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.model.User;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-17T13:47:37+0100",
    comments = "version: 1.6.0.Beta1, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(CreateUserDTO dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setCorreo( dto.correo() );
        user.setNombre( dto.nombre() );

        return user;
    }

    @Override
    public UserResponseDTO toResponseDTO(User user) {
        if ( user == null ) {
            return null;
        }

        Integer id = null;
        String correo = null;
        String nombre = null;
        LocalDateTime fechaCreacion = null;

        id = user.getId();
        correo = user.getCorreo();
        nombre = user.getNombre();
        fechaCreacion = user.getFechaCreacion();

        UserResponseDTO userResponseDTO = new UserResponseDTO( id, correo, nombre, fechaCreacion );

        return userResponseDTO;
    }

    @Override
    public UserSummaryDTO toSummaryDTO(User user) {
        if ( user == null ) {
            return null;
        }

        Integer id = null;
        String nombre = null;
        String correo = null;

        id = user.getId();
        nombre = user.getNombre();
        correo = user.getCorreo();

        UserSummaryDTO userSummaryDTO = new UserSummaryDTO( id, nombre, correo );

        return userSummaryDTO;
    }

    @Override
    public void updateEntityFromDTO(UpdateUserDTO dto, User user) {
        if ( dto == null ) {
            return;
        }

        if ( dto.correo() != null ) {
            user.setCorreo( dto.correo() );
        }
        if ( dto.nombre() != null ) {
            user.setNombre( dto.nombre() );
        }
    }
}
