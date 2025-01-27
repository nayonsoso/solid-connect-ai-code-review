package com.example.solidconnection.unit.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.comment.domain.Comment;
import com.example.solidconnection.comment.dto.CommentCreateRequest;
import com.example.solidconnection.comment.dto.CommentCreateResponse;
import com.example.solidconnection.comment.dto.CommentDeleteResponse;
import com.example.solidconnection.comment.dto.CommentUpdateRequest;
import com.example.solidconnection.comment.dto.CommentUpdateResponse;
import com.example.solidconnection.comment.dto.PostFindCommentResponse;
import com.example.solidconnection.comment.repository.CommentRepository;
import com.example.solidconnection.comment.service.CommentService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPDATE_DEPRECATED_COMMENT;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COMMENT_ID;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COMMENT_LEVEL;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ACCESS;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ID;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

// todo: 많은 개선 필요
@TestContainerSpringBootTest
@DisplayName("댓글 서비스 테스트")
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentRepository commentRepository;

    private SiteUser siteUser;
    private Board board;
    private Post post;
    private Comment parentComment;
    private Comment parentCommentWithNullContent;
    private Comment childComment;
    private Comment childCommentOfNullContentParent;

    @BeforeEach
    void setUp() {
        siteUser = siteUserRepository.save(createSiteUser());
        board = boardRepository.save(createBoard());
        post = postRepository.save(createPost(board, siteUser));
        parentComment = commentRepository.save(createParentComment("parent"));
        parentCommentWithNullContent = commentRepository.save(createParentComment(null));
        childComment = commentRepository.save(createChildComment(parentComment));
        childCommentOfNullContentParent = commentRepository.save(createChildComment(parentCommentWithNullContent));
    }

    @Test
    void 특정_게시글의_댓글들을_조회한다() {
        // Given
        List<Comment> commentList = List.of(
                parentComment, childComment, parentCommentWithNullContent, childCommentOfNullContentParent);

        // When
        List<PostFindCommentResponse> postFindCommentResponses = commentService.findCommentsByPostId(
                siteUser.getEmail(), post.getId());

        // Then
        List<PostFindCommentResponse> expectedResponse = commentList.stream()
                .map(comment -> PostFindCommentResponse.from(isOwner(comment, siteUser), comment))
                .toList();
        assertThat(postFindCommentResponses).extracting(PostFindCommentResponse::id)
                .containsExactlyInAnyOrderElementsOf(expectedResponse.stream()
                        .map(PostFindCommentResponse::id)
                        .toList());
    }

    private Boolean isOwner(Comment comment, SiteUser siteUser) {
        return comment.getSiteUser().getId().equals(siteUser.getId());
    }

    /**
     * 댓글 등록
     */
    @Test
    void 댓글을_등록한다() {
        // Given
        CommentCreateRequest parent = new CommentCreateRequest("parent", null);
        CommentCreateRequest child = new CommentCreateRequest("parent", null);

        // When & Then
        assertAll(() -> assertThatCode(() -> commentService.createComment(siteUser, post.getId(), parent))
                        .doesNotThrowAnyException(),
                () -> assertThatCode(() -> commentService.createComment(siteUser, post.getId(), child))
                        .doesNotThrowAnyException()
        );
    }

    @Test
    void 댓글을_등록할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest("child", null);

        // When & Then
        assertThatCode(() -> commentService.createComment(siteUser, invalidPostId, commentCreateRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    @Test
    void 댓글을_등록할_때_유효한_부모_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidParentCommentId = -1L;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest("child", invalidParentCommentId);

        // When & Then
        assertThatCode(() -> commentService.createComment(siteUser, post.getId(), commentCreateRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_COMMENT_ID.getMessage());
    }

    @Test
    void 댓글을_등록할_때_대대댓글_부터는_예외_응답을_반환한다() {
        // Given
        CommentCreateResponse parent = commentService.createComment(
                siteUser, post.getId(), new CommentCreateRequest("txt", null));
        CommentCreateResponse child = commentService.createComment(
                siteUser, post.getId(), new CommentCreateRequest("txt", parent.id()));

        // When & Then
        CommentCreateRequest childchild = new CommentCreateRequest("txt", child.id());
        assertThatCode(() -> commentService.createComment(siteUser, post.getId(), childchild))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_COMMENT_LEVEL.getMessage());
    }

    @Test
    void 댓글을_수정한다() {
        // Given
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("update");

        // When
        CommentUpdateResponse commentUpdateResponse = commentService.updateComment(
                siteUser, post.getId(), parentComment.getId(), commentUpdateRequest);

        // Then
        assertThat(commentUpdateResponse.id()).isEqualTo(parentComment.getId());
    }

    @Test
    void 댓글을_수정할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        CommentUpdateRequest request = new CommentUpdateRequest("update");

        // When & Then
        assertThatCode(() -> commentService.updateComment(siteUser, invalidPostId, parentComment.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    @Test
    void 댓글을_수정할_때_유효한_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidCommentId = -1L;
        CommentUpdateRequest request = new CommentUpdateRequest("update");

        // When & Then
        assertThatCode(() -> commentService.updateComment(siteUser, post.getId(), invalidCommentId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_COMMENT_ID.getMessage());
    }

    @Test
    void 댓글을_수정할_때_이미_삭제된_댓글이라면_예외_응답을_반환한다() {
        // Given
        parentComment.deprecateComment();
        commentRepository.save(parentComment);
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("update");

        // When & Then
        assertThatCode(() -> commentService.updateComment(siteUser, post.getId(), parentComment.getId(), commentUpdateRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(CAN_NOT_UPDATE_DEPRECATED_COMMENT.getMessage());
    }

    @Test
    void 댓글을_수정할_때_자신의_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidEmail = "invalidEmail@test.com";
        SiteUser other = createSiteUserByEmail(invalidEmail);
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("update");

        // When & Then
        assertThatCode(() -> commentService.updateComment(other, post.getId(), parentComment.getId(), commentUpdateRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ACCESS.getMessage());
    }

    @Test
    void 댓글을_삭제한다_자식댓글_있음() {
        // Given
        Long parentCommentId = 1L;

        // When
        CommentDeleteResponse response = commentService.deleteCommentById(siteUser, post.getId(), parentCommentId);

        // Then
        assertThat(response.id()).isEqualTo(parentCommentId);
    }

    @Test
    void 댓글을_삭제한다_자식댓글_없음() {
        // Given
        Long childCommentId = 1L;

        // When
        CommentDeleteResponse commentDeleteResponse = commentService.deleteCommentById(
                siteUser, post.getId(), childCommentId);

        // Then
        assertThat(commentDeleteResponse.id()).isEqualTo(childCommentId);
    }

    @Test
    void 대댓글을_삭제한다_부모댓글_유효() {
        // Given
        Long childCommentId = 1L;

        // When
        CommentDeleteResponse response = commentService.deleteCommentById(siteUser, post.getId(), childCommentId);

        // Then
        assertThat(response.id()).isEqualTo(childCommentId);
    }

    @Test
    void 대댓글을_삭제한다_부모댓글_유효하지_않음() {
        // Given
        Long childCommentId = 1L;

        // When
        CommentDeleteResponse commentDeleteResponse = commentService.deleteCommentById(
                siteUser, post.getId(), childCommentId);

        // Then
        assertThat(commentDeleteResponse.id()).isEqualTo(childCommentId);
    }

    @Test
    void 댓글을_삭제할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;

        // When & Then
        assertThatCode(() -> commentService.deleteCommentById(siteUser, invalidPostId, parentComment.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    @Test
    void 댓글을_삭제할_때_유효한_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidCommentId = -1L;

        // When & Then
        assertThatCode(() -> commentService.deleteCommentById(siteUser, post.getId(), invalidCommentId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_COMMENT_ID.getMessage());
    }

    @Test
    void 댓글을_삭제할_때_자신의_댓글이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidEmail = "invalidEmail@test.com";
        SiteUser other = createSiteUserByEmail(invalidEmail);

        // When & Then
        assertThatCode(() -> commentService.deleteCommentById(other, post.getId(), parentComment.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ACCESS.getMessage());
    }

    private SiteUser createSiteUser() {
        return new SiteUser(
                "test@example.com",
                "nickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE
        );
    }

    private Board createBoard() {
        return new Board("FREE", "자유게시판");
    }

    private Post createPost(Board board, SiteUser siteUser) {
        Post post = new Post(
                "title",
                "content",
                false,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        post.setBoardAndSiteUser(board, siteUser);
        return post;
    }

    private Comment createParentComment(String content) {
        Comment comment = new Comment(
                content
        );
        comment.setPostAndSiteUser(post, siteUser);
        return comment;
    }

    private Comment createChildComment(Comment parentComment) {
        Comment comment = new Comment(
                "child"
        );
        comment.setParentCommentAndPostAndSiteUser(parentComment, post, siteUser);
        return comment;
    }
}
