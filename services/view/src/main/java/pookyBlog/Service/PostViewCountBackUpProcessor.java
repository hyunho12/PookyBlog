package pookyBlog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.Entity.PostViewCount;
import pookyBlog.Repository.PostViewCountBackUpRepository;

@Service
@RequiredArgsConstructor
public class PostViewCountBackUpProcessor {
    private final PostViewCountBackUpRepository postViewCountBackUpRepository;

    @Transactional
    public void backUp(Long postId, Long viewCount){
        int result = postViewCountBackUpRepository.updateViewCount(postId, viewCount);
        if(result == 0){
            postViewCountBackUpRepository.findById(postId)
                    .ifPresentOrElse(ignored -> { },
                            () -> postViewCountBackUpRepository.save(PostViewCount.builder()
                                            .id(postId)
                                            .viewCount(viewCount)
                                    .build()));
        }
    }
}
