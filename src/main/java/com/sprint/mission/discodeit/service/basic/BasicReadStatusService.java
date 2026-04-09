package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service("readStatusService")
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ReadStatusResponse create(ReadStatusCreateRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new IllegalArgumentException("User with id " + request.userId() + " not found (read status -> create)");
        }
        if (!channelRepository.existsById(request.channelId())) {
            throw new IllegalArgumentException("Channel with id " + request.channelId() + " not found (read status -> create)");
        }

        if (readStatusRepository.isExists(request.userId(), request.channelId())) {
            throw new IllegalArgumentException("Read status already exists for user " + request.userId() + " and channel " + request.channelId());
        }

        ReadStatus readStatus = readStatusRepository.save(new ReadStatus(request.userId(), request.channelId()));

        return ReadStatusResponse.from(readStatus);
    }

    @Override
    public ReadStatusResponse find(UUID id) {
        return readStatusRepository.findById(id)
                .map(ReadStatusResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Read status with id " + id + " not found (read status -> find)"));
    }

    @Override
    public List<ReadStatusResponse> findAllByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User with id " + userId + " not found (read status -> find all by user id)");
        }

        return readStatusRepository.findAllByUserId(userId).stream()
                .map(ReadStatusResponse::from)
                .toList();
    }

    @Override
    public ReadStatusResponse update(ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("Read status with id " + request.id() + " not found (read status -> update)"));

        readStatus.update(request.userId(), request.channelId());
        readStatusRepository.save(readStatus);

        return find(readStatus.getId());
    }

    @Override
    public void delete(UUID id) {
        if (!readStatusRepository.isExists(id)) {
            throw new IllegalArgumentException("Read status with id " + id + " not found (read status -> delete)");
        }

        readStatusRepository.delete(id);
    }
}
