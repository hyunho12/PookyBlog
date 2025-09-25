package pookyBlog.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import pookyBlog.common.Dto.Request.SignUpDto;
import pookyBlog.common.Entity.Role;
import pookyBlog.user.Repository.UserRepository;
import pookyBlog.user.Service.UserService;
import pookyBlog.common.snowflake.Snowflake;
import pookyBlog.user.UserApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UserApplication.class)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Snowflake snowflake;

    @Test
    @Rollback(false)
    @DisplayName("회원가입 후 DB 롤백 확인")
    void signUp_and_rollback() {
        // given
        SignUpDto signUpDto = new SignUpDto("khh", "asdf", "khhNick", "khh@google.com", Role.USER);

        // when
        userService.signUp(signUpDto);

        // then
        // DB에 실제로 저장되었는지 확인
        boolean isUserPresent = userRepository.existsByUsername("khh");
    }
}
