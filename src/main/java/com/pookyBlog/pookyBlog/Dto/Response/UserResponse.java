package com.pookyBlog.pookyBlog.Dto.Response;

import com.pookyBlog.pookyBlog.Entity.Role;
import com.pookyBlog.pookyBlog.Entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private Role role;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
