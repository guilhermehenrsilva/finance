package com.finance.api.domain.tag;

import com.finance.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, String> {
    List<Tag> findByUser(User user);
    Optional<Tag> findByIdAndUser(String id, User user);
    boolean existsByUserAndName(User user, String name);
    List<Tag> findByUserAndIdIn(User user, Collection<String> ids);
}
