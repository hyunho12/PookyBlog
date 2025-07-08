package pookyBlog.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "post_count")
@Entity
@NoArgsConstructor
@Getter
public class PostCount {
    @Id
    private Long id;
    private Long postCount = 0L;

    public PostCount(Long id, Long postCount) {
        this.id = id;
        this.postCount = postCount;
    }

    public void increasePostCount() {
        this.postCount++;
    }
    public void decreasePostCount() {
        this.postCount--;
    }
}
