package shinzo.cineffi.domain.response;

import lombok.Builder;
import lombok.Data;
import shinzo.cineffi.domain.dto.ScrapDto;

import java.util.List;

@Data
@Builder
public class GetScrapRes {
    private int totalScrapNum;
    private List<ScrapDto> scrapList;
}
