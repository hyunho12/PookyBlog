package pookyBlog.comment.Service;

import pookyBlog.common.Dto.Request.CommentCreate;
import pookyBlog.common.Dto.Request.CommentUpdate;
import pookyBlog.common.Entity.Comment;
import pookyBlog.common.Entity.Post;
import pookyBlog.common.Entity.User;
import pookyBlog.comment.Repository.CommentRepository;
import pookyBlog.post.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.common.event.EventType;
import pookyBlog.common.event.payload.CommentCreatedEventPayload;
import pookyBlog.common.event.payload.CommentDeletedEventPayload;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.user.Repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final Snowflake snowflake;

    @Transactional
    public Long create(CommentCreate commentCreate){
        if(commentCreate.getUserId() == null){
            throw new IllegalArgumentException("userId가 제공되지 않음.");
        }
        if (commentCreate.getPostsId() == null) {
            throw new IllegalArgumentException("postId가 제공되지 않았습니다.");
        }


        Post post = postRepository.findById(commentCreate.getPostsId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user= userRepository.findById(commentCreate.getUserId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Comment comment = Comment.builder()
                .id(snowflake.nextId())
                .posts(post)
                .user(user)
                .comments(commentCreate.getComment())
                .build();

        commentRepository.save(comment);

        outboxEventPublisher.publish(
                EventType.COMMENT_CREATED,
                CommentCreatedEventPayload.builder()
                        .commentId(comment.getId())
                        .content(comment.getComments())
                        .postId(comment.getPosts().getId())
                        .writer(comment.getPosts().getWriter())
                        .createdAt(LocalDateTime.parse(comment.getCreatedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .postCommentCount(commentRepository.countByPosts_Id(comment.getPosts().getId()))
                        .build(),
                0L

        );

        return comment.getId();
    }

    public List<Comment> getComment(Long postId){
        return commentRepository.findByPostId(postId);
    }

    @Transactional
    public void update(Long commentId, CommentUpdate commentUpdate){
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        comment.update(commentUpdate.getContent());
    }

    @Transactional
    public void delete(Long commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        commentRepository.delete(comment);

        outboxEventPublisher.publish(
                EventType.COMMENT_DELETED,
                CommentDeletedEventPayload.builder()
                        .commentId(comment.getId())
                        .content(comment.getComments())
                        .postId(comment.getPosts().getId())
                        .writerId(comment.getUser().getId())
                        .deleted(true)
                        .createdAt(LocalDateTime.parse(comment.getCreatedDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .postCommentCount(commentRepository.countByPosts_Id(comment.getPosts().getId()))
                        .build(),
                0L
        );
    }

    @Transactional(readOnly = true)
    public Long count(Long postId){
        return commentRepository.countByPosts_Id(postId);
    }
}
