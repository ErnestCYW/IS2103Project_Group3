/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03reservationclient;

import ejb.session.stateful.BookingReservationSessionBeanRemote;
import ejb.session.stateless.GuestSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import javax.ejb.EJB;

/**
 *
 * @author ernestcyw
 */
public class Main {

    @EJB
    private static GuestSessionBeanRemote guestSessionBean;

    
    
    @EJB
    private static GuestSessionBeanRemote guestSessionBeanRemote;
    
    @EJB
    private static BookingReservationSessionBeanRemote bookingReservationSessionBeanRemote;

    @EJB
    private static RoomTypeSessionBeanRemote roomTypeSessionBeanRemote;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MainApp mainApp = new MainApp(guestSessionBean, roomTypeSessionBeanRemote, bookingReservationSessionBeanRemote);
        mainApp.runApp();
    }

}
