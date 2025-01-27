package com.example.solidconnection.unit.service;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.comment.service.CommentService;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.custom.exception.ErrorCode;
import com.example.solidconnection.entity.PostImage;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.domain.PostLike;
import com.example.solidconnection.post.dto.PostCreateRequest;
import com.example.solidconnection.post.dto.PostCreateResponse;
import com.example.solidconnection.post.dto.PostDeleteResponse;
import com.example.solidconnection.post.dto.PostDislikeResponse;
import com.example.solidconnection.post.dto.PostFindResponse;
import com.example.solidconnection.post.dto.PostLikeResponse;
import com.example.solidconnection.post.dto.PostUpdateRequest;
import com.example.solidconnection.post.dto.PostUpdateResponse;
import com.example.solidconnection.post.repository.PostLikeRepository;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.post.service.PostService;
import com.example.solidconnection.s3.S3Service;
import com.example.solidconnection.s3.UploadedFileUrlResponse;
import com.example.solidconnection.service.RedisService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.ImgType;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.util.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_DELETE_OR_UPDATE_QUESTION;
import static com.example.solidconnection.custom.exception.ErrorCode.CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES;
import static com.example.solidconnection.custom.exception.ErrorCode.DUPLICATE_POST_LIKE;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ACCESS;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_ID;
import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_POST_LIKE;
import static com.example.solidconnection.e2e.DynamicFixture.createSiteUserByEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// todo: 테스트 보충할 필요 있음
@TestContainerSpringBootTest
@DisplayName("게시글 서비스 테스트")
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private CommentService commentService;

    @MockBean
    private RedisService redisService;

    @MockBean
    private RedisUtils redisUtils;

    @MockBean
    private S3Service s3Service;

    private SiteUser siteUser;
    private Board board;
    private Post post;
    private Post postWithImages;
    private Post questionPost;
    private List<MultipartFile> imageFiles;
    private List<UploadedFileUrlResponse> uploadedFileUrlResponseList;


    @BeforeEach
    void setUp() {
        siteUser = siteUserRepository.save(createSiteUser());
        board = boardRepository.save(createBoard());
        imageFiles = createMockImageFiles();
        uploadedFileUrlResponseList = createUploadedFileUrlResponses();
        post = postRepository.save(createPost(board, siteUser));
        postWithImages = postRepository.save(createPostWithImages(board, siteUser));
        questionPost = postRepository.save(createQuestionPost(board, siteUser));
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
        return new Board(
                "FREE", "자유게시판");
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

    private Post createPostWithImages(Board board, SiteUser siteUser) {
        Post postWithImages = new Post(
                "title",
                "content",
                false,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        postWithImages.setBoardAndSiteUser(board, siteUser);

        List<PostImage> postImageList = new ArrayList<>();
        postImageList.add(new PostImage("https://s3.example.com/test1.png"));
        postImageList.add(new PostImage("https://s3.example.com/test2.png"));
        for (PostImage postImage : postImageList) {
            postImage.setPost(postWithImages);
        }
        return postWithImages;
    }

    private Post createQuestionPost(Board board, SiteUser siteUser) {
        Post post = new Post(
                "title",
                "content",
                true,
                0L,
                0L,
                PostCategory.valueOf("자유")
        );
        post.setBoardAndSiteUser(board, siteUser);
        return post;
    }

    private PostLike createPostLike(Post post, SiteUser siteUser) {
        PostLike postLike = new PostLike();
        postLike.setPostAndSiteUser(post, siteUser);
        return postLike;
    }

    private List<MultipartFile> createMockImageFiles() {
        List<MultipartFile> multipartFileList = new ArrayList<>();
        multipartFileList.add(new MockMultipartFile("file1", "test1.png",
                "image/png", "test image content 1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file2", "test1.png",
                "image/png", "test image content 1".getBytes()));
        return multipartFileList;
    }

    private List<UploadedFileUrlResponse> createUploadedFileUrlResponses() {
        return Arrays.asList(
                new UploadedFileUrlResponse("https://s3.example.com/test1.png"),
                new UploadedFileUrlResponse("https://s3.example.com/test2.png")
        );
    }

    @Test
    void 게시글을_등록한다_이미지_있음() {
        // Given
        PostCreateRequest postCreateRequest = new PostCreateRequest("자유", "title", "content", false);
        when(s3Service.uploadFiles(imageFiles, ImgType.COMMUNITY)).thenReturn(uploadedFileUrlResponseList);

        // When
        PostCreateResponse postCreateResponse = postService.createPost(
                siteUser, board.getCode(), postCreateRequest, imageFiles
        );

        // Then
        assertThat(postCreateResponse).isNotNull();
    }

    @Test
    void 게시글을_등록한다_이미지_없음() {
        // Given
        PostCreateRequest postCreateRequest = new PostCreateRequest("자유", "title", "content", false);

        // When
        PostCreateResponse postCreateResponse = postService.createPost(
                siteUser, board.getCode(), postCreateRequest, Collections.emptyList());

        // Then
        assertThat(postCreateResponse).isNotNull();
    }

    @Test
    void 게시글을_등록할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";
        PostCreateRequest postCreateRequest = new PostCreateRequest("자유", "title", "content", false);

        // When & Then
        assertThatCode(() -> postService.createPost(siteUser, invalidBoardCode, postCreateRequest, List.of()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_BOARD_CODE.getMessage());
    }

    @Test
    void 게시글을_등록할_때_유효한_카테고리가_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidPostCategory = "invalidPostCategory";
        PostCreateRequest postCreateRequest = new PostCreateRequest(invalidPostCategory, "title", "content", false);

        // When & Then
        assertThatCode(() -> postService.createPost(siteUser, board.getCode(), postCreateRequest, List.of()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_POST_CATEGORY.getMessage());
    }

    @Test
    void 게시글을_등록할_때_파일_수가_5개를_넘는다면_예외_응답을_반환한다() {
        // Given
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "자유", "title", "content", false);
        List<MultipartFile> moreThanFiveFiles = createMockImageFilesWithMoreThanFiveFiles();

        // When & Then
        assertThatCode(() -> postService.createPost(siteUser, board.getCode(), postCreateRequest, moreThanFiveFiles))
                .isInstanceOf(CustomException.class)
                .hasMessage(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getMessage());
    }

    @Test
    void 게시글을_수정한다_기존_사진_없음_수정_사진_없음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("질문", "updateTitle", "updateContent");

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser, board.getCode(), post.getId(), postUpdateRequest, Collections.emptyList());

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    void 게시글을_수정한다_기존_사진_있음_수정_사진_없음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "updateTitle", "updateContent");

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser, board.getCode(), postWithImages.getId(), postUpdateRequest, Collections.emptyList());

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    void 게시글을_수정한다_기존_사진_없음_수정_사진_있음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "updateTitle", "updateContent");

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser, board.getCode(), post.getId(), postUpdateRequest, imageFiles);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    void 게시글을_수정한다_기존_사진_있음_수정_사진_있음() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "updateTitle", "updateContent");

        // When
        PostUpdateResponse response = postService.updatePost(
                siteUser, board.getCode(), postWithImages.getId(), postUpdateRequest, imageFiles);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    void 게시글을_수정할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        assertThatCode(() -> postService.updatePost(siteUser, invalidBoardCode, post.getId(), postUpdateRequest, imageFiles))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_BOARD_CODE.getMessage());
    }

    @Test
    void 게시글을_수정할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");

        // When & Then
        assertThatCode(() -> postService.updatePost(siteUser, board.getCode(), invalidPostId, postUpdateRequest, imageFiles))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    @Test
    void 게시글을_수정할_때_본인의_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        SiteUser otherSiteUser = siteUserRepository.save(createSiteUserByEmail("other@email.com"));
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");

        // When & Then
        assertThatCode(() -> postService.updatePost(otherSiteUser, board.getCode(), post.getId(), postUpdateRequest, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ACCESS.getMessage());
    }

    @Test
    void 게시글을_수정할_때_질문글_이라면_예외_응답을_반환한다() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");

        // When & Then
        assertThatCode(() -> postService.updatePost(siteUser, board.getCode(), questionPost.getId(), postUpdateRequest, imageFiles))
                .isInstanceOf(CustomException.class)
                .hasMessage(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
    }


    @Test
    void 게시글을_수정할_때_파일_수가_5개를_넘는다면_예외_응답을_반환한다() {
        // Given
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("자유", "title", "content");
        List<MultipartFile> moreThanFiveFiles = createMockImageFilesWithMoreThanFiveFiles();

        // When & Then
        assertThatCode(() -> postService.updatePost(siteUser, board.getCode(), post.getId(), postUpdateRequest, moreThanFiveFiles))
                .isInstanceOf(CustomException.class)
                .hasMessage(CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES.getMessage());
    }

    /**
     * 게시글 조회
     */
    @Test
    void 게시글을_찾는다() {
        // When
        PostFindResponse response = postService.findPostById(siteUser, board.getCode(), post.getId());

        // Then
        assertThat(response.id()).isEqualTo(post.getId());
    }

    @Test
    void 게시글을_찾을_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        assertThatCode(() -> postService.findPostById(siteUser, invalidBoardCode, post.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_BOARD_CODE.getMessage());
    }

    @Test
    void 게시글을_찾을_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;

        // When & Then
        assertThatCode(() -> postService.findPostById(siteUser, board.getCode(), invalidPostId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    /**
     * 게시글 삭제
     */
    @Test
    void 게시글을_삭제한다() {
        // When
        PostDeleteResponse postDeleteResponse = postService.deletePostById(siteUser, board.getCode(), post.getId());

        // Then
        assertEquals(postDeleteResponse.id(), post.getId());
    }

    @Test
    void 게시글을_삭제할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postService.deletePostById(siteUser, invalidBoardCode, post.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getMessage());
        assertThat(exception.getCode())
                .isEqualTo(ErrorCode.INVALID_BOARD_CODE.getCode());
    }

    @Test
    void 게시글을_삭제할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;
        assertThatCode(() -> postService.deletePostById(siteUser, board.getCode(), invalidPostId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    @Test
    void 게시글을_삭제할_때_자신의_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        SiteUser otherSiteUser = siteUserRepository.save(createSiteUserByEmail("hi"));

        // When & Then
        assertThatCode(() -> postService.deletePostById(otherSiteUser, board.getCode(), post.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ACCESS.getMessage());
    }

    @Test
    void 게시글을_삭제할_때_질문글_이라면_예외_응답을_반환한다() {
        assertThatCode(() -> postService.deletePostById(siteUser, board.getCode(), questionPost.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(CAN_NOT_DELETE_OR_UPDATE_QUESTION.getMessage());
    }

    /**
     * 게시글 좋아요
     */
    @Test
    void 게시글_좋아요를_등록한다() {
        // When
        PostLikeResponse postLikeResponse = postService.likePost(siteUser, board.getCode(), post.getId());

        // Then
        assertThat(postLikeResponse.isLiked()).isTrue();
    }

    @Test
    void 게시글_좋아요를_등록할_때_중복된_좋아요라면_예외_응답을_반환한다() {
        // Given
        postService.likePost(siteUser, board.getCode(), post.getId());

        // When & Then
        assertThatCode(() -> postService.likePost(siteUser, board.getCode(), post.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(DUPLICATE_POST_LIKE.getMessage());
    }

    @Test
    void 게시글_좋아요를_등록할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        assertThatCode(() -> postService.likePost(siteUser, invalidBoardCode, post.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_BOARD_CODE.getMessage());
    }

    @Test
    void 게시글_좋아요를_등록할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;

        // When & Then
        assertThatCode(() -> postService.likePost(siteUser, board.getCode(), invalidPostId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    @Test
    void 게시글_좋아요를_삭제한다() {
        // Given
        postService.likePost(siteUser, board.getCode(), post.getId());
        Long likeCount = post.getLikeCount();

        // When
        PostDislikeResponse postDislikeResponse = postService.dislikePost(siteUser, board.getCode(), post.getId());

        // Then
        assertThat(postDislikeResponse.isLiked()).isFalse();
    }

    @Test
    void 게시글_좋아요를_삭제할_때_존재하지_않는_좋아요라면_예외_응답을_반환한다() {
        // When & Then
        assertThatCode(() -> postService.dislikePost(siteUser, board.getCode(), post.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_LIKE.getMessage());
    }

    @Test
    void 게시글_좋아요를_삭제할_때_유효한_게시판이_아니라면_예외_응답을_반환한다() {
        // Given
        String invalidBoardCode = "INVALID_CODE";

        // When & Then
        assertThatCode(() -> postService.dislikePost(siteUser, invalidBoardCode, post.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_BOARD_CODE.getMessage());
    }

    @Test
    void 게시글_좋아요를_삭제할_때_유효한_게시글이_아니라면_예외_응답을_반환한다() {
        // Given
        Long invalidPostId = -1L;

        // When & Then
        assertThatCode(() -> postService.dislikePost(siteUser, board.getCode(), invalidPostId))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_POST_ID.getMessage());
    }

    private List<MultipartFile> createMockImageFilesWithMoreThanFiveFiles() {
        return List.of(
                new MockMultipartFile("file1", "test1.png", "image/png", "test image content 1".getBytes()),
                new MockMultipartFile("file2", "test2.png", "image/png", "test image content 1".getBytes()),
                new MockMultipartFile("file3", "test3.png", "image/png", "test image content 1".getBytes()),
                new MockMultipartFile("file4", "test4.png", "image/png", "test image content 1".getBytes()),
                new MockMultipartFile("file5", "test5.png", "image/png", "test image content 1".getBytes()),
                new MockMultipartFile("file6", "test6.png", "image/png", "test image content 1".getBytes())
        );
    }
}
