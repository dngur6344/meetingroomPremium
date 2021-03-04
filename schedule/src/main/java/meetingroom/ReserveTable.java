package meetingroom;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="ReserveTable_table")
public class ReserveTable {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long roomId;
        private Long reserveId;
        private String userId;
        private String status;
        private Integer floor;
        private Integer score;
        private String comment;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
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
        public Long getReserveId() {
            return reserveId;
        }

        public void setReserveId(Long reserveId) {
            this.reserveId = reserveId;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
