package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    @RequestMapping(value = "/createPublic", method = RequestMethod.POST)
    @ResponseBody
    public ChannelResponse createPublic(@ModelAttribute PublicChannelCreateRequest request){
        UUID id = channelService.createPublicChannel(request).getId();
        return channelService.find(id);
    }

    @RequestMapping(value = "/createPrivate", method = RequestMethod.POST)
    @ResponseBody
    public ChannelResponse createPrivate(@ModelAttribute PrivateChannelCreateRequest request){
        UUID id = channelService.createPrivateChannel(request).getId();
        return channelService.find(id);
    }

    @RequestMapping(value = "/update",  method = RequestMethod.POST)
    @ResponseBody
    public ChannelResponse update(@ModelAttribute ChannelUpdateRequest request){
        return channelService.update(request);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(@ModelAttribute UUID channelId){
        channelService.delete(channelId);
        return "channel deleted : " + channelId;
    }

    @RequestMapping(value = "/find",  method = RequestMethod.GET)
    @ResponseBody
    public List<ChannelResponse> find(@ModelAttribute UUID userId){
        return channelService.findAllByUserId(userId);
    }

}
