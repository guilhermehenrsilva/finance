package com.finance.api.domain.user;

import com.finance.api.domain.user.dto.ChangePasswordRequestDTO;
import com.finance.api.domain.user.dto.UpdateProfileRequestDTO;
import com.finance.api.domain.user.dto.UserProfileResponseDTO;
import com.finance.api.infra.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldUpdateUserProfile() {
        User user = new User("Old Name", "user@test.com", "pass", "BRL");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User authUser = new User();
        authUser.setName("Old Name");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(authUser, "user-1");
        } catch (Exception ignored) {}

        UpdateProfileRequestDTO dto = new UpdateProfileRequestDTO("New Name", "USD");
        UserProfileResponseDTO response = userService.updateProfile(authUser, dto);

        assertEquals("New Name", response.name());
        assertEquals("USD", response.mainCurrency());
    }

    @Test
    void shouldChangePasswordWhenCurrentMatches() {
        User user = new User("User", "user@test.com", "encoded-old", "BRL");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-123", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new-secret-456")).thenReturn("encoded-new");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User authUser = new User();
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(authUser, "user-1");
        } catch (Exception ignored) {}

        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO("current-123", "new-secret-456");
        userService.changePassword(authUser, dto);

        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectPasswordChangeWhenCurrentDoesNotMatch() {
        User user = new User("User", "user@test.com", "encoded-old", "BRL");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-old")).thenReturn(false);

        User authUser = new User();
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(authUser, "user-1");
        } catch (Exception ignored) {}

        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO("wrong-password", "new-secret-456");

        assertThrows(BusinessException.class, () -> userService.changePassword(authUser, dto));
    }
}
