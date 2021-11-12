/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateful;

import ejb.session.stateless.AllocationSessionBeanLocal;
import ejb.session.stateless.GuestSessionBeanLocal;
import ejb.session.stateless.HandleDateTimeSessionBeanLocal;
import ejb.session.stateless.ReservationSessionBeanLocal;
import ejb.session.stateless.RoomSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import entity.Guest;
import entity.Reservation;
import entity.Room;
import entity.RoomRate;
import entity.RoomType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomRateTypeEnum;
import util.enumeration.RoomStatusEnum;
import util.exception.CheckinGuestException;
import util.exception.CheckoutGuestException;
import util.exception.GuestEmailExistException;
import util.exception.InputDataValidationException;
import util.exception.ReserveRoomException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

@Stateful
public class BookingReservationSessionBean implements BookingReservationSessionBeanRemote, BookingReservationSessionBeanLocal {

    @EJB
    private RoomSessionBeanLocal roomSessionBeanLocal;
    
    @EJB
    private GuestSessionBeanLocal guestSessionBeanLocal;

    @EJB
    private AllocationSessionBeanLocal allocationSessionBean;

    @EJB
    private HandleDateTimeSessionBeanLocal handleDateTimeSessionBean;

    @EJB
    private ReservationSessionBeanLocal reservationSessionBean;

    @EJB
    private RoomTypeSessionBeanLocal roomTypeSessionBean;

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    private HashMap<String, Integer> searchRoomResults;
    private List<RoomRate> roomRatesForReservation;
    private HashMap<String, BigDecimal> roomTypeReservationAmount;

    public BookingReservationSessionBean() {
        searchRoomResults = new HashMap<>();
        roomRatesForReservation = new ArrayList<>();
        roomTypeReservationAmount = new HashMap<>();
    }

    @Override
    public Integer getNumOfAvailableRoomsForRoomType(Long roomTypeId, Date checkinDate, Date checkoutDate, Integer totalRooms) { //Total Rooms of that Room Type
        Query numberReservationsQuery = em.createQuery("SELECT r FROM Reservation r WHERE"
                + " AND (r.startDate BETWEEN :checkinDate AND :checkoutDate)"
                + " OR (r.endDate BETWEEN :checkinDate AND :checkoutDate)");
        numberReservationsQuery.setParameter("checkinDate", checkinDate);
        numberReservationsQuery.setParameter("checkoutDate", checkoutDate);

        List<Reservation> reservations = numberReservationsQuery.getResultList();
        Integer numOfRoomsReserved = 0;

        for (Reservation reservation : reservations) {
            if (reservation.getRoomType().getRoomTypeId() == roomTypeId) {
                numOfRoomsReserved++;
            }
        }

        return totalRooms - numOfRoomsReserved;

    }

    public void saveSearchResults(String roomTypeName, Integer numOfAvailablerooms) {
        searchRoomResults.put(roomTypeName, numOfAvailablerooms);
    }

    @Override
    public Double getWalkInPriceForRoomType(RoomType roomType, Date checkinDate, Date checkoutDate) {
        Double totalPrice = 0.0;
        for (RoomRate roomRate : roomType.getRoomRates()) {
            if (roomRate.getRoomRateType().equals(RoomRateTypeEnum.PUBLISHED)) {
                
                //save the room rate used for this reservation
                roomRatesForReservation.add(roomRate);
                
                long diff = checkoutDate.getTime() - checkinDate.getTime();
                totalPrice = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) * roomRate.getRate().doubleValue();
            }
        }
        
        roomTypeReservationAmount.put(roomType.getName(), BigDecimal.valueOf(totalPrice));
        return totalPrice;
    }
    
    public Double getOnlinePriceForRoomType(RoomType roomType, Date checkinDate, Date checkoutDate) {
        Double totalPrice = 0.0;
        for (RoomRate roomRate : roomType.getRoomRates()) {
            if (roomRate.getRoomRateType().equals(RoomRateTypeEnum.PUBLISHED)) {
                
                //save the room rate used for this reservation
                roomRatesForReservation.add(roomRate);
                
                long diff = checkoutDate.getTime() - checkinDate.getTime();
                totalPrice = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) * roomRate.getRate().doubleValue();
            }
        }
        
        roomTypeReservationAmount.put(roomType.getName(), BigDecimal.valueOf(totalPrice));
        return totalPrice;
    }

    @Override
    public Long walkInReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate) throws ReserveRoomException {

        try {
            //check with room availabilities to confirm reservation, otherwise throw

            Integer rooms = searchRoomResults.get(roomTypeName);
            BigDecimal reservationAmount = roomTypeReservationAmount.get(roomTypeName);
            RoomType roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomTypeName);
            
            //cannot reserve if room rate is disabled
            for(RoomRate roomRate : roomRatesForReservation) {
                if (roomRate.isDisabled()) {
                    //EXCEPTION
                }
            }

            if (searchRoomResults.containsKey(roomTypeName) && rooms >= numOfRoomsToReserve) {
                //Creating Reservation & Associating Room Type
                Reservation reservation = new Reservation(checkinDate, checkoutDate, reservationAmount);

                try {
                    reservation = reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);
                    
                    //Associating Room Rate and Reservation
                    for (RoomRate roomRate : roomRatesForReservation) {
                        roomRate.getReservations().add(reservation);
                    }
                    
                    //Associate Reservation and Guest
                    Guest walkInGuest = new Guest();
                    Long guestId = guestSessionBeanLocal.createNewGuest(walkInGuest);
                    reservation.setGuest(walkInGuest);
                    walkInGuest.getReservations().add(reservation);
                    
                    
                } catch (InputDataValidationException | UnknownPersistenceException | GuestEmailExistException ex) {
                    throw new ReserveRoomException(ex.getMessage());
                }

                

                //Allocation Logic if past 2am and checkindate
                if (handleDateTimeSessionBean.isToday(checkinDate) & handleDateTimeSessionBean.isPassed2AM()) {
                    try {
                        allocationSessionBean.allocateRoom(reservation);
                    } catch (Exception ex) {

                    }
                };

                return reservation.getReservationId();

            } else {
                if (!searchRoomResults.containsKey(roomTypeName)) {
                    throw new ReserveRoomException("Invalid Room Type");
                } else {
                    throw new ReserveRoomException("Not enough rooms");
                }
            }
        } catch (RoomTypeNotFoundException ex) {
            throw new ReserveRoomException(ex.getMessage());
        }
    }
    
    
    public Long onlineReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate, Guest loggedInGuest) throws ReserveRoomException {
    }
    
    @Override
    public List<Room> checkinGuest(Long guestId) throws CheckinGuestException {
        
        List<Room> rooms = roomSessionBeanLocal.viewAllRooms();
        List<Room> guestRooms = new ArrayList<>();
        
        for (Room room:rooms)
        {
            if(room.getCurrentReservation().getGuest().getGuestId() == guestId)
            {
                guestRooms.add(room);
            }
        }
        
        if(guestRooms.isEmpty())
        {
            throw new CheckinGuestException("There are no rooms available for Check-in");
        }
        
        return guestRooms;
    }
    
    /**
    public Room CheckInGuest(Long guestId) throws CheckinGuestException {

        Room room = reservation

        if (room == null) {

            throw new CheckinGuestException("Sorry! The hotel has no rooms of all types for your reservation :( ");

        } else {

            return room;
        }
    }
    **/

    @Override
    public void checkoutGuest(Long guestId) {

        List<Room> rooms = roomSessionBeanLocal.viewAllRooms();
        
        for (Room room:rooms)
        {
            if(room.getCurrentReservation().getGuest().getGuestId() == guestId)
            {
                room.setCurrentReservation(null);
                room.setStatus(RoomStatusEnum.AVAILABLE);
                
                //Disassociating Room Rate and Reservation
                for(RoomRate roomRate : roomRatesForReservation)
                {
                    if(roomRate.getReservations().contains(room.getCurrentReservation()))
                    {
                        roomRate.getReservations().remove(room.getCurrentReservation());
                    }
                }
                
            }
        }
        
//        Room room = reservation.getRoom();
//
//        if (room == null) {
//            
//            throw new CheckoutGuestException("Cannot find room associated with reservation");
//
//        } else {
//
//            room.setCurrentReservation(null);
//            room.setStatus(RoomStatusEnum.AVAILABLE);
//            
//            return room;
//
//        }
    }

//    public List<SearchResult> WalkInSearchRoom(Date checkinDate, Date checkoutDate) {
//
//        List<SearchResult> searchResults = new ArrayList<>();
//        List<RoomType> roomTypes = roomTypeSessionBean.viewAllRoomTypes();
//
//        for (RoomType roomType : roomTypes) {
//
//            //Number of Rooms Logic
//            Query numberReservationsQuery = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE"
//                    + " (r.startDate BETWEEN :checkinDate AND :checkoutDate)"
//                    + " OR (r.endDate BETWEEN :checkinDate AND :checkoutDate");
//            numberReservationsQuery.setParameter("checkinDate", checkinDate);
//            numberReservationsQuery.setParameter("checkoutDate", checkoutDate);
//            Integer numRoomsAvailable = (Integer) numberReservationsQuery.getSingleResult();
//
//            //Pricing Logic
//            Double totalPrice = 0.0;
//            for (RoomRate roomRate : roomType.getRoomRates()) {
//                if (roomRate.getRoomRateType().equals(RoomRateTypeEnum.PUBLISHED)) {
//                    long diff = checkoutDate.getTime() - checkinDate.getTime();
//                    totalPrice = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS) * roomRate.getRate().doubleValue();
//                }
//            }
//        }
//        return searchResults;
//    }
    // Walkin reserve room -> same as reserve room on HoRs except one local one remote
//    public Reservation WalkInReserveRoom(RoomType roomType, Date checkinDate, Date checkoutDate) throws InputDataValidationException, UnknownPersistenceException, RoomTypeNotFoundException {
//
//        //Creating Reservation & Associating Room Type
//        Reservation reservation = new Reservation(checkinDate, checkoutDate);
//        reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);
//
//        //Associating Room Rate
//        for (RoomRate roomRate : roomType.getRoomRates()) {
//            if (roomRate.getRoomRateType().equals(RoomRateTypeEnum.PUBLISHED)) {
//                reservation.setRoomRate(roomRate);
//            }
//        }
//
//        //Allocation Logic if past 2am and checkindate
//        if (isToday(checkinDate) & isPassed2AM()) {
//            // Call alloacte room logic
//        };
//
//        return reservation;
//    }
}
