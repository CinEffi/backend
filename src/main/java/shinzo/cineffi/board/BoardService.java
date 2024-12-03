package shinzo.cineffi.board;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.board.repository.CommentRepository;
import shinzo.cineffi.board.repository.PostRepository;
import shinzo.cineffi.board.repository.PostTagRepository;
import shinzo.cineffi.board.repository.WeeklyHotPostRepository;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.board.Comment;
import shinzo.cineffi.domain.entity.board.Post;
import shinzo.cineffi.domain.entity.board.PostTag;
import shinzo.cineffi.domain.entity.board.WeeklyHotPost;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.response.PageResponse;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static shinzo.cineffi.domain.entity.board.WeeklyHotPost.HotPostStatusType.ACTIVE;
import static shinzo.cineffi.exception.message.ErrorMsg.*;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final WeeklyHotPostRepository weeklyHotPostRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public PageResponse<GetPostsDto> getPostList(Pageable pageable) {
        Page<Post> pagedPosts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<WeeklyHotPost> weeklyHotPosts = weeklyHotPostRepository.findAllByHotPostStatus(ACTIVE);

        ArrayList result = new ArrayList<>();

        for (Post post : pagedPosts) {
            Boolean isHot = weeklyHotPosts
                    .stream().map(hotPost -> hotPost.getPost()).toList()
                    .contains(post);

            result.add(GetPostsDto.builder()
                    .postId(EncryptUtil.LongEncrypt(post.getId()))
                    .title(post.getTitle())
                    .view(post.getView())
                    .createdAt(post.getCreatedAt())
                    .user(new UserDto().from(post.getWriter()))
                    .tags(List.of())
                    .likeNumber(post.getLikeNumber())
                    .commentNumber(post.getCommentNumber())
                    .isHotPost(isHot)
                    .build());
        }

        PageResponse pageResponse = new PageResponse().from(pagedPosts, result, pageable);

        return pageResponse;
    }

    @Transactional
    public GetPostDto getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        UserDto userDto = new UserDto().from(post.getWriter());

        // 조회수 증가
        post.increaseView();

        return new GetPostDto().from(post, userDto);
    }

    @Transactional(readOnly = true)
    public PageResponse<GetCommentsDto> getCommentList(Long postId, Pageable pageable) {
        Page<Comment> pagedCommentList = commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId, pageable);

        List<GetCommentsDto> getCommentsDtos = pagedCommentList.stream().map(comment -> {
            UserDto userDto = new UserDto().from(comment.getWriter());
            return new GetCommentsDto().from(comment, userDto);
        }).toList();

        return new PageResponse<>(
                getCommentsDtos,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pagedCommentList.getTotalElements(),
                pagedCommentList.getTotalPages(),
                pagedCommentList.hasNext());
    }

    @Transactional
    public void postPostComment(Long decryptedPostId, Long userId, String content) {
        // 유저 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));

        // 게시글 엔티티 조회
        Post post = postRepository.findById(decryptedPostId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        // 저장
        commentRepository.save(Comment.builder()
                                .post(post)
                                .writer(user)
                                .content(content)
                                .build());

        // 게시글의 댓글 수 증가
        post.increaseCommentNumber();
    }

    @Transactional
    public void patchPost(Long postId, Long loginUserId) {
        User user = userRepository.findById(loginUserId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        // 해당 유저가 해당 게시글 작성자인지 검사
        if (!post.getWriter().equals(user))
            throw new CustomException(ACCESS_DENIED);

        // 삭제
        post.setIsDelete(true);
    }

    @Transactional
    /* 게시글 태그 설정 메서드 : 이전 거는 삭제되고 인자에 지정한대로 저장됨 */
    public void submitPost(Long userId, String title, String content, List<String> tags) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Post post = postRepository.save(Post.builder()
                .title(title)
                .content(content)
                .writer(user)
                .build());
        addTags(post, tags); // 태그 추가
    }

    @Transactional
    public void addTags(Post post, List<String> tagList) {
        // 이미 존재하는 태그가 있는지 확인, 있다면 삭제
        List<PostTag> existingTags = postTagRepository.findAllByPost(post);
        if (post.getTags() != null || !existingTags.isEmpty()) {
            existingTags.stream().forEach(tag -> tag.setIsDelete(true));
            post.clearTags();
        }

        // 인자로 받은 태그 리스트를 게시글 태그로 새로 지정
        List<PostTag> postTagList = tagList.stream().map(tag -> {
            PostTag postTag = PostTag.builder()
                    .post(post)
                    .content(tag)
                    .build();
            postTagRepository.save(postTag);
            return postTag;
        }).toList();
        post.setTags(postTagList);
    }


}
