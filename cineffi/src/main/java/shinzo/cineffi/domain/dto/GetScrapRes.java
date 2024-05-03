package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetScrapRes {
    private int totalPageNum;
    private List<ScrapDto> scrapList;
}
