package com.pookyBlog.pookyBlog.Repository;

import com.pookyBlog.pookyBlog.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, UserRepositoryCustom{
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
