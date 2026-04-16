package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public String create(@ModelAttribute UserCreateRequest request) {
        // request.profileImageId() 에 프론트가 보내준 UUID가 담겨 들어옴
        User user = userService.create(request);
        return "user created : " + user.getId();
    }

    @RequestMapping(value = "/update",  method = RequestMethod.POST)
    @ResponseBody
    public String update(@ModelAttribute UserUpdateRequest request) {
        UserResponse user = userService.update(request);
        return "user updated : " + user.id();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(@RequestParam UUID id) {
        userService.delete(id);
        return "user deleted : " + id;
    }

    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    @ResponseBody
    public List<UserResponse> userList() {
        return userService.findAll();
    }

    @RequestMapping(value = "/userStatusUpdate", method = RequestMethod.POST)
    @ResponseBody
    public String userStatusUpdate(@RequestParam UUID id) {
        userStatusService.updateByUserId(id);
        return "user status updated : " + id;
    }

}
