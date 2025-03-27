package com.pookyBlog.pookyBlog.Service;

import com.pookyBlog.pookyBlog.Dto.Request.CommentCreate;
import com.pookyBlog.pookyBlog.Dto.Request.CommentUpdate;
import com.pookyBlog.pookyBlog.Entity.Comment;
import com.pookyBlog.pookyBlog.Entity.Post;
import com.pookyBlog.pookyBlog.Entity.User;
import com.pookyBlog.pookyBlog.Repository.CommentRepository;
import com.pookyBlog.pookyBlog.Repository.PostRepository;
import com.pookyBlog.pookyBlog.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public Long create(CommentCreate commentCreate){
        if(commentCreate.getUserId() == null){
            throw new IllegalArgumentException("userId가 제공되지 않음.");
        }
        if (commentCreate.getPostsId() == null) {
            throw new IllegalArgumentException("postId가 제공되지 않았습니다.");
        }


        Post post = postRepository.findById(commentCreate.getPostsId()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user= userRepository.findById(commentCreate.getUserId()).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Comment comment = Comment.builder()
                .posts(post)
                .user(user)
                .comments(commentCreate.getComment())
                .build();
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
    }
}
