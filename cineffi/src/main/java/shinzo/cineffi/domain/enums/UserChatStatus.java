package shinzo.cineffi.domain.enums;
/***********************************************************************
 * 데이터가 정규화되지 못한것 같아서, 분리해볼까 하는 생각도 있지만 아직은 계획이 없습니다. *
 ***********************************************************************/

public enum UserChatStatus {
    BANNED,
    MUTED,
    LEAVED,
    JOINED,
    OPERATOR_JOINED,
    OPERATOR_LEAVED,
    OWNER_JOINED,
    OWNER_LEAVED
}