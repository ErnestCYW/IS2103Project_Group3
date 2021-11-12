/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.Room;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomRateTypeEnum;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class AllocationSessionBean implements AllocationSessionBeanRemote, AllocationSessionBeanLocal {

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;
    
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    //Allocate Room to Current Day Reservations
    //Ran on timer
    //Calls allocate room 
    //Create the allocation report for the day
    
    //Allocate Room
    public Room allocateRoom(Reservation reservation) {
        //Check all roooms if available
        Query availableSameTypeRoomsQuery = em.createQuery("SELECT r FROM rooms r WHERE"
                + " r.status = util.enumeration.RoomStatusEnum.AVAILABLE AND r.roomType = :inRoomType");
        availableSameTypeRoomsQuery.setParameter("inRoomType", reservation.getRoomType());
        Room availableSameTypeRoom = (Room) availableSameTypeRoomsQuery.getSingleResult();
        
        if (availableSameTypeRoom != null) {
            availableSameTypeRoom.setCurrentReservation(reservation);
            reservation.setRoom(availableSameTypeRoom);
            
            return availableSameTypeRoom;
        } else {
            int numberRoomTypes = RoomRateTypeEnum.values().length;
            int index = RoomRateTypeEnum.valueOf(reservation.getRoomType().toString()).ordinal() + 1;
            
            while (index < numberRoomTypes) {
                Query availableDifferentTypeRoomsQuery = em.createQuery("SELECT r FROM rooms r WHERE"
                + " r.status = util.enumeration.RoomStatusEnum.AVAILABLE AND r.roomType = :inRoomType");
                availableDifferentTypeRoomsQuery.setParameter("inRoomType", RoomRateTypeEnum.values()[index]);
                
                Room availableDifferentTypeRoom = (Room) availableDifferentTypeRoomsQuery.getSingleResult();
                
                if (availableDifferentTypeRoom != null) {
                    availableDifferentTypeRoom.setCurrentReservation(reservation);
                    reservation.setRoom(availableDifferentTypeRoom);
                    
                    //Add to allocation report can assign different room
                    
                    return availableDifferentTypeRoom;
                }
                
                index++;
            }
            
            //Add to allocation report cannot assign different room
            
            return null;
        }
    }
    
    //View Room Allocation Report

    public void persist(Object object) {
        em.persist(object);
    }
}
