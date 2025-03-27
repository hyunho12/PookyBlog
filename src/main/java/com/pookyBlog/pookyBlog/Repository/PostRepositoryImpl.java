package com.pookyBlog.pookyBlog.Repository;


import com.pookyBlog.pookyBlog.Dto.Request.PostSearch;
import com.pookyBlog.pookyBlog.Entity.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.pookyBlog.pookyBlog.Entity.QComment.comment;
import static com.pookyBlog.pookyBlog.Entity.QPost.post;
import static com.pookyBlog.pookyBlog.Entity.QUser.user;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Post> getList(PostSearch postSearch) {
        return jpaQueryFactory.selectFrom(post)
                .limit(postSearch.getSize())
                .offset(postSearch.getOffset())
                .orderBy(post.id.desc())
                .fetch();
    }

    @Override
    public Page<Post> getAllListWithPagination(PostSearch postSearch, Pageable pageable) {
        List<Post> content = jpaQueryFactory.selectFrom(post)
                .offset(postSearch.getOffset()) // 몇번째 페이지부터 시작
                .limit(postSearch.getSize()) // 페이지당 몇개
                .orderBy(post.id.desc())
                .fetch();

        long total = jpaQueryFactory.selectFrom(post)
                .fetch()
                .size();


        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<Post> findByIdWithComments(Long id) {
        Post postComment = jpaQueryFactory
                .selectFrom(post)
                .leftJoin(post.comments, comment).fetchJoin()
                .leftJoin(comment.user, user).fetchJoin()
                .where(post.id.eq(id))
                .distinct()
                .fetchOne();

        return Optional.ofNullable(postComment);
    }
}
