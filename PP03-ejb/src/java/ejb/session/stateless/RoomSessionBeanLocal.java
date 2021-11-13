/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Room;
import java.util.List;
import javax.ejb.Local;
import util.exception.InputDataValidationException;
import util.exception.RoomNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Local
public interface RoomSessionBeanLocal {

    public Room createNewRoom(Long roomTypeId, Room newRoomEntity) throws UnknownPersistenceException, InputDataValidationException, RoomTypeNotFoundException;

    public Room updateRoom(Long roomTypeId, Room roomEntity) throws RoomTypeNotFoundException, InputDataValidationException, RoomNotFoundException;

    public void deleteRoom(Long roomId) throws RoomNotFoundException;

    public List<Room> viewAllRooms();

    public Room retrieveRoomByRoomNumber(String roomNum) throws RoomNotFoundException;
    
}
