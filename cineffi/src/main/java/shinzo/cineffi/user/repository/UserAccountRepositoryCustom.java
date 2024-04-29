package shinzo.cineffi.user.repository;

import shinzo.cineffi.domain.entity.user.UserAccount;

import java.util.Optional;

public interface UserAccountRepositoryCustom {
    Optional<UserAccount> findByIdAndFetchUserToken(Long id);


    Optional<String> selectTokenBymemberNo(Long userSequenceValue);
}
