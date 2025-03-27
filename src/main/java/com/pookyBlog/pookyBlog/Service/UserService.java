package com.pookyBlog.pookyBlog.Service;

import com.pookyBlog.pookyBlog.Dto.Request.SignUpDto;
import com.pookyBlog.pookyBlog.Entity.User;
import com.pookyBlog.pookyBlog.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(SignUpDto signUpDto) {
        if(userRepository.existsByUsername(signUpDto.getUsername())){
            throw new IllegalArgumentException("이미 사용중인 사용자 이름입니다.");
        }

        if(userRepository.existsByEmail(signUpDto.getEmail())){
            throw new IllegalArgumentException("이미 사용중인 이메일 이름입니다.");
        }

        User user = User.builder()
                .username(signUpDto.getUsername())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .nickname(signUpDto.getNickname())
                .email(signUpDto.getEmail())
                .role(signUpDto.getRole())
                .build();

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
