package pookyBlog.post.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pookyBlog.common.jwt.JwtAuthenticationFilter;
import pookyBlog.common.jwt.JwtTokenProvider;
import pookyBlog.post.Service.PostService;
import pookyBlog.post.controller.PostApiController;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostApiController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, PostApiController.class})
class SecurityConfigTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void authenticateBearerToken() {
        when(jwtTokenProvider.validateToken("valid-jwt")).thenReturn(true);
        when(jwtTokenProvider.getAuthentication("valid-jwt")).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    void authenticatedGetSucceeds() throws Exception {
        mockMvc.perform(get("/posts").header("Authorization", "Bearer valid-jwt"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedPostSucceedsWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .header("Authorization", "Bearer valid-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\",\"writer\":\"writer\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedPostIsRejected() throws Exception {
        mockMvc.perform(post("/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\",\"writer\":\"writer\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedPatchAndDeleteSucceedWithoutCsrfToken() throws Exception {
        mockMvc.perform(patch("/posts/update/1")
                        .header("Authorization", "Bearer valid-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/posts/delete/1")
                        .header("Authorization", "Bearer valid-jwt"))
                .andExpect(status().isOk());
    }
}
