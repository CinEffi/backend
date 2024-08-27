package shinzo.cineffi.chat;

import lombok.Builder;
import lombok.Getter;

enum QUERY_TYPE {
    NONE,
    TAG,
    TITLE
}

enum QUERY_SORT {
    ACCURACY,
    TIME_ASC,
    TIME_DESC,
    NUMBER_ASC,
    NUMBER_DESC
}

@Getter
@Builder
public class ChatQuery {
    private QUERY_TYPE queryType;
    private String query;
    private QUERY_SORT qureySort;
}