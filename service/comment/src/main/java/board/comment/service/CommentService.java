package board.comment.service;

import board.comment.entity.Comment;
import board.comment.repository.CommentRepository;
import board.comment.service.request.CommentCreateRequest;
import board.comment.service.response.CommentPageResponse;
import board.comment.service.response.CommentResponse;
import board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = this.findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if ( parentCommentId == null) {
            // 상위 댓글이 없음
            return null;
        }

        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted)) // 삭제 안됨
                .filter(Comment::isRoot) // 루트이어야함 페이지당 2개 댓글표시니
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (this.hasChildren(comment)) {
                        comment.delete(); // 삭제시 자식 댓글이 있으면 삭제표시하고 더티 체킹
                    } else {
                        this.delete(comment); // 없으면 진짜로 삭제
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2; // 나와 자식 포함하여 2개
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment); // 삭제
        if (!comment.isRoot()) { // 최상위 댓글이 아닌 경우
            commentRepository.findById(comment.getParentCommentId()) // 부모 댓글 조회하여
                    .filter(Comment::getDeleted) // 부모 댓글이 삭제된 표시이어야며
                    .filter(not(this::hasChildren)) // 자식은 가지고 있지 않아야한다.
                    .ifPresent(this::delete); // 삭제
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
                commentRepository.findAllInfiniteScroll(articleId, limit) :
                commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

}
