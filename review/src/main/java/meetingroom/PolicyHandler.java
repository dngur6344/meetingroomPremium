package meetingroom;

import meetingroom.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PolicyHandler{
    @Autowired
    ReviewRepository reviewRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    @Transactional
    public void wheneverEnded_UpdateConference(@Payload Deleted deleted){

        if(deleted.isMe()){
            List<Review> reviewList = reviewRepository.findAllByRoomId(deleted.getId());
            System.out.println("##### listener  : " + deleted.toJson());
            for(Review review:reviewList){
                reviewRepository.deleteById(review.getId());
            }
        }
    }

}
