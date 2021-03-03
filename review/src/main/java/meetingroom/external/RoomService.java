
package meetingroom.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="room", url="${api.room.url}")
public interface RoomService {

    @RequestMapping(method= RequestMethod.GET, path="/rooms/check")
    public String search(@RequestBody Room room);

}