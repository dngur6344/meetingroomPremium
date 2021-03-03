package meetingroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@RestController
 public class RoomController {
 @Autowired
 RoomRepository roomRepository;

 @RequestMapping(value="/rooms/check")
 public String search(@RequestBody Room room){
  Optional<Room> result=roomRepository.findById(room.getId());
  if(result.isPresent()){
    return "valid";
   }
  return "invalid";
 }
}
