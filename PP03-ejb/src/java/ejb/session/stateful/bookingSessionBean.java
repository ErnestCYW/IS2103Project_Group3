/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateful;

import javax.ejb.Stateful;

/**
 *
 * @author ernestcyw
 */
@Stateful
public class bookingSessionBean implements bookingSessionBeanRemote, bookingSessionBeanLocal {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    
    // Walkin search room -> same as search room on HoRs except one local one remote. Can this be done in stateless instead?
    
    // Walkin reserve room -> same as reserve room on HoRs except one local one remote
    
    // Check-in Guest? Can this be done in stateless instead?
    // Handle the exception produced
    // Print out the associate / room allocated to guest
    
    // Check-out Guest? Can this be done in stateless instead?
    // Must marked reservation as passed
    // Must unassociate reservation with the room
    
    
    
}
