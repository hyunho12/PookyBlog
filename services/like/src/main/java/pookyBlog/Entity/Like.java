package pookyBlog.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@Table(name = "post_like")
@Entity
public class Like extends BaseTimeEntity{
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public Like(Long id, User user, Post post) {
        this.id = id;
        this.user = user;
        this.post = post;
    }
}
