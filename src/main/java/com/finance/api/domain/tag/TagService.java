package com.finance.api.domain.tag;

import com.finance.api.domain.tag.dto.TagRequestDTO;
import com.finance.api.domain.tag.dto.TagResponseDTO;
import com.finance.api.domain.user.User;
import com.finance.api.infra.exception.BusinessException;
import com.finance.api.infra.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Transactional
    public TagResponseDTO createTag(User user, TagRequestDTO dto) {
        if (tagRepository.existsByUserAndName(user, dto.name().trim())) {
            throw new BusinessException("Já existe uma tag com este nome");
        }

        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(dto.name().trim());
        tag.setColor(dto.color());

        Tag saved = tagRepository.save(tag);
        return new TagResponseDTO(saved);
    }

    public List<TagResponseDTO> listTags(User user) {
        return tagRepository.findByUser(user).stream()
                .map(TagResponseDTO::new)
                .collect(Collectors.toList());
    }

    public TagResponseDTO getTagById(User user, String id) {
        Tag tag = findTagEntity(user, id);
        return new TagResponseDTO(tag);
    }

    @Transactional
    public TagResponseDTO updateTag(User user, String id, TagRequestDTO dto) {
        Tag tag = findTagEntity(user, id);

        String trimmedName = dto.name().trim();
        if (!tag.getName().equalsIgnoreCase(trimmedName) && tagRepository.existsByUserAndName(user, trimmedName)) {
            throw new BusinessException("Já existe uma tag com este nome");
        }

        tag.setName(trimmedName);
        tag.setColor(dto.color());

        Tag updated = tagRepository.save(tag);
        return new TagResponseDTO(updated);
    }

    @Transactional
    public void deleteTag(User user, String id) {
        Tag tag = findTagEntity(user, id);
        tagRepository.delete(tag);
    }

    public Tag findTagEntity(User user, String id) {
        return tagRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tag não encontrada"));
    }

    public List<Tag> findTagsByUserAndIds(User user, Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return tagRepository.findByUserAndIdIn(user, ids);
    }
}
