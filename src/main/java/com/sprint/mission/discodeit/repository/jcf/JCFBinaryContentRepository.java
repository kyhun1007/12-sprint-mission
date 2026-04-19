package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository("jcfBinaryContentRepository")
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final Map<UUID, BinaryContent> data;

    public JCFBinaryContentRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        this.data.put(binaryContent.getId(), binaryContent);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return this.data.values().stream()
                .filter(binaryContent -> binaryContent.getId().equals(id)).findFirst();
    }

    @Override
    public void delete(UUID id) {
        this.data.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return this.data.values().stream()
                .anyMatch(binaryContent -> binaryContent.getId().equals(id));
    }
}
