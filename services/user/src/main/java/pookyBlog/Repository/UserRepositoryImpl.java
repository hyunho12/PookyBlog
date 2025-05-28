package pookyBlog.Repository;

import pookyBlog.Entity.User;
import pookyBlog.Entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByUsername(String username) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(QUser.user)
                .where(QUser.user.username.eq(username))
                .fetchFirst(); // 값이존재하면 1, 없으면 null 반환
        return fetchOne != null;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(QUser.user)
                        .where(username != null ? QUser.user.username.eq(username) : QUser.user.username.isNull())
                        .fetchOne()
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(QUser.user)
                .where(QUser.user.email.eq(email))
                .fetchFirst();
        return fetchOne != null;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(QUser.user)
                        .where(email != null ? QUser.user.email.eq(email) : QUser.user.email.isNull())
                        .fetchOne()
        );
    }
}
