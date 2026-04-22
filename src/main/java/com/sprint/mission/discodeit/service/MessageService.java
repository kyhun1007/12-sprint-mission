package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto create(MessageCreateRequest request);
    Message find(UUID messageId);
    List<MessageDto> findAllByChannelId(UUID channelId);
    MessageDto update(MessageUpdateRequest request);
    void delete(UUID messageId);
    int totalMessageNumber();
}
