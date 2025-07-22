package pookyBlog.Repository;

import pookyBlog.Entity.Comment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static pookyBlog.Entity.QComment.comment;


@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Comment> findByPostId(Long postId) {
        return  jpaQueryFactory
                .selectFrom(comment)
                .where(postId != null ? comment.posts.id.eq(postId) : comment.id.isNull())
                .orderBy(comment.id.asc()) // 댓글을 ID 기준 오름차순 정렬
                .fetch();
    }


}
