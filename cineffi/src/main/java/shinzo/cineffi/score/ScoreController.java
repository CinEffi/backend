package shinzo.cineffi.score;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.dto.ScoreMovieDTO;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/score")
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/movie")
    public ResponseEntity<ResponseDTO<?>> scoreMovie (@RequestBody ScoreMovieDTO scoreMovieDTO) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        scoreService.scoreMovie(scoreMovieDTO.getScore(), scoreMovieDTO.getMovieId(), userId);
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }
}