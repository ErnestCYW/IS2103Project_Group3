/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.Room;
import entity.RoomAllocationReport;
import java.util.Date;
import javax.ejb.Local;
import util.exception.CannotGetTodayDateException;
import util.exception.RoomAllocationReportNotFoundException;

/**
 *
 * @author ernestcyw
 */
@Local
public interface AllocationSessionBeanLocal {

    public void allocateRoomToCurrentDayReservations() throws CannotGetTodayDateException, RoomAllocationReportNotFoundException;

    public void allocateRoomToFutureDayReservations(Date date) throws CannotGetTodayDateException, RoomAllocationReportNotFoundException;

    public Room allocateRoom(Reservation reservation, RoomAllocationReport roomAllocationReport) throws CannotGetTodayDateException, RoomAllocationReportNotFoundException;

}
