package pookyBlog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pookyBlog.Service.HotPostService;
import pookyBlog.common.jwt.JwtAuthenticationFilter;
import pookyBlog.common.jwt.JwtTokenProvider;
import pookyBlog.config.SecurityConfig;
import pookyBlog.controller.HotPostController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotPostController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, HotPostController.class})
class HotPostSecurityConfigTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean HotPostService hotPostService;
    @MockitoBean JwtTokenProvider jwtTokenProvider;

    @Test
    void todaysHotPostsArePubliclyReadable() throws Exception {
        mockMvc.perform(get("/hot-posts/date/20261016"))
                .andExpect(status().isOk());
    }
}
