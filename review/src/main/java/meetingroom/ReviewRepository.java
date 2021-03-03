package meetingroom;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ReviewRepository extends PagingAndSortingRepository<Review, Long>{
    public List<Review> findAllByRoomId(Long roomId);
}