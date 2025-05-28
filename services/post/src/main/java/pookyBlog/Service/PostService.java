package pookyBlog.Service;

import pookyBlog.Dto.Request.PostCreate;
import pookyBlog.Dto.Request.PostSearch;
import pookyBlog.Dto.Request.PostUpdate;
import pookyBlog.Dto.Response.PostIndexResponse;
import pookyBlog.Dto.Response.PostResponse;
import pookyBlog.Entity.Post;
import pookyBlog.Entity.PostCount;
import pookyBlog.Repository.PostCountRepository;
import pookyBlog.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.common.snowflake.Snowflake;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostCountRepository postCountRepository;
    private final Snowflake snowflake;

    public void write(PostCreate postCreate){
        Post post = Post.builder()
                .id(snowflake.nextId())
                .title(postCreate.getTitle())
                .content(postCreate.getContent())
                .writer(postCreate.getWriter())
                .build();
        postRepository.save(post);
        postCountRepository.save(new PostCount(post.getId(), 0L));
        postCountRepository.increasePostCount(post.getId());
    }

    public PostResponse get(Long postId) {
        Post post = postRepository.findByIdWithComments(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id입니다."));

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter())
                .createdDate(post.getCreatedDate())
                .view(post.getView())
                .comments(post.getComments())
                .build();
    }

    public List<PostResponse> getListPosts(PostSearch postSearch) {
        return postRepository.getList(postSearch).stream()
                .map(PostResponse::new)
                .collect(Collectors.toList());
    }

    public Page<PostIndexResponse> getAllPostsWithPagination(PostSearch postSearch, Pageable pageable) {
        return postRepository.getAllListWithPagination(postSearch, pageable);
    }

    public List<PostIndexResponse> getAllPostWithPaginationWithoutCount(PostSearch postSearch){
        return postRepository.getAllPostWithPaginationWithoutCount(postSearch);
    }

    @Transactional
    public void update(Long postId, PostUpdate postUpdate) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 id입니다."));

        post.update(postUpdate.getTitle() != null ? postUpdate.getTitle() : post.getTitle(),
                    postUpdate.getContent() != null ? postUpdate.getContent() : post.getContent());
    }

    public void delete(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 id입니다."));

        postRepository.delete(post);
        postCountRepository.deleteById(post.getId());
        postCountRepository.decreasePostCount(postId);
    }

    public Long count(Long postId){
        return postCountRepository.findPostCountById(postId);
    }
}
