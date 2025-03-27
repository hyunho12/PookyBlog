package com.pookyBlog.pookyBlog.Service;

import com.pookyBlog.pookyBlog.Dto.Request.PostCreate;
import com.pookyBlog.pookyBlog.Dto.Request.PostSearch;
import com.pookyBlog.pookyBlog.Dto.Request.PostUpdate;
import com.pookyBlog.pookyBlog.Dto.Response.PostResponse;
import com.pookyBlog.pookyBlog.Entity.Post;
import com.pookyBlog.pookyBlog.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public void write(PostCreate postCreate){
        Post post = Post.builder()
                .title(postCreate.getTitle())
                .content(postCreate.getContent())
                .writer(postCreate.getWriter())
                .build();
        postRepository.save(post);
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

    public Page<Post> getAllPostsWithPagination(PostSearch postSearch, Pageable pageable) {
        return postRepository.getAllListWithPagination(postSearch, pageable);
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
    }

}
