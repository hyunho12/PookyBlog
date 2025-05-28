package pookyBlog.controller;

import pookyBlog.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserService userService;

    @GetMapping("/auth/signup")
    public String signup() {
        return "/user/user-signup";
    }

    @GetMapping("/auth/login")
    public String login(){
        return "/user/user-login";
    }
}
