/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.Room;
import javax.ejb.Stateless;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class AllocationSessionBean implements AllocationSessionBeanRemote, AllocationSessionBeanLocal {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    //Allocate Room to Current Day Reservations
    //Ran on timer
    //Calls allocate room 
    
    //Allocate Room
    public Room allocateRoom(Reservation reservation) {
        //Check all roooms if available
        
        
        
        
    }
    
    
    //View Room Allocation Report
}
