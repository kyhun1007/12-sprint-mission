package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.UUID;

public class ChannelNotFoundException extends ChannelException {

  public ChannelNotFoundException() {
    super(ErrorCode.CHANNEL_NOT_FOUND);
  }

  public static ChannelNotFoundException withId(UUID id) {
    ChannelNotFoundException ex = new ChannelNotFoundException();
    ex.addDetail("userId", id);
    return ex;
  }
}
