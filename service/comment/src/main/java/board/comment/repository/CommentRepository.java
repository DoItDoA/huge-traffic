package board.comment.repository;

import board.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(
            value = "SELECT count(*) FROM (" +
                    "   SELECT comment_id FROM comment " +
                    "   WHERE article_id = :articleId AND parent_comment_id = :parentCommentId " +
                    "   LIMIT :limit" +
                    ") t",
            nativeQuery = true
    )
    Long countBy(@Param("articleId") Long articleId,
                 @Param("parentCommentId") Long parentCommentId,
                 @Param("limit") Long limit);

    @Query(
            value = "SELECT comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "FROM (" +
                    "   SELECT comment_id FROM comment WHERE article_id = :articleId " +
                    "   ORDER BY parent_comment_id ASC, comment_id ASC " +
                    "   LIMIT :limit OFFSET :offset " +
                    ") t LEFT JOIN comment ON t.comment_id = comment.comment_id",
            nativeQuery = true
    )
    List<Comment> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    @Query(
            value = "SELECT count(*) FROM (" +
                    "   SELECT comment_id FROM comment WHERE article_id = :articleId LIMIT :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    @Query(
            value = "SELECT comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "FROM comment " +
                    "WHERE article_id = :articleId " +
                    "ORDER BY parent_comment_id ASC, comment_id ASC " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    @Query(
            value = "SELECT comment.comment_id, comment.content, comment.parent_comment_id, comment.article_id, " +
                    "comment.writer_id, comment.deleted, comment.created_at " +
                    "FROM comment " +
                    "WHERE article_id = :articleId AND (" +
                    "   parent_comment_id > :lastParentCommentId or " +
                    "   (parent_comment_id = :lastParentCommentId AND comment_id > :lastCommentId) " +
                    ")" +
                    "ORDER BY parent_comment_id ASC, comment_id ASC " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastParentCommentId") Long lastParentCommentId,
            @Param("lastCommentId") Long lastCommentId,
            @Param("limit") Long limit
    );
}
