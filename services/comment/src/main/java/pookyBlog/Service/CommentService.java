package pookyBlog.Service;

import pookyBlog.Dto.Request.CommentCreate;
import pookyBlog.Dto.Request.CommentUpdate;
import pookyBlog.Entity.Comment;
import pookyBlog.Entity.Post;
import pookyBlog.Entity.User;
import pookyBlog.Repository.CommentRepository;
import pookyBlog.Repository.PostRepository;
import pookyBlog.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.common.outboxmessage.OutboxEventPublisher;
import pookyBlog.event.EventType;
import pookyBlog.event.payload.CommentCreatedEventPayload;
import pookyBlog.event.payload.CommentDeletedEventPayload;

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
                .posts(post)
                .user(user)
                .comments(commentCreate.getComment())
                .build();

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

        return commentRepository.save(comment).getId();
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
    }
}
