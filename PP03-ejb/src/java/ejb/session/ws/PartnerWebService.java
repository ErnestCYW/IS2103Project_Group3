/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.ws;

import ejb.session.stateful.BookingReservationSessionBean;
import ejb.session.stateful.BookingReservationSessionBeanLocal;
import ejb.session.stateless.HandleDateTimeSessionBeanLocal;
import ejb.session.stateless.PartnerSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import entity.Partner;
import entity.Reservation;
import entity.RoomType;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import util.exception.CannotGetOnlinePriceException;
import util.exception.InvalidLoginCredentialException;
import util.exception.ReservationNotFoundException;
import util.exception.ReserveRoomException;
import util.exception.RoomTypeNotFoundException;

/**
 *
 * @author elgin
 */
@WebService(serviceName = "PartnerWebService")
@Stateless()
public class PartnerWebService {

    @EJB(name = "HandleDateTimeSessionBeanLocal")
    private HandleDateTimeSessionBeanLocal handleDateTimeSessionBeanLocal;

    @EJB(name = "RoomTypeSessionBeanLocal")
    private RoomTypeSessionBeanLocal roomTypeSessionBeanLocal;

    @EJB
    private BookingReservationSessionBeanLocal bookingReservationSessionBeanLocal;
    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    @EJB(name = "PartnerSessionBeanLocal")
    private PartnerSessionBeanLocal partnerSessionBeanLocal;

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "partnerLogin")
    public Partner partnerLogin(@WebParam(name = "username") String username,
            @WebParam(name = "password") String password)
            throws InvalidLoginCredentialException {
        Partner partner = partnerSessionBeanLocal.partnerLogin(username, password);
        partner.getReservations().size();
        em.detach(partner);

        for (Reservation reservation : partner.getReservations()) {
            em.detach(reservation);
            reservation.setPartner(null);
            partner.setReservations(null);
        }

        System.out.println("********** Partner "
                + partner.getPartnerName()
                + " login remotely via web service");
        return partner;
    }

    
     @WebMethod(operationName = "viewReservationDetails") 
     public Reservation viewReservationDetails(@WebParam(name = "partner") Partner partner,
                                            @WebParam(name = "reservationId") Long reservationId) 
                                            throws ReservationNotFoundException, InvalidLoginCredentialException { 
         Reservation reservation = partnerSessionBeanLocal.viewPartnerReservation(reservationId, partner); 
         
         em.detach(reservation);
         reservation.setPartner(null);
         reservation.setRoomType(null);
         
         return reservation;
     }
     
     
    @WebMethod(operationName = "viewAllReservations")
    public List<Reservation> viewAllReservations(@WebParam(name = "partner") Partner partner) {
        List<Reservation> reservations = partnerSessionBeanLocal.viewReservations(partner);

        for (Reservation reservation : reservations) {
            //If no need room type
            //reservation.setRoomType(null);
            em.detach(reservation);
            reservation.setRoomType(null);
            
            reservation.setPartner(null);
            //reservation.setRoomType(null);
            
            //If need room type
//            RoomType roomType = reservation.getRoomType();
//            em.detach(roomType);
//            roomType.getReservations().remove(reservation);
        }

        return reservations;
    }

    @WebMethod(operationName = "viewAllRoomTypes")
    public List<RoomType> viewAllRoomTypes() {
        List<RoomType> roomTypes = roomTypeSessionBeanLocal.viewAllRoomTypes();
        
        for(RoomType roomType : roomTypes) {
            em.detach(roomType);
            
            roomType.setRooms(null);
            roomType.setRoomRates(null);
            roomType.setReservations(null);
        }
        
        return roomTypes;
        
    }
    
    @WebMethod(operationName = "getNumOfAvailableRoomsForRoomType")
    public Integer getNumOfAvailableRoomsForRoomType(@WebParam(name = "roomTypeId") Long roomTypeId,
                                                    @WebParam(name = "checkinStr") String checkinStr,
                                                    @WebParam(name = "checkoutStr") String checkoutStr) 
                                                    throws RoomTypeNotFoundException, ParseException    
    {
        Date checkinDate = handleDateTimeSessionBeanLocal.convertStringInputToDate(checkinStr);
        Date checkoutDate = handleDateTimeSessionBeanLocal.convertStringInputToDate(checkoutStr);
        return bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(roomTypeId, checkinDate, checkoutDate);
    }
    
    @WebMethod(operationName = "getOnlinePriceForRoomType")
    public Double getOnlinePriceForRoomType(@WebParam(name = "roomType") RoomType roomType, 
                                            @WebParam(name = "checkinDate") String checkinStr,
                                            @WebParam(name = "checkoutDate") String checkoutStr)
                                            throws CannotGetOnlinePriceException, ParseException
    {
        Date checkinDate = handleDateTimeSessionBeanLocal.convertStringInputToDate(checkinStr);
        Date checkoutDate = handleDateTimeSessionBeanLocal.convertStringInputToDate(checkoutStr);
        
        return bookingReservationSessionBeanLocal.getOnlinePriceForRoomType(roomType, checkinDate, checkoutDate);
    }
    
//    @WebMethod(operationName = "partnerReserveRoom")
//    public List<Long> partnerReserveRoom(@WebParam(name = "roomTypeName") String roomTypeName, 
//                                        @WebParam(name = "availableRooms") Integer availableRooms,
//                                        @WebParam(name = "numOfRoomsToReserve") Integer numOfRoomsToReserve,
//                                        @WebParam(name = "price") Double price,
//                                        @WebParam(name = "checkinDate") Date checkinDate,
//                                        @WebParam(name = "checkoutDate") Date checkoutDate,
//                                        @WebParam(name = "partner") Guest partner)
//                                        throws ReserveRoomException
//    {
//        bookingReservationSessionBeanLocal.saveSearchResults(roomTypeName, availableRooms);
//        bookingReservationSessionBeanLocal.savePrices(roomTypeName, price);
//        return bookingReservationSessionBeanLocal.onlineReserveRoom(roomTypeName, numOfRoomsToReserve, checkinDate, checkoutDate, partner);
//    }
    
    @WebMethod(operationName = "partnerReserveRoom")
    public List<Long> partnerReserveRoom(@WebParam(name = "availableInteger") Integer availableRooms,
                                        @WebParam(name = "price") BigDecimal price,
                                        @WebParam(name = "roomTypeName") String roomTypeName, 
                                        @WebParam(name = "numOfRoomsToReserve") Integer numOfRoomsToReserve,
                                        @WebParam(name = "checkinDate") String checkinStr,
                                        @WebParam(name = "checkoutDate") String checkoutStr,
                                        @WebParam(name = "partner") Partner partner)
                                        throws RoomTypeNotFoundException, ReserveRoomException, ParseException
    {
        Date checkinDate = handleDateTimeSessionBeanLocal.convertStringInputToDate(checkinStr);
        Date checkoutDate = handleDateTimeSessionBeanLocal.convertStringInputToDate(checkoutStr);
        
        //Integer availableRooms = bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(roomTypeId, checkinDate, checkoutDate);
        bookingReservationSessionBeanLocal.saveSearchResults(roomTypeName, availableRooms);
        bookingReservationSessionBeanLocal.savePrice(roomTypeName, price);
        
        return bookingReservationSessionBeanLocal.onlineReserveRoomPartner(roomTypeName, numOfRoomsToReserve, checkinDate, checkoutDate, partner);
    }
    
    
    

    /**
     * @WebMethod(operationName = "getNumOfRoomsWithRoomType") public
     * HashMap<String,Integer> getNumOfRoomsWithRoomType(@WebParam(name =
     * "roomTypes") List<RoomType> roomTypes,
     * @WebParam(name = "checkinDate") Date checkinDate,
     * @WebParam(name = "checkoutDate") Date checkoutDate,
     * @WebParam(name = "numRooms") Integer numRooms) throws
     * RoomTypeNotFoundException { for(RoomType roomType:roomTypes) { Integer
     * availableRooms =
     * bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(),
     * checkinDate, checkoutDate);
     *
     * if (availableRooms >= numRooms) {
     * bookingReservationSessionBeanLocal.saveSearchResults(roomType.getName(),
     * availableRooms); }
     *
     * }
     *
     * return bookingReservationSessionBeanLocal.getSearchRoomResults(); }
     *
     * @WebMethod(operationName = "getNumOfRoomsWithRoomType") public
     * HashMap<String,BigDecimal> getPriceWithRoomType(@WebParam(name =
     * "roomTypes") List<RoomType> roomTypes,
     * @WebParam(name = "checkinDate") Date checkinDate,
     * @WebParam(name = "checkoutDate") Date checkoutDate,
     * @WebParam(name = "numRooms") Integer numRooms) throws
     * RoomTypeNotFoundException, CannotGetOnlinePriceException { for(RoomType
     * roomType:roomTypes) { Integer availableRooms =
     * bookingReservationSessionBeanLocal.getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(),
     * checkinDate, checkoutDate); if (availableRooms >= numRooms) {
     *
     * bookingReservationSessionBeanLocal.getOnlinePriceForRoomType(roomType,
     * checkinDate, checkoutDate);
     *
     * }
     * }
     *
     * return bookingReservationSessionBeanLocal.getRoomTypeNameAndTotalPrice();
     * }
     *
     * public void persist(Object object) { em.persist(object); }
     *
     */
}
