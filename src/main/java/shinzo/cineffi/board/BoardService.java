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
import shinzo.cineffi.domain.dto.PageResponse;
import shinzo.cineffi.domain.dto.UserDto;
import shinzo.cineffi.domain.entity.board.Post;
import shinzo.cineffi.domain.entity.board.WeeklyHotPost;

import java.util.ArrayList;
import java.util.List;

import static shinzo.cineffi.domain.entity.board.WeeklyHotPost.HotPostStatusType.ACTIVE;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final PostRepository postRepository;
    private final WeeklyHotPostRepository weeklyHotPostRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetPostDto> getPostList(Pageable pageable) {
        Page<Post> pagedPosts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<WeeklyHotPost> weeklyHotPosts = weeklyHotPostRepository.findAllByHotPostStatus(ACTIVE);

        ArrayList result = new ArrayList<>();

        for (Post post : pagedPosts) {
            Boolean isHot = weeklyHotPosts
                    .stream().map(hotPost -> hotPost.getPost()).toList()
                    .contains(post);

            result.add(GetPostDto.builder()
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
}
