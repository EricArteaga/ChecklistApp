package com.example.checklistapp.user.mapper;

import com.example.checklistapp.user.dto.UserSummaryDTO;
import com.example.checklistapp.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserSummaryDTO toSummaryDTO(User user);
}
