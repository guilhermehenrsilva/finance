package com.finance.api.domain.tag;

import com.finance.api.domain.tag.dto.TagRequestDTO;
import com.finance.api.domain.tag.dto.TagResponseDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void shouldCreateTag() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        when(tagRepository.existsByUserAndName(user, "Viagem")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TagRequestDTO dto = new TagRequestDTO("Viagem", "#3B82F6");
        TagResponseDTO response = tagService.createTag(user, dto);

        assertEquals("Viagem", response.name());
        assertEquals("#3B82F6", response.color());
    }

    @Test
    void shouldRejectDuplicateTagName() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        when(tagRepository.existsByUserAndName(user, "Viagem")).thenReturn(true);

        TagRequestDTO dto = new TagRequestDTO("Viagem", "#3B82F6");

        assertThrows(BusinessException.class, () -> tagService.createTag(user, dto));
    }

    @Test
    void shouldDeleteTag() {
        User user = new User("User", "user@test.com", "pass", "BRL");
        Tag tag = new Tag();
        tag.setId("tag-1");
        tag.setUser(user);

        when(tagRepository.findByIdAndUser("tag-1", user)).thenReturn(Optional.of(tag));

        tagService.deleteTag(user, "tag-1");

        verify(tagRepository).delete(tag);
    }
}
