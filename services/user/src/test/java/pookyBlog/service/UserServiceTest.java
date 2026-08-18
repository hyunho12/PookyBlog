package pookyBlog.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pookyBlog.common.Dto.Request.SignUpDto;
import pookyBlog.common.Entity.Role;
import pookyBlog.user.Repository.UserRepository;
import pookyBlog.user.Service.UserService;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.user.UserApplication;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

@SpringBootTest(classes = UserApplication.class)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Snowflake snowflake;

    @Test
    @Transactional
    @DisplayName("회원가입 후 DB 롤백 확인")
    void signUp_and_rollback() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 12);
        String username = "test-" + uniqueId;
        SignUpDto signUpDto = new SignUpDto(username, "asdf", "nick-" + uniqueId, uniqueId + "@test.local", Role.USER);

        // when
        userService.signUp(signUpDto);

        // then
        // DB에 실제로 저장되었는지 확인
        boolean isUserPresent = userRepository.existsByUsername(username);
        assertThat(isUserPresent).isTrue();
    }
}
