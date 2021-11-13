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
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomStatusEnum;
import util.exception.CannotGetOnlinePriceException;
import util.exception.CannotGetTodayDateException;
import util.exception.CannotGetWalkInPriceException;
import util.exception.CheckinGuestException;
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

    //Contains RoomType Name & Number of Available Rooms For That Type
    private HashMap<String, Integer> searchRoomResults;
    //Contains All RoomRates Used For That Reservation Based On Period
    private List<RoomRate> roomRatesForReservation;
    //Contains The Room Type And Total Price For A Reservation
    private HashMap<String, BigDecimal> roomTypeNameAndTotalPrice;

    public BookingReservationSessionBean() {
        searchRoomResults = new HashMap<>();
        roomRatesForReservation = new ArrayList<>();
        roomTypeNameAndTotalPrice = new HashMap<>();
    }

    @Override
    public Integer getNumOfAvailableRoomsForRoomType(Long roomTypeId, Date checkinDate, Date checkoutDate) throws RoomTypeNotFoundException {

        RoomType roomType = roomTypeSessionBean.viewRoomTypeDetails(roomTypeId);

        Query countRoomsForTypeQuery = em.createQuery("SELECT COUNT(r) FROM Room r "
                + "WHERE r.roomType = :inRoomType");
        countRoomsForTypeQuery.setParameter("inRoomType", roomType);
        Long numRoomsForType = (Long) countRoomsForTypeQuery.getSingleResult();

        Query countReservationsForTypeQuery = em.createQuery("SELECT COUNT(r) FROM Reservation r "
                + "WHERE ((r.startDate BETWEEN :inCheckinDate AND :inCheckoutDate) "
                + "OR (r.endDate BETWEEN :inCheckinDate AND :inCheckoutDate))"
                + "AND r.roomType = :inRoomType");
        countReservationsForTypeQuery.setParameter("inCheckinDate", checkinDate);
        countReservationsForTypeQuery.setParameter("inCheckoutDate", checkoutDate);
        countReservationsForTypeQuery.setParameter("inRoomType", roomType);
        Long numReservationsForType = (Long) countReservationsForTypeQuery.getSingleResult();

        Long result = numRoomsForType - numReservationsForType;
        return result.intValue();
    }

    @Override
    public Double getWalkInPriceForRoomType(RoomType roomType, Date checkinDate, Date checkoutDate) throws CannotGetWalkInPriceException {

        Double totalPrice = 0.0;

        List<Date> datesStayed = handleDateTimeSessionBean.retrieveDatesBetween(checkinDate, checkoutDate);

        for (Date dateStayed : datesStayed) {

            Query walkInRatesQuery = em.createQuery("SELECT rr FROM RoomRate rr "
                    + "WHERE rr.roomType = :inRoomType "
                    + "AND (:inDateStayed BETWEEN rr.startDate AND rr.endDate) "
                    + "AND NOT rr.disabled "
                    + "AND rr.roomRateType = util.enumeration.RoomRateTypeEnum.PUBLISHED");
            walkInRatesQuery.setParameter("inRoomType", roomType);
            walkInRatesQuery.setParameter("inDateStayed", dateStayed);

            try {
                RoomRate walkInRate = (RoomRate) walkInRatesQuery.getSingleResult();

                if (walkInRate != null) {

                    totalPrice += walkInRate.getRate().doubleValue();

                } else {

                    throw new CannotGetWalkInPriceException("No rates for Date: " + dateStayed
                            + " for Room Type: " + roomType.getName()
                            + " (ie. cannot book for whole period)");

                }

            } catch (NonUniqueResultException ex) {
                throw new CannotGetWalkInPriceException("More than one walkin price applicable");
            }

        }

        roomTypeNameAndTotalPrice.put(roomType.getName(), BigDecimal.valueOf(totalPrice));
        return totalPrice;
    }

    @Override
    public Double getOnlinePriceForRoomType(RoomType roomType, Date checkinDate, Date checkoutDate) throws CannotGetOnlinePriceException {

        Double totalPrice = 0.0;

        List<Date> datesStayed = handleDateTimeSessionBean.retrieveDatesBetween(checkinDate, checkoutDate);

        for (Date dateStayed : datesStayed) {

            Query query = em.createQuery("SELECT rr FROM RoomRate rr "
                    + "WHERE rr.roomType = :inRoomType "
                    + "AND (:inDateStayed BETWEEN rr.startDate AND rr.endDate) "
                    + "AND NOT rr.disabled "
                    + "ORDER BY rr.roomRateType DESC"); //Need to test if really sort by enum field priority (natural ordering vs. string)
            query.setParameter("inRoomType", roomType);
            query.setParameter("inDateStayed", dateStayed);
            RoomRate roomRate = (RoomRate) query.getSingleResult();

            if (roomRate != null) {

                totalPrice += roomRate.getRate().doubleValue();

            } else {

                throw new CannotGetOnlinePriceException("No rates for Date: " + dateStayed
                        + " for Room Type: " + roomType.getName()
                        + " (ie. cannot book for whole period)");

            }

        }

        roomTypeNameAndTotalPrice.put(roomType.getName(), BigDecimal.valueOf(totalPrice));
        return totalPrice;
    }

    @Override
    public List<Long> walkInReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate) throws ReserveRoomException {

        try {

            //Confirm Input With Search Results
            Integer availableRooms = searchRoomResults.get(roomTypeName);
            RoomType roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomTypeName);
            BigDecimal totalPrice = roomTypeNameAndTotalPrice.get(roomTypeName);

            List<Long> reservationIds = new ArrayList<>();

            for (RoomRate roomRate : roomRatesForReservation) {
                if (roomRate.isDisabled()) {
                    throw new ReserveRoomException("A Room Rate for your specified Room Type has been diabled."
                            + "Please search room again.");
                }
            }

            if (searchRoomResults.containsKey(roomTypeName) & numOfRoomsToReserve <= availableRooms) {

                //Creating Reservation & Associating Room Type
                try {

                    Random rand = new Random();
                    Integer int_random = rand.nextInt(9999999);
                    Guest walkInGuest = new Guest(int_random.toString(), "password");
                    Long guestId = guestSessionBeanLocal.createNewGuest(walkInGuest);

                    for (int roomCount = 0; roomCount < numOfRoomsToReserve; roomCount++) {

                        Reservation reservation = new Reservation(checkinDate, checkoutDate, totalPrice);
                        reservation.setGuest(walkInGuest);
                        walkInGuest.getReservations().add(reservation);

                        reservation = reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);

                        //Associate Room Rate
                        for (RoomRate roomRate : roomRatesForReservation) {
                            roomRate.getReservations().add(reservation);
                        }

                        //Associate Guest
                        //Guest guest = guestSessionBeanLocal.retrieveGuestByGuestId(guestId);
                        reservation.setGuest(walkInGuest);
                        walkInGuest.getReservations().add(reservation);

                        //Allocate Room If It Is Past 2AM On Checkin Day
                        if (handleDateTimeSessionBean.isToday(checkinDate) & handleDateTimeSessionBean.isPassed2AM()) {

                            allocationSessionBean.allocateRoom(reservation);

                        };

                        reservationIds.add(reservation.getReservationId());
                    }

                    return reservationIds;

                } catch (InputDataValidationException | UnknownPersistenceException | GuestEmailExistException | CannotGetTodayDateException ex) {

                    throw new ReserveRoomException(ex.getMessage());

                }

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

    @Override
    public List<Long> onlineReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate, Guest loggedInGuest) throws ReserveRoomException {

        try {

            //Confirm Input With Search Results
            Integer availableRooms = searchRoomResults.get(roomTypeName);
            RoomType roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomTypeName);
            BigDecimal totalPrice = roomTypeNameAndTotalPrice.get(roomTypeName);

            List<Long> reservationIds = new ArrayList<>();

            for (RoomRate roomRate : roomRatesForReservation) {
                if (roomRate.isDisabled()) {
                    throw new ReserveRoomException("A Room Rate for your specified Room Type has been diabled."
                            + "Please search room again.");
                }
            }

            if (searchRoomResults.containsKey(roomTypeName) & numOfRoomsToReserve <= availableRooms) {

                //Creating Reservation & Associating Room Type
                try {

                    for (int roomCount = 0; roomCount < numOfRoomsToReserve; roomCount++) {

                        Reservation reservation = new Reservation(checkinDate, checkoutDate, totalPrice);

                        reservation = reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);

                        //Associate Room Rate
                        for (RoomRate roomRate : roomRatesForReservation) {
                            roomRate.getReservations().add(reservation);
                        }

                        //Associate Guest
                        reservation.setGuest(loggedInGuest);
                        loggedInGuest.getReservations().add(reservation);

                        //Allocate Room If It Is Past 2AM On Checkin Day
                        if (handleDateTimeSessionBean.isToday(checkinDate) & handleDateTimeSessionBean.isPassed2AM()) {

                            allocationSessionBean.allocateRoom(reservation);

                        };

                        reservationIds.add(reservation.getReservationId());
                    }

                    return reservationIds;

                } catch (InputDataValidationException | UnknownPersistenceException | CannotGetTodayDateException ex) {

                    throw new ReserveRoomException(ex.getMessage());

                }

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

    @Override
    public void saveSearchResults(String roomTypeName, Integer numOfAvailablerooms) {
        searchRoomResults.put(roomTypeName, numOfAvailablerooms);
    }

    /**
     * public Long onlineReserveRoom(String roomTypeName, Integer
     * numOfRoomsToReserve, Date checkinDate, Date checkoutDate, Guest
     * loggedInGuest) throws ReserveRoomException { }
     *
     */
    @Override
    public List<Room> checkinGuest(Long guestId) throws CheckinGuestException {

        List<Room> rooms = roomSessionBeanLocal.viewAllRooms();
        List<Room> guestRooms = new ArrayList<>();

        for (Room room : rooms) {
            if (room.getCurrentReservation() != null){
                continue;
            } else if (Objects.equals(room.getCurrentReservation().getGuest().getGuestId(), guestId)) {
                    guestRooms.add(room);
            }
        }

        if (guestRooms.isEmpty()) {
            throw new CheckinGuestException("There are no rooms available for Check-in");
        }

        return guestRooms;
    }

    @Override
    public void checkoutGuest(Long guestId) {

        List<Room> rooms = roomSessionBeanLocal.viewAllRooms();

        for (Room room : rooms) {
            if (room.getCurrentReservation() == null){
                continue;
            } if (Objects.equals(room.getCurrentReservation().getGuest().getGuestId(), guestId)) {
                room.setCurrentReservation(null);
                room.setStatus(RoomStatusEnum.AVAILABLE);

                //Disassociating Room Rate and Reservation
                for (RoomRate roomRate : roomRatesForReservation) {
                    if (roomRate.getReservations().contains(room.getCurrentReservation())) {
                        roomRate.getReservations().remove(room.getCurrentReservation());
                    }
                }

            }
        }
    }

}
