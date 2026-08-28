package pookyBlog.comment.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pookyBlog.comment.Service.CommentService;
import pookyBlog.comment.controller.CommentApiController;
import pookyBlog.common.jwt.JwtAuthenticationFilter;
import pookyBlog.common.jwt.JwtTokenProvider;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentApiController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CommentApiController.class})
class SecurityConfigTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    static class TestApplication {}

    @Autowired MockMvc mockMvc;
    @MockitoBean CommentService commentService;
    @MockitoBean JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void validToken() {
        when(jwtTokenProvider.validateToken("valid-jwt")).thenReturn(true);
        when(jwtTokenProvider.getAuthentication("valid-jwt"))
                .thenReturn(new UsernamePasswordAuthenticationToken("user", null, List.of()));
    }

    @Test void validBearerIsAccepted() throws Exception {
        mockMvc.perform(get("/api/posts/1").header("Authorization", "Bearer valid-jwt"))
                .andExpect(status().isOk());
    }
    @Test void missingBearerIsRejected() throws Exception {
        mockMvc.perform(get("/api/posts/1")).andExpect(status().isForbidden());
    }
    @Test void invalidBearerIsRejected() throws Exception {
        mockMvc.perform(get("/api/posts/1").header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden());
    }
}
