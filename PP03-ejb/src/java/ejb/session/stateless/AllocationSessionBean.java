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
    
    //Allocate Room
    public Room allocateRoom(Reservation reservation) {
        //Check all roooms if available
        Query availableSameTypeRoomsQuery = em.createQuery("SELECT r FROM rooms r WHERE"
                + " r.status = util.enumeration.RoomStatusEnum.AVAILABLE AND r.roomType = :inRoomType");
        availableSameTypeRoomsQuery.setParameter("inRoomType", reservation.getRoomType());
        Room availableRoom = (Room) availableSameTypeRoomsQuery.getSingleResult();
        
        if (availableRoom != null) {
            availableRoom.setCurrentReservation(reservation);
            reservation.setRoom(availableRoom);
            
            return availableRoom;
        } else {
            
            
            
        }
    }
    
    
    //View Room Allocation Report

    public void persist(Object object) {
        em.persist(object);
    }
}
