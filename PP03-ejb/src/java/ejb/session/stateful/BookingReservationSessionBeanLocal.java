/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateful;

import entity.Room;
import entity.RoomType;
import java.util.Date;
import java.util.List;
import javax.ejb.Local;
import util.exception.CannotGetWalkInPriceException;
import util.exception.CheckinGuestException;
import util.exception.ReserveRoomException;
import util.exception.RoomTypeNotFoundException;

/**
 *
 * @author elgin
 */
@Local
public interface BookingReservationSessionBeanLocal {

    public Integer getNumOfAvailableRoomsForRoomType(Long roomTypeId, Date checkinDate, Date checkoutDate) throws RoomTypeNotFoundException;

    public Double getWalkInPriceForRoomType(RoomType roomType, Date checkinDate, Date checkoutDate) throws CannotGetWalkInPriceException;

    public void saveSearchResults(String roomTypeName, Integer numOfAvailablerooms);

    public List<Long> walkInReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate) throws ReserveRoomException;

    public List<Room> checkinGuest(Long guestId) throws CheckinGuestException;

    public void checkoutGuest(Long guestId);
    
}
