package com.pookyBlog.pookyBlog.Service;

import com.pookyBlog.pookyBlog.Dto.Request.PostCreate;
import com.pookyBlog.pookyBlog.Dto.Request.PostSearch;
import com.pookyBlog.pookyBlog.Dto.Request.PostUpdate;
import com.pookyBlog.pookyBlog.Dto.Response.PostResponse;
import com.pookyBlog.pookyBlog.Entity.Post;
import com.pookyBlog.pookyBlog.Repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class PostServiceTest {
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    public void clean(){
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("글 작성")
    public void test1() throws Exception{
        //given
        PostCreate postCreate = PostCreate.builder()
                .title("제목생성")
                .content("내용생성")
                .build();
        //when
        postService.write(postCreate);

        //then
        assertThat(postRepository.count()).isEqualTo(1L);
        Post post = postRepository.findAll().get(0);
        assertThat(post.getTitle()).isEqualTo(postCreate.getTitle());
        assertThat(post.getContent()).isEqualTo(postCreate.getContent());
    }

    @Test
    @DisplayName("글 1개 조회")
    public void test2() throws Exception{
        //given
        Post requestPost = Post.builder()
                .title("pooky")
                .content("bear")
                .build();
        postRepository.save(requestPost);

        //when
        PostResponse response = postService.get(requestPost.getId());

        //then
        assertThat(response).isNotNull();
        assertThat(postRepository.count()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("pooky");
        assertThat(response.getContent()).isEqualTo("bear");
    }

    @Test
    @DisplayName("글 여러개 조회")
    public void test3() throws Exception{
        //given
        List<Post> requestPost = IntStream.range(0,20)
                .mapToObj(i-> Post.builder()
                        .title("pooky" + i)
                        .content("poo" + i)
                        .build())
                .collect(Collectors.toList());
        postRepository.saveAll(requestPost);

        PostSearch postSearch = PostSearch.builder()
                .page(1)
                .build();

        //when
        List<PostResponse> posts = postService.getListPosts(postSearch);

        //then
        assertThat(posts.size()).isEqualTo(10L);
        assertThat(posts.get(0).getTitle()).isEqualTo("pooky19");

    }

    @Test
    @DisplayName("글 제목 수정")
    public void test4() throws Exception{
        //given
        Post post = Post.builder()
                .title("pooky")
                .content("송파")
                .build();
        postRepository.save(post);

        PostUpdate postUpdate = PostUpdate.builder()
                .title("피카츄")
                .content("송파")
                .build();
        //when
        postService.update(post.getId(), postUpdate);

        //then
        Post updatedPost = postRepository.findById(post.getId())
                .orElseThrow(()-> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(updatedPost.getTitle()).isEqualTo("피카츄");
    }

    @Test
    @DisplayName("글 내용 수정")
    public void test5() throws Exception{
        //given
        Post post = Post.builder()
                .title("pooky")
                .content("시그니엘")
                .build();
        postRepository.save(post);

        PostUpdate postUpdate = PostUpdate.builder()
                .title("pooky")
                .content("아크로포레스트")
                .build();

        //when
        postService.update(post.getId(), postUpdate);

        //then
        Post updatedPost = postRepository.findById(post.getId())
                .orElseThrow(()-> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));
        assertThat(updatedPost.getContent()).isEqualTo("아크로포레스트");
    }

    @Test
    @DisplayName("게시글 삭제")
    public void test6() throws Exception{
        //given
        Post post = Post.builder()
                .title("pooky")
                .content("test")
                .build();
        postRepository.save(post);

        //when
        postService.delete(post.getId());

        //then
        assertThat(postRepository.count()).isEqualTo(0L);

    }
}