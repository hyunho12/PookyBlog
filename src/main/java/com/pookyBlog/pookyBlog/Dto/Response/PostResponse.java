package com.pookyBlog.pookyBlog.Dto.Response;

import com.pookyBlog.pookyBlog.Entity.Comment;
import com.pookyBlog.pookyBlog.Entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class PostResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String writer;
    private final String createdDate;
    private final Integer view;
    private final List<Comment> comments;

    public PostResponse(Post post){
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.writer = post.getWriter();
        this.createdDate = post.getCreatedDate();
        this.view = post.getView();
        this.comments = post.getComments();
    }

    @Builder
    public PostResponse(Long id, String title, String content, String writer, String createdDate, Integer view, List<Comment> comments) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createdDate = createdDate;
        this.view = view;
        this.comments = comments;
    }
}
