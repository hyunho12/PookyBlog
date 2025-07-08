package pookyBlog.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pookyBlog.Repository.HotCommentCountRepository;
import pookyBlog.Repository.PostLikeCountRepository;
import pookyBlog.Repository.PostViewCountRepository;

@Service
@RequiredArgsConstructor
public class HotPostScoreCalculator {
    private final PostLikeCountRepository postLikeCountRepository;
    private final PostViewCountRepository postViewCountRepository;
    private final HotCommentCountRepository commentCountRepository;

    private static final long POST_LIKE_COUNT_WEIGHT = 3;
    private static final long POST_COMMENT_COUNT_WEIGHT = 2;
    private static final long POST_VIEW_COUNT_WEIGHT = 1;

    public long calculate(Long postId){
        Long postLikeCount = postLikeCountRepository.get(postId);
        Long postViewCount = postViewCountRepository.get(postId);
        Long postCommentCount = commentCountRepository.get(postId);

        return postLikeCount * POST_LIKE_COUNT_WEIGHT
                + postViewCount * POST_VIEW_COUNT_WEIGHT
                + postCommentCount * POST_COMMENT_COUNT_WEIGHT;
    }
}
