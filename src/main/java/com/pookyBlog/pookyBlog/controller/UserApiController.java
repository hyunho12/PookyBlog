package com.pookyBlog.pookyBlog.controller;

import com.pookyBlog.pookyBlog.Dto.Request.LoginDto;
import com.pookyBlog.pookyBlog.Dto.Request.SignUpDto;
import com.pookyBlog.pookyBlog.Dto.Response.LoginResponse;
import com.pookyBlog.pookyBlog.Security.JwtAuthenticationFilter;
import com.pookyBlog.pookyBlog.Security.JwtTokenProvider;
import com.pookyBlog.pookyBlog.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserApiController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpDto signUpDto){
        try {
            userService.signUp(signUpDto);
            return ResponseEntity.ok().body(Collections.singletonMap("message","회원가입 성공"));
        }
        catch (IllegalArgumentException e){
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletResponse response){
        try{
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateToken(authentication).getAccessToken();
            String refreshToekn = jwtTokenProvider.generateToken(authentication).getRefreshToken();

            log.info("✅ 로그인 성공: {}", authentication.getName());
            log.info("✅ SecurityContext에 저장된 사용자: {}", SecurityContextHolder.getContext().getAuthentication().getName());

            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken",accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(60*60) // 1시간
                    .build();
            response.addHeader("Set-Cookie", jwtCookie.toString());

            //return ResponseEntity.ok("로그인 성공!");
            return ResponseEntity.ok().body(Collections.singletonMap("message","로그인 성공!"));
        } catch (Exception e){
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error","로그인 실패: 아이디 또는 비밀번호를 확인하세요."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        SecurityContextHolder.clearContext();

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken","")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", jwtCookie.toString());
        log.info("✅ 로그아웃 완료: JWT 삭제 및 SecurityContext 초기화");
        return ResponseEntity.ok(Collections.singletonMap("message", "로그아웃 성공"));
    }
}

