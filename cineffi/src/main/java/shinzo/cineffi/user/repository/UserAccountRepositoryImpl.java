package shinzo.cineffi.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import shinzo.cineffi.domain.entity.user.UserAccount;

import java.util.Optional;

@Repository
public class UserAccountRepositoryImpl implements UserAccountRepositoryCustom{
    private final EntityManager entityManager;

    public UserAccountRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UserAccount> findByIdAndFetchUserToken(Long id) {
        String jpql = "SELECT ua FROM UserAccount ua WHERE ua.id = :id";
        try {
            UserAccount userAccount = entityManager.createQuery(jpql, UserAccount.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(userAccount);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> selectTokenBymemberNo(Long userSequenceValue) {
        String jpql = "SELECT ua.userToken FROM UserAccount ua WHERE ua.id = :userId";
        return entityManager.createQuery(jpql, String.class)
                .setParameter("userId", userSequenceValue)
                .getResultList()
                .stream()
                .findFirst();
    }


}