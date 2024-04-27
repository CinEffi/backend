package shinzo.cineffi.redis;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.SuccessMsg;

/**
 * Redis 동작 Controller
 *
 * @author : lee
 * @fileName : RedisController
 * @since : 3/29/24
 */
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    /**
     * Redis의 값을 조회합니다.
     *
     * @param redisDto
     * @return
     */
    @GetMapping("/getValue")
    public ResponseEntity<ResponseDTO<String>> getValue(@RequestBody RedisDto redisDto) {
        String result = redisService.getValue(redisDto.getKey());
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(result)
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Redis의 값을 추가/수정합니다.
     *
     * @param redisDto
     * @return
     */
    @PostMapping("/setValue")
    public ResponseEntity<ResponseDTO<String>> setValue(@RequestBody RedisDto redisDto) {
        String key = redisDto.getKey();
        redisService.setValues(key, redisDto.getValue());
        String value = redisService.getValue(key);

        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(key + ":" + value)
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Redis의 key 값을 기반으로 row를 제거합니다.
     * @param redisDto
     * @return
     */
    @PostMapping("/deleteValue")
    public ResponseEntity<ResponseDTO<String>> deleteRow(@RequestBody RedisDto redisDto) {
        String key = redisDto.getKey();
        redisService.deleteValue(key);
        String deleted = redisService.getValue(key);
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result("delete " + key + " : " + (deleted == "" ? "success" : "fail"))
                .build();
        return ResponseEntity.ok(responseDTO);
    }
}

