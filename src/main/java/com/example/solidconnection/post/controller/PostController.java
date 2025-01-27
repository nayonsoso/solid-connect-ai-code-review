package com.example.solidconnection.post.controller;

import com.example.solidconnection.custom.auth.argumentresolver.AuthorizedUser;
import com.example.solidconnection.post.dto.PostCreateRequest;
import com.example.solidconnection.post.dto.PostCreateResponse;
import com.example.solidconnection.post.dto.PostDeleteResponse;
import com.example.solidconnection.post.dto.PostDislikeResponse;
import com.example.solidconnection.post.dto.PostFindResponse;
import com.example.solidconnection.post.dto.PostLikeResponse;
import com.example.solidconnection.post.dto.PostUpdateRequest;
import com.example.solidconnection.post.dto.PostUpdateResponse;
import com.example.solidconnection.post.service.PostService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/communities")
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/{code}/posts")
    public ResponseEntity<?> createPost(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("code") String code,
            @Valid @RequestPart("postCreateRequest") PostCreateRequest postCreateRequest,
            @RequestParam(value = "file", required = false) List<MultipartFile> imageFile) {

        if (imageFile == null) {
            imageFile = Collections.emptyList();
        }
        PostCreateResponse post = postService.createPost(siteUser, code, postCreateRequest, imageFile);
        return ResponseEntity.ok().body(post);
    }

    @PatchMapping(value = "/{code}/posts/{post_id}")
    public ResponseEntity<?> updatePost(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId,
            @Valid @RequestPart("postUpdateRequest") PostUpdateRequest postUpdateRequest,
            @RequestParam(value = "file", required = false) List<MultipartFile> imageFile) {

        if (imageFile == null) {
            imageFile = Collections.emptyList();
        }
        PostUpdateResponse postUpdateResponse = postService.updatePost(
                siteUser, code, postId, postUpdateRequest, imageFile);
        return ResponseEntity.ok().body(postUpdateResponse);
    }


    @GetMapping("/{code}/posts/{post_id}")
    public ResponseEntity<?> findPostById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId) {

        PostFindResponse postFindResponse = postService.findPostById(siteUser, code, postId);
        return ResponseEntity.ok().body(postFindResponse);
    }

    @DeleteMapping(value = "/{code}/posts/{post_id}")
    public ResponseEntity<?> deletePostById(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId) {

        PostDeleteResponse postDeleteResponse = postService.deletePostById(siteUser, code, postId);
        return ResponseEntity.ok().body(postDeleteResponse);
    }

    @PostMapping(value = "/{code}/posts/{post_id}/like")
    public ResponseEntity<?> likePost(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId
    ) {

        PostLikeResponse postLikeResponse = postService.likePost(siteUser, code, postId);
        return ResponseEntity.ok().body(postLikeResponse);
    }

    @DeleteMapping(value = "/{code}/posts/{post_id}/like")
    public ResponseEntity<?> dislikePost(
            @AuthorizedUser SiteUser siteUser,
            @PathVariable("code") String code,
            @PathVariable("post_id") Long postId
    ) {

        PostDislikeResponse postDislikeResponse = postService.dislikePost(siteUser, code, postId);
        return ResponseEntity.ok().body(postDislikeResponse);
    }
}
