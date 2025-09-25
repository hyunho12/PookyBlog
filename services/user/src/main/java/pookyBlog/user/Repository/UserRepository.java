package pookyBlog.user.Repository;

import pookyBlog.common.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, UserRepositoryCustom{
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
