/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.Room;
import entity.RoomAllocationReport;
import entity.RoomType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomStatusEnum;
import util.exception.CannotGetTodayDateException;
import util.exception.RoomAllocationReportNotFoundException;
import util.exception.RoomTypeNotFoundException;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class AllocationSessionBean implements AllocationSessionBeanRemote, AllocationSessionBeanLocal {

    @EJB
    private RoomTypeSessionBeanLocal roomTypeSessionBean;

    @EJB
    private HandleDateTimeSessionBeanLocal handleDateTimeSessionBean;

    @EJB
    private RoomAllocationReportSessionBeanLocal roomAllocationReportSessionBean;

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    @Schedules({
        @Schedule(dayOfWeek = "*"),
        @Schedule(hour = "2")
    })
    public void allocateRoomToCurrentDayReservations() throws CannotGetTodayDateException, RoomAllocationReportNotFoundException {

        RoomAllocationReport roomAllocationReport = roomAllocationReportSessionBean.createRoomAllocationReport();

        Query currentDayReservationsQuery = em.createQuery("SELECT r FROM Reservation r WHERE r.startDate = :inStartDate");
        currentDayReservationsQuery.setParameter("inStartDate", handleDateTimeSessionBean.getTodayDate());
        List<Reservation> reservations = currentDayReservationsQuery.getResultList();

        for (Reservation reservation : reservations) {
            allocateRoom(reservation, roomAllocationReport);
        }

    }

    //The following method is never called in our implementation of the business process.
    //It is purely to demonstrate allocation logic and has its restrictions. See docs.
    @Override
    public HashMap<Reservation, Room> allocateRoomToFutureDayReservations(Date date) throws CannotGetTodayDateException, RoomAllocationReportNotFoundException {

        HashMap<Reservation, Room> allocation = new HashMap<>();
        
        RoomAllocationReport roomAllocationReport = roomAllocationReportSessionBean.createRoomAllocationReportForFuture(date);

        Query query = em.createQuery("SELECT r FROM Reservation r WHERE r.startDate = :inStartDate");
        query.setParameter("inStartDate", date);
        List<Reservation> reservations = query.getResultList();

        for (Reservation reservation : reservations) {
            Room room = allocateRoom(reservation, roomAllocationReport);
            allocation.put(reservation, room);
        }
        
        return allocation;
    }

    @Override
    public Room allocateRoom(Reservation reservation, RoomAllocationReport roomAllocationReport) throws CannotGetTodayDateException {

        //Can find same type room
        RoomType roomType = reservation.getRoomType();

        Query availableSameTypeRoomsQuery = em.createQuery("SELECT r FROM Room r "
                + "WHERE r.status = util.enumeration.RoomStatusEnum.AVAILABLE "
                + "AND r.roomType = :inRoomType "
                + "AND NOT r.disabled");
        availableSameTypeRoomsQuery.setParameter("inRoomType", roomType);
        List<Room> temp1 = availableSameTypeRoomsQuery.setMaxResults(1).getResultList();

        if (!temp1.isEmpty()) {

            Room availableSameTypeRoom = temp1.get(0);

            availableSameTypeRoom.setCurrentReservation(reservation);
            availableSameTypeRoom.setStatus(RoomStatusEnum.UNAVAILABLE);

            return availableSameTypeRoom;

        } else {

            //Can find different type room of higher quality

            while (roomType.getNextHigherRoomType() != null) {

                Query availableDifferentTypeRoomsQuery = em.createQuery("SELECT r FROM Room r "
                        + "WHERE r.status = util.enumeration.RoomStatusEnum.AVAILABLE "
                        + "AND r.roomType = :inRoomType "
                        + "AND NOT r.disabled");
                try {
                availableDifferentTypeRoomsQuery.setParameter("inRoomType", roomTypeSessionBean.retrieveRoomTypeByName(roomType.getNextHigherRoomType()));
                } catch (RoomTypeNotFoundException ex) {
                    break;
                }
                List<Room> temp2 = availableDifferentTypeRoomsQuery.setMaxResults(1).getResultList();

                if (!temp2.isEmpty()) {

                    Room availableDifferentTypeRoom = temp2.get(0);

                    availableDifferentTypeRoom.setCurrentReservation(reservation);
                    availableDifferentTypeRoom.setStatus(RoomStatusEnum.UNAVAILABLE);

                    String notificationMessage = "Reservation ID: " + reservation.getReservationId() + " has been upgraded from "
                            + reservation.getRoomType().getName() + " to room: " + availableDifferentTypeRoom.getNumber()
                            + " of type: " + availableDifferentTypeRoom.getRoomType().getName();
                    roomAllocationReport.getNoAvailableRoomUpgrade().add(notificationMessage);

                    return availableDifferentTypeRoom;

                } else {

                    try {

                        roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomType.getNextHigherRoomType());

                    } catch (RoomTypeNotFoundException ex) {

                        break;

                    }

                }
            }

            //Cannot find any rooms
            String notificationMessage = "Reservation ID: " + reservation.getReservationId() + " cannot be allocated a room";
            roomAllocationReport.getNoAvailableRoomNoUpgrade().add(notificationMessage);

            return null;

        }

    }

}
