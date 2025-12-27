package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.generated.dto.UserDto;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.DefaultUserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Validator validator;

    @Mock
    private EnversRevisionService enversRevisionService;

    @Mock
    private ConstraintViolation<UserDto> constraintViolation;

    private DefaultUserService userService;

    @BeforeEach
    void setUp() {
        userService = new DefaultUserService(userRepository, userMapper, validator, enversRevisionService);
    }

    @Test
    void save_withMissingName_shouldThrowConstraintViolationException() {
        // Given
        UserDto userDto = new UserDto();
        // name is null - should trigger validation

        // Mock validation failure
        when(validator.validate(userDto)).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getMessage()).thenReturn("Name is required");

        // When & Then
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> userService.save(userDto)
        );

        assertFalse(exception.getConstraintViolations().isEmpty());
        verify(validator).validate(userDto);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void save_withEmptyName_shouldThrowConstraintViolationException() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName(""); // Empty string should trigger @NotBlank

        // Mock validation failure
        when(validator.validate(userDto)).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getMessage()).thenReturn("Name is required");

        // When & Then
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> userService.save(userDto)
        );

        assertFalse(exception.getConstraintViolations().isEmpty());
        verify(validator).validate(userDto);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void save_withBlankName_shouldThrowConstraintViolationException() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("   "); // Whitespace should trigger @NotBlank

        // Mock validation failure
        when(validator.validate(userDto)).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getMessage()).thenReturn("Name is required");

        // When & Then
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> userService.save(userDto)
        );

        assertFalse(exception.getConstraintViolations().isEmpty());
        verify(validator).validate(userDto);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void save_withValidName_shouldSucceed() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("Test User");

        User user = new User();
        User savedUser = new User();
        UserDto expectedResult = new UserDto();
        expectedResult.setId(1L);
        expectedResult.setName("Test User");

        // Mock successful validation
        when(validator.validate(userDto)).thenReturn(Set.of());
        when(userMapper.getEntityClass()).thenReturn((Class) User.class);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.map(savedUser)).thenReturn(expectedResult);

        // When
        UserDto result = userService.save(userDto);

        // Then
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        verify(validator).validate(userDto);
        verify(userRepository).save(any(User.class));
        verify(userMapper).update(eq(userDto), any(User.class));
        verify(userMapper).map(savedUser);
    }
}