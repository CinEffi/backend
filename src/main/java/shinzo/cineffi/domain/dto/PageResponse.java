package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> contents;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNextPage;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }

    public PageResponse setPagingInfo(Page<T> page) {
        this.page = page.getNumber();
        this.size = page.getNumberOfElements();
        this.totalElements = page.getTotalElements();
        this.totalElements = page.getTotalPages();
        this.hasNextPage = page.hasNext();

        return this;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }
}

