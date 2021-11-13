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
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomStatusEnum;
import util.exception.CannotGetTodayDateException;
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

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @Schedule(hour = "2")
    @Override
    public void allocateRoomToCurrentDayReservations() throws CannotGetTodayDateException {

        roomAllocationReportSessionBean.createRoomAllocationReport();

        Query currentDayReservationsQuery = em.createQuery("SELECT r FROM Reservation r WHERE r.startDate = :inStartDate");
        currentDayReservationsQuery.setParameter("inStartDate", handleDateTimeSessionBean.getTodayDate());
        List<Reservation> reservations = currentDayReservationsQuery.getResultList();

        for (Reservation reservation : reservations) {
            allocateRoom(reservation);
        }

    }

    @Override
    public Room allocateRoom(Reservation reservation) throws CannotGetTodayDateException {

        RoomType roomType = reservation.getRoomType();

        Query availableSameTypeRoomsQuery = em.createQuery("SELECT r FROM Room r "
                + "WHERE r.status = util.enumeration.RoomStatusEnum.AVAILABLE "
                + "AND r.roomType = :inRoomType "
                + "AND NOT r.disabled");
        availableSameTypeRoomsQuery.setParameter("inRoomType", roomType);
        Room availableSameTypeRoom = (Room) availableSameTypeRoomsQuery.getSingleResult();

        if (availableSameTypeRoom != null) {

            availableSameTypeRoom.setCurrentReservation(reservation);
            availableSameTypeRoom.setStatus(RoomStatusEnum.UNAVAILABLE);

            return availableSameTypeRoom;

        } else {

            RoomAllocationReport roomAllocationReport = roomAllocationReportSessionBean
                    .viewRoomAllocationReportByDate(handleDateTimeSessionBean.getTodayDate());

            while (roomType.getNextHigherRoomType() != null) {

                Query availableDifferentTypeRoomsQuery = em.createQuery("SELECT r FROM Room r "
                        + "WHERE r.status = util.enumeration.RoomStatusEnum.AVAILABLE "
                        + "AND r.roomType = :inRoomType "
                        + "AND NOT r.disabled");
                availableDifferentTypeRoomsQuery.setParameter("inRoomType", roomType.getNextHigherRoomType());
                Room availableDifferentTypeRoom = (Room) availableDifferentTypeRoomsQuery.getSingleResult();

                if (availableDifferentTypeRoom != null) {

                    availableDifferentTypeRoom.setCurrentReservation(reservation);
                    availableDifferentTypeRoom.setStatus(RoomStatusEnum.UNAVAILABLE);

                    String notificationMessage = reservation.getReservationId() + " has been upgraded from "
                            + reservation.getRoomType() + " to room: " + availableDifferentTypeRoom.getNumber()
                            + " of type: " + availableDifferentTypeRoom.getRoomType();
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

            String notificationMessage = reservation.getReservationId() + " cannot be allocated a room";
            roomAllocationReport.getNoAvailableRoomNoUpgrade().add(notificationMessage);

            return null;
            
        }

    }

    @Override
    public RoomAllocationReport viewRoomAllocationReport() throws CannotGetTodayDateException {
        return roomAllocationReportSessionBean.viewRoomAllocationReportByDate(handleDateTimeSessionBean.getTodayDate());
    }

}
