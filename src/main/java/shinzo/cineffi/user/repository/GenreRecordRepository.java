package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.GenreRecord;

public interface GenreRecordRepository extends JpaRepository<GenreRecord, Long> { }