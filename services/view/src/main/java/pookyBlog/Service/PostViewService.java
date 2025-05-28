package pookyBlog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pookyBlog.Repository.PostViewCountRepository;
import pookyBlog.Repository.PostViewDistributedLockRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PostViewService {
    private final PostViewCountRepository postViewCountRepository;
    private final PostViewCountBackUpProcessor postViewCountBackUpProcessor;
    private final PostViewDistributedLockRepository postViewDistributedLockRepository;

    private static final int BACK_UP_BATCH_SIZE = 100;
    private static Duration TTL = Duration.ofSeconds(10);

    public Long increase(Long postId, Long userId){
        if(!postViewDistributedLockRepository.lock(postId, userId, TTL)){
            return postViewCountRepository.read(postId); // false lock획득실패 이미 조회한상태
        }

        Long count = postViewCountRepository.increase(postId);
        if(count % BACK_UP_BATCH_SIZE == 0){
            postViewCountBackUpProcessor.backUp(postId, count);
        }
        return count;
    }

    public Long count(Long postId){
        return postViewCountRepository.read(postId);
    }
}
