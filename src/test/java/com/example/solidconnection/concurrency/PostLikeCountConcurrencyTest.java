package com.example.solidconnection.concurrency;

import com.example.solidconnection.board.domain.Board;
import com.example.solidconnection.board.repository.BoardRepository;
import com.example.solidconnection.post.domain.Post;
import com.example.solidconnection.post.repository.PostRepository;
import com.example.solidconnection.post.service.PostService;
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
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestContainerSpringBootTest
@DisplayName("게시글 좋아요 동시성 테스트")
class PostLikeCountConcurrencyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Value("${view.count.scheduling.delay}")
    private int SCHEDULING_DELAY_MS;

    private int THREAD_NUMS = 1000;
    private int THREAD_POOL_SIZE = 200;
    private int TIMEOUT_SECONDS = 10;

    private Post post;
    private Board board;
    private SiteUser siteUser;

    @BeforeEach
    void setUp() {
        board = boardRepository.save(createBoard());
        siteUser = siteUserRepository.save(createSiteUser());
        post = postRepository.save(createPost(board, siteUser));
        createSiteUsers();
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

    private void createSiteUsers() {
        List<SiteUser> siteUsers = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            siteUsers.add(new SiteUser(
                    "email" + i,
                    "nickname",
                    "profileImageUrl",
                    "1999-01-01",
                    PreparationStatus.CONSIDERING,
                    Role.MENTEE,
                    Gender.MALE
            ));
        }
        siteUserRepository.saveAll(siteUsers);
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

    @Test
    void 게시글_좋아요_동시성_문제를_해결한다() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_NUMS);

        Long likeCount = postRepository.getById(post.getId()).getLikeCount();
        List<SiteUser> users = siteUserRepository.findAll();

        for (int i = 0; i < THREAD_NUMS; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    SiteUser currentUser = users.get(index);
                    postService.likePost(currentUser, board.getCode(), post.getId());
                    postService.dislikePost(currentUser, board.getCode(), post.getId());
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        doneSignal.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!terminated) {
            System.err.println("ExecutorService did not terminate in the expected time.");
        }

        Thread.sleep(100);
        assertEquals(likeCount, postRepository.getById(post.getId()).getLikeCount());
    }
}
