package com.example.checklistapp.user.mapper;

import com.example.checklistapp.user.dto.CreateUserDTO;
import com.example.checklistapp.user.dto.UpdateUserDTO;
import com.example.checklistapp.user.dto.UserResponseDTO;
import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    User toEntity(CreateUserDTO dto);
    UserResponseDTO toResponseDTO(User user);
    UserSummaryDTO toSummaryDTO(User user);
    void updateEntityFromDTO(UpdateUserDTO dto, @MappingTarget User user);
}
