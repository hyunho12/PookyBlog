package com.pookyBlog.pookyBlog.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Comment extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comments; // 댓글 내용

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post posts;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void update(String comment){
        this.comments = comment;
    }
}
