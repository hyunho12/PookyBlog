package com.pookyBlog.pookyBlog.Repository;

import com.pookyBlog.pookyBlog.Entity.User;

import java.util.Optional;

public interface UserRepositoryCustom {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
