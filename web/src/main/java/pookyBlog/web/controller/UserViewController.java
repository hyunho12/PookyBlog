package pookyBlog.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class UserViewController {
    @GetMapping("/signup")
    public String signupPage() {
        return "user/user-signup";
    }

    @GetMapping("/login")
    public String loginPage(){
        return "user/user-login";
    }
}
