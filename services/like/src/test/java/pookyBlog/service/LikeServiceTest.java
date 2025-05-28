package pookyBlog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.Dto.Request.PostSearch;
import pookyBlog.Dto.Response.PostIndexResponse;
import pookyBlog.Entity.Role;
import pookyBlog.Entity.User;
import pookyBlog.LikeApplication;
import pookyBlog.Repository.LikeRepository;
import pookyBlog.Repository.PostRepository;
import pookyBlog.Repository.UserRepository;
import pookyBlog.Service.LikeService;
import pookyBlog.common.snowflake.Snowflake;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = LikeApplication.class)
class LikeServiceTest {
    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Snowflake snowflake;

    private Long userId;
    private Long postId;

    @BeforeEach
    void setup(){
        PostSearch postSearch = PostSearch.builder()
                .page(1)
                .size(10)
                .build();
        List<PostIndexResponse> post = postRepository.getAllPostWithPaginationWithoutCount(postSearch);
        postId = post.get(0).getId();
    }

    @Test
    @DisplayName("좋아요 누르고 확인하고 취소하는 기능검증")
    @Transactional
    void like_unlikeTest() throws Exception {
        User user = User.builder()
                .id(snowflake.nextId())
                .username("khh")
                .password("asdf")
                .email(UUID.randomUUID() + "test@naver.com")
                .nickname("khh12")
                .role(Role.USER)
                .build();
        userId = userRepository.save(user).getId();

        // 좋아요
        likeService.likePost(userId, postId);

        // 좋아요 여부 확인
        boolean hasLiked = likeService.hasUserLikePost(userId, postId);
        assertTrue(hasLiked, "좋아요 여부 확인");

        // 좋아요 취소
        likeService.unlikePost(userId, postId);

        // 다시확인
        boolean hasLikeAfterUnlike = likeService.hasUserLikePost(userId, postId);
        assertFalse(hasLikeAfterUnlike, "좋아요 취소 이후 좋아요상태");
    }

    @Test
    @DisplayName("비관적 락 동시성 테스트")
    @Transactional
    void likePessimistic() throws Exception{
        //given
        setupUsers(3000);
        long time = measure(() -> {
            try {
                runConcurrentTest(user -> () -> likeService.likePostWithPessimisticLock(user.getId(), postId));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("비관적 락 수행시간 " + time); //28
    }

    @Test
    @DisplayName("낙관적 락 동시성 테스트")
    @Transactional
    void likeOptimistic() throws Exception{
        setupUsers(3000);
        long time = measure(() -> {
            try{
                runConcurrentTest(user -> () -> likeService.likePostWithOptimisticLock(user.getId(), postId));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("낙관적 락 수행시간:" + time); // 32
    }

    @Test
    @DisplayName("단순증가 쿼리 동시성 테스트")
    @Transactional
    void likePlusQuery() throws Exception{
        setupUsers(3000);
        long time = measure(() -> {
            try {
                runConcurrentTest(user -> () -> likeService.likePostWithPlusQuery(user.getId(), postId));
            } catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        });
        System.out.println("단순 쿼리 수행시간:" + time); // 21
    }

    private void setupUsers(int count){
        List<User> users = IntStream.range(0, count)
                .mapToObj(i -> User.builder()
                        .id(snowflake.nextId())
                        .username("user" + i)
                        .password("asdf")
                        .email("user" + i + "@naver.com")
                        .nickname("nick" + i)
                        .role(Role.USER)
                        .build())
                .toList();
        userRepository.saveAll(users);
    }

    private void runConcurrentTest(Function<User, Runnable> taskProvider) throws InterruptedException {
        List<User> users = userRepository.findAll();
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(users.size());

        for (User user : users) {
            executor.execute(() -> {
                try{
                    taskProvider.apply(user).run();
                }
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    private long measure(Runnable action) {
        long start = System.currentTimeMillis();
        action.run();
        return (System.currentTimeMillis() - start) / 1000;
    }
}
