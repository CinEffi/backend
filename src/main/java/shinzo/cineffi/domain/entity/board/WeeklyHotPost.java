package shinzo.cineffi.domain.entity.board;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Entity
@Getter
@NoArgsConstructor
public class WeeklyHotPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_hot_post_id")
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HotPostStatusType hotPostStatus;

    public enum HotPostStatusType {
        ACTIVE, INACTIVE
    }

    @PrePersist
    public void setDefaultValues() {
        this.expiredAt = LocalDateTime.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .toLocalDate()
                .atStartOfDay();
        this.hotPostStatus = HotPostStatusType.ACTIVE;
    }
}
