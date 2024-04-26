package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.DailyMovie;

public interface DailyBoxOfficeRepository extends JpaRepository<DailyMovie, Long> {

    //특정 날짜의 데이터를 삭제하는 메서드
    void deleteByTargetDt(String targetDt);
}
