/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.ws;

import ejb.session.stateful.BookingReservationSessionBeanLocal;
import ejb.session.stateless.PartnerSessionBeanLocal;
import entity.Partner;
import entity.Reservation;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import util.exception.InvalidLoginCredentialException;
import util.exception.ReservationNotFoundException;

/**
 *
 * @author elgin
 */
@WebService(serviceName = "PartnerWebService")
@Stateless()
public class PartnerWebService {

    @EJB
    private BookingReservationSessionBeanLocal bookingReservationSessionBeanLocal;

    @EJB(name = "PartnerSessionBeanLocal")
    private PartnerSessionBeanLocal partnerSessionBeanLocal;
    
    
    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "partnerLogin")
    public Partner partnerLogin(@WebParam(name = "username") String username,
                             @WebParam(name = "password") String password) 
                             throws InvalidLoginCredentialException
    {
        Partner partner = partnerSessionBeanLocal.partnerLogin(username, password);
        System.out.println("********** Partner " 
                            + partner.getPartnerName()
                            + " login remotely via web service");
        return partner;
    }
    
    @WebMethod(operationName = "getNumOfAvailableRooms")
    public HashMap<String,Integer> getNumOfAvailableRooms(@WebParam(name = "checkinDate") Date checkinDate, 
                                                        @WebParam(name = "checkoutDate") Date checkoutDate)
    {
        //Integer numAvailableRooms = bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(, checkinDate, checkoutDate);
        //bookingReservationSessionBeanLocal.
        
        
        return bookingReservationSessionBeanLocal.getSearchRoomResults();
    }
    
    @WebMethod(operationName = "getPrices")
    public HashMap<String,BigDecimal> getPrices(@WebParam(name = "checkinDate") Date checkinDate, 
                                            @WebParam(name = "checkoutDate") Date checkoutDate)
    {
        //Integer numAvailableRooms = bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(, checkinDate, checkoutDate);
        //bookingReservationSessionBeanLocal.
        
        
        return bookingReservationSessionBeanLocal.getRoomTypeNameAndTotalPrice();
    }
    
//    @WebMethod(operationName = "reserveRoom")
//    public List<Reservations> reserveRoom()
            
    
    @WebMethod(operationName = "viewReservationDetails")
    public Reservation viewReservationDetails(@WebParam(name = "partner") Partner partner,
                                            @WebParam(name = "reservationId") Long reservationId)
                                            throws ReservationNotFoundException, InvalidLoginCredentialException
    {
        return partnerSessionBeanLocal.viewPartnerReservation(reservationId, partner);
    }
    
    @WebMethod(operationName = "viewAllReservations")
    public List<Reservation> viewAllReservations(@WebParam(name = "partner") Partner partner)
    {
        return partnerSessionBeanLocal.viewPartnerReservations(partner);
    }
}
