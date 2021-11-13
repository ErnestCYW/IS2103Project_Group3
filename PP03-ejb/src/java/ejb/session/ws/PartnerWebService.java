/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.ws;

import ejb.session.stateful.BookingReservationSessionBeanLocal;
import ejb.session.stateless.PartnerSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import entity.Partner;
import entity.Reservation;
import entity.RoomType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import util.exception.CannotGetOnlinePriceException;
import util.exception.InvalidLoginCredentialException;
import util.exception.ReservationNotFoundException;
import util.exception.RoomTypeNotFoundException;

/**
 *
 * @author elgin
 */
@WebService(serviceName = "PartnerWebService")
@Stateless()
public class PartnerWebService {

    @EJB(name = "RoomTypeSessionBeanLocal")
    private RoomTypeSessionBeanLocal roomTypeSessionBeanLocal;

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
        return partnerSessionBeanLocal.viewReservations(partner);
    }
    
    @WebMethod(operationName = "viewAllRoomTypes")
    public List<RoomType> viewAllRoomTypes()
    {
        return roomTypeSessionBeanLocal.viewAllRoomTypes();
    }
    
    @WebMethod(operationName = "getNumOfRoomsWithRoomType")
    public HashMap<String,Integer> getNumOfRoomsWithRoomType(@WebParam(name = "roomTypes") List<RoomType> roomTypes, 
                                                            @WebParam(name = "checkinDate") Date checkinDate, 
                                                            @WebParam(name = "checkoutDate") Date checkoutDate,
                                                            @WebParam(name = "numRooms") Integer numRooms)
                                                            throws RoomTypeNotFoundException
    {
        for(RoomType roomType:roomTypes)
        {
            Integer availableRooms = bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(), checkinDate, checkoutDate);
            
            if (availableRooms >= numRooms) {
                bookingReservationSessionBeanLocal.saveSearchResults(roomType.getName(), availableRooms);
            }
            
        }
        
        return bookingReservationSessionBeanLocal.getSearchRoomResults();
    }
    
    @WebMethod(operationName = "getNumOfRoomsWithRoomType")
    public HashMap<String,BigDecimal> getPriceWithRoomType(@WebParam(name = "roomTypes") List<RoomType> roomTypes, 
                                                            @WebParam(name = "checkinDate") Date checkinDate, 
                                                            @WebParam(name = "checkoutDate") Date checkoutDate,
                                                            @WebParam(name = "numRooms") Integer numRooms)
                                                            throws RoomTypeNotFoundException, CannotGetOnlinePriceException
    {
        for(RoomType roomType:roomTypes)
        {
            Integer availableRooms = bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(), checkinDate, checkoutDate);
            if (availableRooms >= numRooms) {
                
                bookingReservationSessionBeanLocal.getOnlinePriceForRoomType(roomType, checkinDate, checkoutDate);
        
            }
        }
        
        return bookingReservationSessionBeanLocal.getRoomTypeNameAndTotalPrice();
    }
    
}
