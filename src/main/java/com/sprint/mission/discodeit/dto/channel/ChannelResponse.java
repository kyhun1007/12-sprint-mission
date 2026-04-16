package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        String description,
        ChannelType type,
        Instant lastTime,
        List<UUID> users
) {
//    public static ChannelResponse from(Channel channel) {
//        return new ChannelResponse(
//                channel.getId(),
//                channel.getName(),
//                channel.getDescription(),
//                channel.getType(),
//                channel.getLastTime(),
//                channel.getUsers().stream().map(user -> user.getId()).toList()
//        );
//    }
}
