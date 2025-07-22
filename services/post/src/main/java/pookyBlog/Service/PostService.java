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
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.common.outboxmessage.OutboxRepository;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.PostCreatedEventPayload;
import pookyBlog.event.payload.PostUnlikedEventPayload;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostCountRepository postCountRepository;
    private final Snowflake snowflake;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void write(PostCreate postCreate){
        Post post = Post.builder()
                .id(snowflake.nextId())
                .title(postCreate.getTitle())
                .content(postCreate.getContent())
                .writer(postCreate.getWriter())
                .build();
        Post prePost = postRepository.save(post);
        postCountRepository.save(new PostCount(prePost.getId(), 0L));
        postCountRepository.increasePostCount(prePost.getId());

        outboxEventPublisher.publish(
                EventType.POST_CREATED,
                PostCreatedEventPayload.builder()
                        .postId(prePost.getId())
                        .title(prePost.getTitle())
                        .content(prePost.getContent())
                        .writer(prePost.getWriter())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
//                        .createdAt(LocalDateTime.parse(post.getCreatedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
//                        .updatedAt(LocalDateTime.parse(post.getModifiedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")))
                        .build(),
                0L
        );
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

        outboxEventPublisher.publish(
                EventType.POST_CREATED,
                PostCreatedEventPayload.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .writer(post.getWriter())
                        .createdAt(LocalDateTime.parse(post.getCreatedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .updatedAt(LocalDateTime.parse(post.getModifiedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")))
                        .build(),
                0L
        );
    }

    public void delete(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 id입니다."));

        postRepository.delete(post);
        postCountRepository.deleteById(post.getId());
        postCountRepository.decreasePostCount(postId);

        outboxEventPublisher.publish(
                EventType.POST_DELETED,
                PostCreatedEventPayload.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .writer(post.getWriter())
                        .createdAt(LocalDateTime.parse(post.getCreatedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .updatedAt(LocalDateTime.parse(post.getModifiedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")))
                        .build(),
                0L
        );
    }

    public Long count(Long postId){
        return postCountRepository.findPostCountById(postId);
    }
}
