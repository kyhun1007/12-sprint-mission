package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService{
    Channel createPublicChannel(PublicChannelCreateRequest request);
    Channel createPrivateChannel(PrivateChannelCreateRequest request);
    ChannelResponse find(UUID channelId);
    List<ChannelResponse> findAllByUserId(UUID userId);
    ChannelResponse update(ChannelUpdateRequest request);
    void delete(UUID channelId);
}
