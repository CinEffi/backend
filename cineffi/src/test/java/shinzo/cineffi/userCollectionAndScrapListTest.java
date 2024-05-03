package shinzo.cineffi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import shinzo.cineffi.domain.dto.GetCollectionRes;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.review.ReviewService;
import shinzo.cineffi.review.repository.ReviewRepository;

@SpringBootTest(classes=CineffiApplication.class)
public class userCollectionAndScrapListTest {

    @Autowired private ReviewService reviewService;
    @Test
    void test() {
        GetCollectionRes userReviewList = reviewService.getUserReviewList(3L, Pageable.unpaged());
        System.out.println("userReviewList = " + userReviewList.getCollection().get(0).getUserScore());
    }
}
