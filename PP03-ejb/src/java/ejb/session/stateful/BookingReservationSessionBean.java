/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateful;

import ejb.session.stateless.AllocationSessionBeanLocal;
import ejb.session.stateless.HandleDateTimeSessionBeanLocal;
import ejb.session.stateless.ReservationSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import entity.Reservation;
import entity.Room;
import entity.RoomRate;
import entity.RoomType;
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
import util.exception.InputDataValidationException;
import util.exception.ReserveRoomException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

@Stateful
public class BookingReservationSessionBean implements BookingReservationSessionBeanRemote, BookingReservationSessionBeanLocal {

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

    public BookingReservationSessionBean() {
        searchRoomResults = new HashMap<>();
    }

    @Override
    public Integer getNumOfAvailableRoomsForRoomType(Long roomTypeId, Date checkinDate, Date checkoutDate, Integer totalRooms) {
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
                long diff = checkoutDate.getTime() - checkinDate.getTime();
                totalPrice = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) * roomRate.getRate().doubleValue();
            }
        }
        return totalPrice;
    }

    public Long doReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate) throws ReserveRoomException {

        try {
            //check with room availabilities to confirm reservation, otherwise throw

            Integer rooms = searchRoomResults.get(roomTypeName);
            RoomType roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomTypeName);

            if (searchRoomResults.containsKey(roomTypeName) && rooms >= numOfRoomsToReserve) {
                //Creating Reservation & Associating Room Type
                Reservation reservation = new Reservation(checkinDate, checkoutDate);

                try {
                    reservation = reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);
                } catch (InputDataValidationException | UnknownPersistenceException ex) {
                    throw new ReserveRoomException(ex.getMessage());
                }

                //Associating Room Rate
                for (RoomRate roomRate : roomType.getRoomRates()) {
                    if (roomRate.getRoomRateType().equals(RoomRateTypeEnum.PUBLISHED)) {
                        reservation.setRoomRate(roomRate);
                    }
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

    public Room CheckInGuest(Reservation reservation) throws CheckinGuestException {

        Room room = reservation.getRoom();

        if (room == null) {

            throw new CheckinGuestException("Sorry! The hotel has no rooms of all types for your reservation :( ");

        } else {

            return room;
        }
    }

    public Room CheckOutGuest(Reservation reservation) throws CheckoutGuestException  {

        Room room = reservation.getRoom();

        if (room == null) {
            
            throw new CheckoutGuestException("Cannot find room associated with reservation");

        } else {

            room.setCurrentReservation(null);
            room.setStatus(RoomStatusEnum.AVAILABLE);
            reservation.setPassed(true);
            return room;

        }
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
