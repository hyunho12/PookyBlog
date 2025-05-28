package pookyBlog.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pookyBlog.Dto.Request.PostSearch;
import pookyBlog.Dto.Response.PostIndexResponse;
import pookyBlog.PostApplication;
import pookyBlog.Repository.PostRepositoryImpl;
import pookyBlog.Service.PostService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PostApplication.class)
public class PostRepositoryTest {
    @Autowired
    private PostService postService;
    @Autowired
    private PostRepositoryImpl postRepositoryImpl;

    @Test
    void getAllListWithPaginationTest(){
        //given
        PostSearch postSearch = PostSearch.builder()
                .page(1)
                .size(10)
                .build();
        Pageable pageable = PageRequest.of(postSearch.getPage() - 1, postSearch.getSize());

        //when
        Page<PostIndexResponse> result = postRepositoryImpl.getAllListWithPagination(postSearch, pageable);

        //then
        assertThat(result.getContent()).hasSize(10);
        PostIndexResponse post = result.getContent().get(0);
        assertThat(post.getTitle()).startsWith("title");
        assertThat(post.getWriter()).startsWith("writer");
    }

    @Test
    @DisplayName("Index 페이지 쿼리 실행속도")
    public void querySpeedCheck() throws Exception{
        //given
        PostSearch postSearch = PostSearch.builder()
                .page(1)
                .size(10)
                .build();

        Pageable pageable = PageRequest.of(postSearch.getPage() - 1, postSearch.getSize());

        //when
        long startTime = System.currentTimeMillis();
        Page<PostIndexResponse> post = postService.getAllPostsWithPagination(postSearch, pageable);
        long endTime = System.currentTimeMillis();

        //then
        long duration = (endTime - startTime) / 1000;
        System.out.println("Query 실행시간: " + duration);
    }

    @Test
    @DisplayName("count없이 index페이지쿼리 속도체크")
    public void querySpeedCheckWithoutCount() throws Exception{
        //given
        PostSearch postSearch = PostSearch.builder()
                .page(1)
                .size(10)
                .build();

        //when
        long startTime = System.currentTimeMillis();
        List<PostIndexResponse> posts = postService.getAllPostWithPaginationWithoutCount(postSearch);
        long endTime = System.currentTimeMillis();

        //then
        long duration = (endTime - startTime) / 1000;
        System.out.println("Query 실행시간 (count 없이): " + duration + "초");

    }
}
