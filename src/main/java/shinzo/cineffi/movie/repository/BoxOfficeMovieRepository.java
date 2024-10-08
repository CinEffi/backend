package shinzo.cineffi.movie.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.BoxOfficeMovie;

public interface BoxOfficeMovieRepository extends JpaRepository<BoxOfficeMovie, Long> {

    //특정 날짜의 데이터를 삭제하는 메서드\
    @Transactional
    void deleteByTargetDt(String targetDt);
}
