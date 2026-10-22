package com.finance.api.domain.user;

import com.finance.api.domain.user.dto.ChangePasswordRequestDTO;
import com.finance.api.domain.user.dto.UpdateProfileRequestDTO;
import com.finance.api.domain.user.dto.UserProfileResponseDTO;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserProfileResponseDTO getProfile(User user) {
        User freshUser = findUserEntity(user.getId());
        return new UserProfileResponseDTO(freshUser);
    }

    @Transactional
    public UserProfileResponseDTO updateProfile(User user, UpdateProfileRequestDTO dto) {
        User freshUser = findUserEntity(user.getId());
        freshUser.setName(dto.name().trim());

        if (dto.mainCurrency() != null && !dto.mainCurrency().isBlank()) {
            freshUser.setMainCurrency(dto.mainCurrency().trim().toUpperCase());
        }

        User saved = userRepository.save(freshUser);
        return new UserProfileResponseDTO(saved);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequestDTO dto) {
        User freshUser = findUserEntity(user.getId());

        if (!passwordEncoder.matches(dto.currentPassword(), freshUser.getPassword())) {
            throw new BusinessException("A senha atual informada está incorreta");
        }

        freshUser.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(freshUser);
    }

    private User findUserEntity(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
