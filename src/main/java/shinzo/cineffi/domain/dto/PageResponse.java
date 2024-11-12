package shinzo.cineffi.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {
    @Schema(description = "데이터")
    private List<T> contents;

    @Schema(description = "페이지 넘버")
    private int page;

    @Schema(description = "페이지 사이즈")
    private int size;

    @Schema(description = "전체 데이터 개수")
    private long totalElements;

    @Schema(description = "전체 페이지 개수")
    private int totalPages;

    @Schema(description = "다음 페이지 유무")
    private boolean hasNextPage;

    public static <T> PageResponse<T> from(Page<T> page, Pageable pageable) {
        return new PageResponse<>(
                page.getContent(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }

    public static <T> PageResponse<T> from(Page<T> page, List<T> contents, Pageable pageable) {
        return new PageResponse<>(
                contents,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }

}

