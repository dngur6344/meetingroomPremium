package meetingroom;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Review_table")
public class Review {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long roomId;
    private String userId;
    private Integer score;

    @PrePersist
    public void onPrePersist(){
        RoomChecked roomChecked = new RoomChecked();
        BeanUtils.copyProperties(this, roomChecked);
        roomChecked.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        meetingroom.external.Room room = new meetingroom.external.Room();
        room.setId(roomId);
        // mappings goes here
        String result=ReviewApplication.applicationContext.getBean(meetingroom.external.RoomService.class)
            .search(room);
        if(result.equals("valid")){
            System.out.println("Success!");
        }
        else{
            System.out.println("FAIL!! There is no room!!");
            Exception ex = new Exception();
            ex.notify();
        }
    }
    @PostPersist
    public void onPostPersist(){
        Registered registered = new Registered();
        BeanUtils.copyProperties(this, registered);
        registered.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }




}
