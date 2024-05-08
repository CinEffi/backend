package shinzo.cineffi.domain.entity.user;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.domain.dto.LabelDTO;
import shinzo.cineffi.domain.enums.Genre;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@DynamicInsert
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAnalysis {

    private static final String[] scoreTendency = new String[] {
            null, "영화의 무덤", "무자비한 비평의 칼날", "평작 도살자", "섬세한 비평가",
            "균형을 수호하는 중재자", "실용주의자", "걱정 마세요 감독님!",
            "나는 관대하다", "영화라면 뭐든 좋아!", "만점 앵무새"
    };

    private static final String[] genreTendency = new String[] {
            "눈을 뗄 수 없는 힘과 속도의 미학",
            "미지의 영역을 탐험하는 불꽃과 같은 열정",
            "다채로운 화면 속 영원한 피터 팬",
            "웃음은 만병통치약!",
            "죄와 벌, 어둠 속에 빛나는 진실의 불꽃",
            "진실된 이야기, 깊은 울림",
            "삶의 풍경을 그리는 감성 화가",
            "사랑하는 사람들을 더욱 소중하게",
            "환상 속 신비로운 세계를 꿈꾸는 몽상가",
            "과거에서 배우는 현명함",
            "소름끼치는 공포, 요동치는 맥박",
            "아름다운 멜로디만 있다면 어디든!",
            "이목을 끌어당기는 불가사의한 매력",
            "사랑에 빠진 영혼",
            "미래와 우주의 경계를 넘나드는 개척자",
            "TV 앞에서만 느낄 수 있는 즐거움",
            "숨 막히는 긴장감, 아드레날린 폭발!",
            "전장 속 진실, 인간의 용기와 절망을 관찰하는 눈",
            "황야를 꿈꾸는 카우보이"
    };


    private static final String[] genreTendencyDetail = new String[] {
            "액션", "어드벤처", "애니메이션", "코미디",
            "범죄", "다큐멘터리", "드라마", "가족영화", "판타지",
            "역사", "공포", "음악영화", "미스터리", "로맨스",
            "SF", "TV 영화", "스릴러", "전쟁영화", "서부영화"
    };

    public final static int reviewPoint = 15;
    public final static float scorePoint = 2.0f;
    public final static int scrapPoint = 4;


    @Id
    @Column(name = "user_id")
    private Long id;

    //
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ColumnDefault("0.0")
    private Float scoreSum;

    @ColumnDefault("0")
    private Integer scoreNum;

    @ColumnDefault("0")
    private Integer scoreLabelIndex;

    @ColumnDefault("0")
    private Integer genreLabelIndex;

    @OneToMany(mappedBy = "userAnalysis")
    private List<GenreRecord> genreRecordList;

    @PrePersist
    private void initializeGenreRecord() {
        if (genreRecordList == null) {
            genreRecordList = new ArrayList<>();
            for (int i = 0; i < 19; i++) {
                genreRecordList.add(GenreRecord.builder().userAnalysis(this).genreScore(0).build());
            }
        }
    }

    // 평점 5개 이상부터 생깁니다.
    public void updateScoreTendency(Float deltaScore, Integer deltaCount) {
        this.scoreSum += deltaScore;
        this.scoreNum += deltaCount;
        this.scoreLabelIndex = (int)Math.floor(CinEffiUtils.averageScore(scoreSum, scoreNum) * 2);
    }

    public void updateGenreTendency(Genre genre, int score) {
        int challenger = genre.ordinal();
        GenreRecord genreRecord = this.genreRecordList.get(challenger);
        int newRecord = genreRecord.getGenreScore() + score;
        genreRecord.setGenreScore(newRecord);

        this.genreRecordList.set(challenger, genreRecord);

        int champion = this.genreLabelIndex;
        if (champion != challenger) {
            int oldRecord = this.genreRecordList.get(champion).getGenreScore();
            if (oldRecord < newRecord)
                this.genreLabelIndex = challenger;
        }
    }

    public LabelDTO getScoreTendency() {
        String label = null, description = null;
        if (5 <= this.scoreNum) {
            label = scoreTendency[this.scoreLabelIndex];
            if (this.scoreLabelIndex < 10) {
                Float scoreRange = this.scoreLabelIndex * 0.5f;
                description = "평균 평점이 " + scoreRange + " 부터 " + (scoreRange + 0.5f) + "사이에 분포";
            }
            else description = "평균 평점 5.0";
        }
        return LabelDTO.builder().label(label).description(description).build();
    }

    public LabelDTO getGenreTendency() {
        String label = null, description = null;
        if (10 <= genreRecordList.get(this.genreLabelIndex).getGenreScore()) {
            label = genreTendency[this.genreLabelIndex];
            description = genreTendencyDetail[this.genreLabelIndex] + " 장르 선호 경향";
        }
        return LabelDTO.builder().label(label).description(description).build();
    }

}