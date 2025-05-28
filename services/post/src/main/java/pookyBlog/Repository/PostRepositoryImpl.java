package pookyBlog.Repository;

import com.querydsl.core.types.Projections;
import pookyBlog.Dto.Request.PostSearch;
import pookyBlog.Dto.Response.PostIndexResponse;
import pookyBlog.Entity.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static pookyBlog.Entity.QComment.comment;
import static pookyBlog.Entity.QPost.post;
import static pookyBlog.Entity.QUser.user;

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
    public Page<PostIndexResponse> getAllListWithPagination(PostSearch postSearch, Pageable pageable) {
        List<PostIndexResponse> content = jpaQueryFactory
                .select(Projections.constructor(PostIndexResponse.class,
                        post.id,
                        post.title,
                        post.writer,
                        post.createdDate
                ))
                .from(post)
                .offset(postSearch.getOffset())
                .limit(postSearch.getSize())
                .orderBy(post.id.desc())
                .fetch();

        long total = jpaQueryFactory
                .select(post.count())
                .from(post)
                .fetchOne();


        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<PostIndexResponse> getAllPostWithPaginationWithoutCount(PostSearch postSearch) {
        return jpaQueryFactory
                .select(Projections.constructor(PostIndexResponse.class,
                        post.id,
                        post.title,
                        post.writer,
                        post.createdDate
                ))
                .from(post)
                .offset(postSearch.getOffset())
                .limit(postSearch.getSize())
                .orderBy(post.id.desc())
                .fetch();
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
