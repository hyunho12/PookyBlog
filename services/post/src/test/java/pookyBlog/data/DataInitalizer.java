package pookyBlog.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import pookyBlog.Entity.Post;
import pookyBlog.PostApplication;
import pookyBlog.common.snowflake.Snowflake;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest(classes = PostApplication.class)
public class DataInitalizer {
    @PersistenceContext
    EntityManager em;

    @Autowired
    TransactionTemplate transactionTemplate;

    Snowflake snowflake = new Snowflake();

    static final int BULK_INSERT_SIZE = 2000;
    static final int EXECUTE_COUNT = 80;
    static final int THREAD_COUNT = 6;

    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    @Test
    void bulkInsertPosts() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for(int i=0; i<EXECUTE_COUNT; i++) {
            executor.submit(() -> {
                try {
                    insertPosts();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
    }

    void insertPosts(){
        transactionTemplate.executeWithoutResult(status -> {
            for(int i=0; i< BULK_INSERT_SIZE; i++){
                long id = snowflake.nextId();
                int random = ThreadLocalRandom.current().nextInt(10000);

                Post post = Post.builder()
                        .id(id)
                        .title("title" + id)
                        .content("content" + random)
                        .writer("writer" + random)
                        .build();

                em.persist(post);

                if(i % 100 == 0){
                    em.flush();
                    em.clear();
                }
            }
        });
    }
}
