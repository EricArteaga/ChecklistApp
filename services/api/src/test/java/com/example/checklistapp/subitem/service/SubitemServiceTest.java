package com.example.checklistapp.subitem.service;

import com.example.checklistapp.common.exception.ResourceNotFoundException;
import com.example.checklistapp.subitem.dto.CreateSubitemDTO;
import com.example.checklistapp.subitem.dto.SubitemResponseDTO;
import com.example.checklistapp.subitem.dto.UpdateSubitemDTO;
import com.example.checklistapp.subitem.mapper.SubitemMapper;
import com.example.checklistapp.subitem.model.Subitem;
import com.example.checklistapp.subitem.repository.SubitemRepository;
import com.example.checklistapp.task.model.Task;
import com.example.checklistapp.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para SubitemService")
class SubitemServiceTest {

    @Mock
    private SubitemRepository subitemRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SubitemMapper subitemMapper;

    @InjectMocks
    private SubitemService subitemService;

    private Task testTask;
    private Subitem testSubitem;
    private CreateSubitemDTO createSubitemDTO;
    private SubitemResponseDTO subitemResponseDTO;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1);

        testSubitem = new Subitem();
        testSubitem.setId(1);
        testSubitem.setTarea(testTask);
        testSubitem.setDescription("Test subitem");
        testSubitem.setChecked(false);
        testSubitem.setFechaCreacion(LocalDateTime.now());

        createSubitemDTO = new CreateSubitemDTO("Test subitem", false);

        subitemResponseDTO = new SubitemResponseDTO(
            1,
            1,
            "Test subitem",
            false,
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("✅ create() - Happy path")
    void testCreate_ValidSubitem_ReturnsSubitemResponseDTO() {
        // GIVEN
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(subitemMapper.toEntity(createSubitemDTO)).thenReturn(testSubitem);
        when(subitemRepository.save(any(Subitem.class))).thenReturn(testSubitem);
        when(subitemMapper.toResponseDTO(testSubitem)).thenReturn(subitemResponseDTO);

        // WHEN
        SubitemResponseDTO result = subitemService.create(1, createSubitemDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("Test subitem");
        assertThat(result.checked()).isFalse();
        verify(subitemRepository).save(any(Subitem.class));
    }

    @Test
    @DisplayName("❌ create() - Tarea no existe")
    void testCreate_TareaNoExiste_ThrowsResourceNotFoundException() {
        // GIVEN
        when(taskRepository.findById(999)).thenReturn(Optional.empty());

        // WHEN/THEN
        assertThatThrownBy(() -> subitemService.create(999, createSubitemDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Tarea");
    }

    @Test
    @DisplayName("✅ findByIdTarea() - Happy path")
    void testFindByIdTarea_ExistenSubitems_ReturnsList() {
        // GIVEN
        List<Subitem> subitems = Arrays.asList(testSubitem);
        when(taskRepository.existsById(1)).thenReturn(true);
        when(subitemRepository.findByIdTarea(1)).thenReturn(subitems);
        when(subitemMapper.toResponseDTO(testSubitem)).thenReturn(subitemResponseDTO);

        // WHEN
        List<SubitemResponseDTO> result = subitemService.findByIdTarea(1);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Test subitem");
    }

    @Test
    @DisplayName("❌ findByIdTarea() - Tarea no existe")
    void testFindByIdTarea_TareaNoExiste_ThrowsResourceNotFoundException() {
        // GIVEN
        when(taskRepository.existsById(999)).thenReturn(false);

        // WHEN/THEN
        assertThatThrownBy(() -> subitemService.findByIdTarea(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Tarea");
    }

    @Test
    @DisplayName("✅ findById() - Happy path")
    void testFindById_SubitemExiste_ReturnsSubitemResponseDTO() {
        // GIVEN
        when(subitemRepository.findById(1)).thenReturn(Optional.of(testSubitem));
        when(subitemMapper.toResponseDTO(testSubitem)).thenReturn(subitemResponseDTO);

        // WHEN
        SubitemResponseDTO result = subitemService.findById(1);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("❌ findById() - Subitem no existe")
    void testFindById_SubitemNoExiste_ThrowsResourceNotFoundException() {
        // GIVEN
        when(subitemRepository.findById(999)).thenReturn(Optional.empty());

        // WHEN/THEN
        assertThatThrownBy(() -> subitemService.findById(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Subitem");
    }

    @Test
    @DisplayName("✅ update() - Happy path")
    void testUpdate_SubitemValid_ReturnsSubitemResponseDTO() {
        // GIVEN
        UpdateSubitemDTO updateDTO = new UpdateSubitemDTO("Updated description", true);
        when(subitemRepository.findById(1)).thenReturn(Optional.of(testSubitem));
        when(subitemRepository.save(any(Subitem.class))).thenReturn(testSubitem);
        when(subitemMapper.toResponseDTO(testSubitem)).thenReturn(subitemResponseDTO);

        // WHEN
        SubitemResponseDTO result = subitemService.update(1, updateDTO);

        // THEN
        assertThat(result).isNotNull();
        verify(subitemRepository).save(any(Subitem.class));
    }

    @Test
    @DisplayName("❌ delete() - Happy path")
    void testDelete_SubitemExiste_DeletesSubitem() {
        // GIVEN
        when(subitemRepository.existsById(1)).thenReturn(true);
        doNothing().when(subitemRepository).deleteById(1);

        // WHEN
        subitemService.delete(1);

        // THEN
        verify(subitemRepository).deleteById(1);
    }

    @Test
    @DisplayName("❌ delete() - Subitem no existe")
    void testDelete_SubitemNoExiste_ThrowsResourceNotFoundException() {
        // GIVEN
        when(subitemRepository.existsById(999)).thenReturn(false);

        // WHEN/THEN
        assertThatThrownBy(() -> subitemService.delete(999))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Subitem");
    }
}
