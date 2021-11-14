/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateful.BookingReservationSessionBeanRemote;
import ejb.session.stateless.EmployeeSessionBeanRemote;
import ejb.session.stateless.HandleDateTimeSessionBeanRemote;
import ejb.session.stateless.PartnerSessionBeanRemote;
import ejb.session.stateless.RoomAllocationReportSessionBeanRemote;
import ejb.session.stateless.RoomRateSessionBeanRemote;
import ejb.session.stateless.RoomSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import javax.ejb.EJB;

/**
 *
 * @author ernestcyw
 */
public class Main {

    @EJB
    private static RoomAllocationReportSessionBeanRemote roomAllocationReportSessionBeanRemote;

    @EJB
    private static HandleDateTimeSessionBeanRemote handleDateTimeSessionBeanRemote;

    @EJB
    private static RoomRateSessionBeanRemote roomRateSessionBeanRemote;

    @EJB
    private static RoomSessionBeanRemote roomSessionBeanRemote;
    

    @EJB
    private static BookingReservationSessionBeanRemote bookingReservationSessionBeanRemote;

    @EJB
    private static RoomTypeSessionBeanRemote roomTypeSessionBeanRemote;

    
    @EJB
    private static PartnerSessionBeanRemote partnerSessionBeanRemote;

    @EJB
    private static EmployeeSessionBeanRemote employeeSessionBeanRemote;
    
    

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MainApp mainApp = new MainApp(employeeSessionBeanRemote, partnerSessionBeanRemote, roomSessionBeanRemote, roomRateSessionBeanRemote, roomTypeSessionBeanRemote, bookingReservationSessionBeanRemote, handleDateTimeSessionBeanRemote, roomAllocationReportSessionBeanRemote);
        mainApp.runApp();
    }
    
}
