package pookyBlog.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Table(name = "like_count")
@Entity
public class LikeCount {
    @Id
    private Long id;
    private Long likeCount = 0L;
    @Version
    private Long version;

    public void increaseLikeCount() {
        this.likeCount++;
    }
    public void decreaseLikeCount() {
        this.likeCount--;
    }
}
