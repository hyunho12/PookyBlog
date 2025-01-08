package com.pookyBlog.pookyBlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pookyBlog.pookyBlog.Dto.Request.PostCreate;
import com.pookyBlog.pookyBlog.Dto.Request.PostUpdate;
import com.pookyBlog.pookyBlog.Entity.Post;
import com.pookyBlog.pookyBlog.Repository.PostRepository;
import com.pookyBlog.pookyBlog.Service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import javax.print.attribute.standard.Media;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void clean(){
        postRepository.deleteAll();
    }


    @Test
    @DisplayName("/posts 요청시 title값은 필수")
    public void test2() throws Exception{
        //given
        PostCreate postCreate = PostCreate.builder()
                .content("내용")
                .build();
        String json = objectMapper.writeValueAsString(postCreate);

        //expected
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.title").value("제목을 입력하세요"))
                .andDo(print());

    }

    @Test
    @DisplayName("/posts요청시 db에 값 저장")
    void test3() throws Exception{
        //given
        PostCreate postCreate = PostCreate.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .build();
        String json = objectMapper.writeValueAsString(postCreate);

        //when
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        assertThat(postRepository.count()).isEqualTo(1L);

        Post post = postRepository.findAll().get(0);
        assertThat(post.getTitle()).isEqualTo("제목입니다.");
        assertThat(post.getContent()).isEqualTo("내용입니다.");
    }

    @Test
    @DisplayName("글 1개 조회")
    public void test4() throws Exception{
        //given
        Post post = Post.builder()
                .title("제목")
                .content("test")
                .build();
        postRepository.save(post);

        //expected
        mockMvc.perform(get("/posts/{postId}", post.getId())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.content").value("test"))
                .andDo(print());
    }

    @Test
    @DisplayName("글 여러개 조회")
    public void test5() throws Exception{
        //given
        List<Post> requestPosts = IntStream.range(0,20)
                .mapToObj(i-> Post.builder()
                        .title("pooky"+i)
                        .content("test"+i)
                        .build())
                .collect(Collectors.toList());

        postRepository.saveAll(requestPosts);

        //expected
        mockMvc.perform(get("/posts?page=1&size=10")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("pooky19"))
                .andExpect(jsonPath("$[0].content").value("test19"))
                .andDo(print());
    }

    @Test
    @DisplayName("글 제목 수정")
    void test7() throws Exception{
        //given
        Post post = Post.builder()
                .title("pooky")
                .content("시그니엘")
                .build();
        postRepository.save(post);

        PostUpdate postUpdate = PostUpdate.builder()
                .title("피카츄")
                .content("시그니엘")
                .build();

        String json = objectMapper.writeValueAsString(postUpdate);

        //expected
        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        Post changePost = postRepository.findAll().get(0);
        assertThat(changePost.getTitle()).isEqualTo("피카츄");
        assertThat(changePost.getContent()).isEqualTo("시그니엘");
    }

    @Test
    @DisplayName("게시글 삭제")
    void test8() throws Exception {
        // given
        Post post = Post.builder()
                .title("푸키")
                .content("반포자이")
                .build();
        postRepository.save(post);

        // expected
        mockMvc.perform(delete("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

}
