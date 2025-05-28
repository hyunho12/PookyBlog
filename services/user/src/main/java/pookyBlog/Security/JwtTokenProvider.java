package pookyBlog.Security;

import pookyBlog.Entity.User;
import pookyBlog.Dto.JwtToken;
import pookyBlog.Repository.UserRepository;
import pookyBlog.Security.auth.CustomUserDetails;
import pookyBlog.Security.auth.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    private final Key key;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailsService customUserDetailsService, UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
    }

    // 사용자가 인증후 AccessToken, RefreshToken생성
    public JwtToken generateToken(Authentication authentication){
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // AccessToken 생성
        Date accessTokenExpiresln = new Date(now + 86400000); // 24시간후
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName()) // 사용자 식별정보
                .claim("auth", authorities) // 권한 정보 포함
                .setExpiration(accessTokenExpiresln) // 만료시간 설정
                .signWith(key, SignatureAlgorithm.HS256) // 서명 HS256알고리즘
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // JWT에서 사용자 인증 정보를 추출
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if(claims.get("auth") == null){
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User user = userRepository.findByUsername(claims.getSubject())
                .orElseThrow(()-> new UsernameNotFoundException("User not Found:" + claims.getSubject()));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(customUserDetails,"",authorities);
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }
        catch (SecurityException | MalformedJwtException e){
            log.info("Invalid JWT Token: {}", e.getMessage());
        }
        catch (ExpiredJwtException e){
            log.info("Expired JWT Token: {}", e.getMessage());
        }
        catch (UnsupportedJwtException e){
            log.info("Unsupported JWT Token: {}", e.getMessage());
        }
        catch (IllegalArgumentException e){
            log.info("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // Claim(토큰에 담긴 데이터)를 추출
    private Claims parseClaims(String accessToken){
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        }
        catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }
}
