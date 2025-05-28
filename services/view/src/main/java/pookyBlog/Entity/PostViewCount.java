package pookyBlog.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "post_view_count")
@Getter
@NoArgsConstructor
@Entity
@Builder
public class PostViewCount {
    @Id
    private Long id;
    private Long viewCount;

    public PostViewCount(Long id, Long viewCount) {
        this.id = id;
        this.viewCount = viewCount;
    }
}
