/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.Room;
import entity.RoomAllocationReport;
import javax.ejb.Local;
import util.exception.CannotGetTodayDateException;

/**
 *
 * @author ernestcyw
 */
@Local
public interface AllocationSessionBeanLocal {

    public void allocateRoomToCurrentDayReservations() throws CannotGetTodayDateException;

    public RoomAllocationReport viewTodayRoomAllocationReport() throws CannotGetTodayDateException;

    public Room allocateRoom(Reservation reservation) throws CannotGetTodayDateException;
    
}
