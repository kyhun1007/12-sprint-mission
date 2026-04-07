package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository {
    BinaryContent save(BinaryContent binaryContent);
    BinaryContent findById(UUID id);
    BinaryContent findById(UUID userId, UUID messageId);
    List<BinaryContent> findAll();
    List<BinaryContent> findAllByUserId(UUID userId);
    List<BinaryContent> findAllByMessageId(UUID messageId);
    void delete(UUID id);
    void delete(UUID userId, UUID messageId);
    boolean isExists(UUID userId, UUID messageId);
    boolean isExists(UUID id);
}
