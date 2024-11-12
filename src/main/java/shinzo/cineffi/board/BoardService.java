package shinzo.cineffi.board;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.board.repository.PostRepository;
import shinzo.cineffi.board.repository.WeeklyHotPostRepository;
import shinzo.cineffi.domain.dto.GetPostDto;
import shinzo.cineffi.domain.dto.GetPostsDto;
import shinzo.cineffi.domain.dto.PageResponse;
import shinzo.cineffi.domain.dto.UserDto;
import shinzo.cineffi.domain.entity.board.Post;
import shinzo.cineffi.domain.entity.board.WeeklyHotPost;
import shinzo.cineffi.exception.CustomException;

import java.util.ArrayList;
import java.util.List;

import static shinzo.cineffi.domain.entity.board.WeeklyHotPost.HotPostStatusType.ACTIVE;
import static shinzo.cineffi.exception.message.ErrorMsg.POST_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final PostRepository postRepository;
    private final WeeklyHotPostRepository weeklyHotPostRepository;

    @Transactional(readOnly = true)
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
                    .createdAt(post.getCreatedAt())
                    .user(new UserDto().from(post.getWriter()))
                    .tags(List.of())
                    .likeNumber(post.getLikeNumber())
                    .commentNumber(post.getCommentNumber())
                    .isHotPost(isHot)
                    .build());
        }

        PageResponse pageResponse = new PageResponse().setPagingInfo(pagedPosts);
        pageResponse.setContents(result);

        return pageResponse;
    }

    @Transactional
    public GetPostDto getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        UserDto userDto = new UserDto().from(post.getWriter());

        return new GetPostDto().from(post, userDto);
    }
}
