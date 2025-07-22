package pookyBlog.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Commit;
import org.springframework.web.client.RestClient;
import pookyBlog.Dto.Request.PostCreate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PostKafkaTest {
    private final RestClient restClient = RestClient.create("http://localhost:8083");

    @Test
    @WithMockUser
    @Commit
    @DisplayName("게시글 생성 API를 호출하고, 애플리케이션 로그를 통해 전체 과정을 확인한다")
    public void createTest_withRestClient() {
        // given: API 요청에 필요한 DTO를 생성합니다.
        PostCreate postCreate = PostCreate.builder()
                .title("RestClient 테스트 제목")
                .content("RestClient 테스트 내용")
                .writer("rest_tester")
                .build();

        // when: RestClient를 사용하여 실제 실행 중인 애플리케이션에 POST 요청을 보냅니다.
        ResponseEntity<Void> response = restClient.post()
                .uri("/posts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(postCreate)
                .retrieve()
                .toBodilessEntity(); // 응답 본문이 없다면 toBodilessEntity() 사용

        // then: 자동화된 검증이 아닌, 수동 확인을 위한 단계입니다.
        // 1. HTTP 응답 상태 코드가 성공(2xx)인지 기본적인 확인을 합니다.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        System.out.println(">>> API 요청 성공! 응답 코드: " + response.getStatusCode());

        // 2. 개발자가 직접 '실행 중인 애플리케이션의 콘솔 로그'를 확인해야 합니다.
        System.out.println(">>> 이제 실행 중인 'post-service'의 콘솔 창을 확인하여 아래 로그들이 순서대로 찍히는지 확인하세요.");
        System.out.println("    1. [MessageRelay.createOutbox] 로그");
        System.out.println("    2. [MessageRelay.publishEvent] Success 로그");
        System.out.println(">>> 마지막으로 DB에 접속하여 'outbox' 테이블이 비어있는지 확인하면 완벽합니다.");
    }
}
