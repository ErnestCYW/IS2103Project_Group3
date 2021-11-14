/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateful;

import ejb.session.stateless.AllocationSessionBeanLocal;
import ejb.session.stateless.GuestSessionBeanLocal;
import ejb.session.stateless.HandleDateTimeSessionBeanLocal;
import ejb.session.stateless.PartnerSessionBeanLocal;
import ejb.session.stateless.ReservationSessionBeanLocal;
import ejb.session.stateless.RoomAllocationReportSessionBeanLocal;
import ejb.session.stateless.RoomRateSessionBeanLocal;
import ejb.session.stateless.RoomSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import entity.Guest;
import entity.Partner;
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
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomStatusEnum;
import util.exception.CannotGetOnlinePriceException;
import util.exception.CannotGetTodayDateException;
import util.exception.CannotGetWalkInPriceException;
import util.exception.CheckinGuestException;
import util.exception.CheckoutGuestException;
import util.exception.GuestEmailExistException;
import util.exception.GuestNotFoundException;
import util.exception.InputDataValidationException;
import util.exception.PartnerNotFoundException;
import util.exception.ReserveRoomException;
import util.exception.RoomAllocationReportNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

@Stateful
public class BookingReservationSessionBean implements BookingReservationSessionBeanRemote, BookingReservationSessionBeanLocal {

    @EJB
    private PartnerSessionBeanLocal partnerSessionBeanLocal;

    @EJB
    private RoomAllocationReportSessionBeanLocal roomAllocationReportSessionBeanLocal;

    @EJB
    private RoomRateSessionBeanLocal roomRateSessionBeanLocal;

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

        //Find number of rooms for required type
        Query countRoomsForTypeQuery = em.createQuery("SELECT COUNT(r) FROM Room r "
                + "WHERE r.roomType = :inRoomType");
        countRoomsForTypeQuery.setParameter("inRoomType", roomType);
        Long numRoomsForType = (Long) countRoomsForTypeQuery.getSingleResult();

        //Find number of reservations for that type for that period
        Query countReservationsForTypeQuery = em.createQuery("SELECT COUNT(r) FROM Reservation r "
                + "WHERE ((r.startDate BETWEEN :inCheckinDate AND :inCheckoutDate) "
                + "OR (r.endDate BETWEEN :inCheckinDate AND :inCheckoutDate))"
                + "AND r.roomType = :inRoomType");
        countReservationsForTypeQuery.setParameter("inCheckinDate", checkinDate);
        countReservationsForTypeQuery.setParameter("inCheckoutDate", checkoutDate);
        countReservationsForTypeQuery.setParameter("inRoomType", roomType);
        Long numReservationsForType = (Long) countReservationsForTypeQuery.getSingleResult();

        //Calculate number of rooms remaining
        Long result = numRoomsForType - numReservationsForType;
        return result.intValue();
    }

    @Override
    public Double getWalkInPriceForRoomType(RoomType roomType, Date checkinDate, Date checkoutDate) throws CannotGetWalkInPriceException {

        Double totalPrice = 0.0;

        List<Date> datesStayed = handleDateTimeSessionBean.retrieveDatesBetween(checkinDate, checkoutDate);

        //For each day
        for (Date dateStayed : datesStayed) {

            //Find a published rate applicable to that day (same period) for that type
            Query query = em.createQuery("SELECT rr FROM RoomRate rr "
                    + "WHERE rr.roomType = :inRoomType "
                    + "AND (:inDateStayed BETWEEN rr.startDate AND rr.endDate) "
                    + "AND NOT rr.disabled "
                    + "AND rr.roomRateType = util.enumeration.RoomRateTypeEnum.PUBLISHED");
            query.setParameter("inRoomType", roomType);
            query.setParameter("inDateStayed", dateStayed);

            //Calculate the rate for the day using found rate
            try {

                RoomRate walkInRate = (RoomRate) query.getSingleResult();

                if (walkInRate != null) {

                    totalPrice += walkInRate.getRate().doubleValue();
                    roomRatesForReservation.add(walkInRate);

                } else {

                    throw new CannotGetWalkInPriceException("No rates for Date: " + dateStayed
                            + " for Room Type: " + roomType.getName()
                            + " (ie. cannot book for whole period)");

                }

            } catch (NonUniqueResultException ex) {

                throw new CannotGetWalkInPriceException("More than one publised rate for that room type applicable");

            } catch (NoResultException ex) {

                throw new CannotGetWalkInPriceException("No published rates for that room type");

            }

        }

        getRoomTypeNameAndTotalPrice().put(roomType.getName(), BigDecimal.valueOf(totalPrice));
        return totalPrice;
    }

    @Override
    public Double getOnlinePriceForRoomType(RoomType roomType, Date checkinDate, Date checkoutDate) throws CannotGetOnlinePriceException {

        Double totalPrice = 0.0;

        List<Date> datesStayed = handleDateTimeSessionBean.retrieveDatesBetween(checkinDate, checkoutDate);

        //For each day
        for (Date dateStayed : datesStayed) {

            //Find a rate applicable to that day (same period) for that type in the order Promotion -> Peak -> Normal  
            Query query = em.createQuery("SELECT rr FROM RoomRate rr "
                    + "WHERE rr.roomType = :inRoomType "
                    + "AND (:inDateStayed BETWEEN rr.startDate AND rr.endDate) "
                    + "AND NOT rr.disabled "
                    + "ORDER BY rr.roomRateType ASC");
            query.setParameter("inRoomType", roomType);
            query.setParameter("inDateStayed", dateStayed);
            List<RoomRate> temp = query.setMaxResults(1).getResultList();

            //Calculate the rate for the day using found rate
            if (!temp.isEmpty()) {

                RoomRate onlineRate = temp.get(0);
                totalPrice += onlineRate.getRate().doubleValue();
                roomRatesForReservation.add(onlineRate);

            } else {

                throw new CannotGetOnlinePriceException("No rates for Date: " + dateStayed
                        + " for Room Type: " + roomType.getName()
                        + " (ie. cannot book for whole period)");

            }

        }

        getRoomTypeNameAndTotalPrice().put(roomType.getName(), BigDecimal.valueOf(totalPrice));
        return totalPrice;
    }

    @Override
    public List<Long> walkInReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate) throws ReserveRoomException {

        try {

            //Retrieve information stored in stateful session bean
            Integer availableRooms = getSearchRoomResults().get(roomTypeName);
            RoomType roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomTypeName);
            BigDecimal totalPrice = getRoomTypeNameAndTotalPrice().get(roomTypeName);

            List<Long> reservationIds = new ArrayList<>();

            //Check if room rate was just disabled after search
            for (RoomRate roomRate : roomRatesForReservation) {
                if (roomRate.isDisabled()) {
                    throw new ReserveRoomException("A Room Rate for your specified Room Type has just been disabled."
                            + "Please search room again.");
                }
            }

            //Confirm Input With Search Results
            if (searchRoomResults.containsKey(roomTypeName) & numOfRoomsToReserve <= availableRooms) {

                //Creating Reservation & Associating Room Type
                try {

                    //Creating new guest account for walkin guest
                    Random rand = new Random();
                    Integer int_random = rand.nextInt(99999999);
                    Guest walkInGuest = new Guest(int_random.toString(), "password");
                    guestSessionBeanLocal.createNewGuest(walkInGuest);

                    for (int roomCount = 0; roomCount < numOfRoomsToReserve; roomCount++) {

                        Reservation reservation = new Reservation(checkinDate, checkoutDate, totalPrice);

                        //Associate Guest
                        reservation.setGuest(walkInGuest);
                        walkInGuest.getReservations().add(reservation);

                        //Associate Room Rate
                        for (RoomRate roomRate : roomRatesForReservation) {
                            roomRate.getReservations().add(reservation);
                        }

                        reservation = reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);

                        //Allocate Room If It Is Past 2AM On Checkin Day
                        if (handleDateTimeSessionBean.isToday(checkinDate) & handleDateTimeSessionBean.isPassed2AM()) {

                            allocationSessionBean.allocateRoom(reservation, roomAllocationReportSessionBeanLocal.viewTodayRoomAllocationReport());

                        }

                        reservationIds.add(reservation.getReservationId());
                    }

                    return reservationIds;

                } catch (InputDataValidationException | UnknownPersistenceException | CannotGetTodayDateException | GuestEmailExistException | RoomAllocationReportNotFoundException ex) {

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

            throw new ReserveRoomException("Invalid Room Type Entered");

        }

    }

    @Override
    public List<Long> onlineReserveRoom(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate, Guest loggedInGuest) throws ReserveRoomException {

        try {

            //Retrieve information stored in stateful session bean
            Integer availableRooms = getSearchRoomResults().get(roomTypeName);
            RoomType roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomTypeName);
            BigDecimal totalPrice = getRoomTypeNameAndTotalPrice().get(roomTypeName);

            List<Long> reservationIds = new ArrayList<>();

            //Check if room rate was just disabled after search
            for (RoomRate roomRate : roomRatesForReservation) {
                if (roomRate.isDisabled()) {
                    throw new ReserveRoomException("A Room Rate for your specified Room Type has been diabled."
                            + "Please search room again.");
                }
            }

            //Confirm Input With Search Results
            if (searchRoomResults.containsKey(roomTypeName) & numOfRoomsToReserve <= availableRooms) {

                //Creating Reservation & Associating Room Type
                try {

                    for (int roomCount = 0; roomCount < numOfRoomsToReserve; roomCount++) {

                        Reservation reservation = new Reservation(checkinDate, checkoutDate, totalPrice);

                        //Associate Guest
                        loggedInGuest = guestSessionBeanLocal.retrieveGuestByGuestId(loggedInGuest.getGuestId()); //Managed instance of guest
                        loggedInGuest.getReservations().size();
                        reservation.setGuest(loggedInGuest);
                        loggedInGuest.getReservations().add(reservation);

                        //Associate Room Rate
                        for (RoomRate roomRate : roomRatesForReservation) {
                            roomRate.getReservations().add(reservation);
                        }

                        reservation = reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);

                        //Allocate Room If It Is Past 2AM On Checkin Day
                        if (handleDateTimeSessionBean.isToday(checkinDate) & handleDateTimeSessionBean.isPassed2AM()) {

                            allocationSessionBean.allocateRoom(reservation, roomAllocationReportSessionBeanLocal.viewTodayRoomAllocationReport());

                        }

                        reservationIds.add(reservation.getReservationId());
                    }

                    return reservationIds;

                } catch (InputDataValidationException | UnknownPersistenceException | CannotGetTodayDateException | GuestNotFoundException | RoomAllocationReportNotFoundException ex) {

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

            throw new ReserveRoomException("Invalid Room Type Entered");

        }

    }

    @Override
    public List<Long> onlineReserveRoomPartner(String roomTypeName, Integer numOfRoomsToReserve, Date checkinDate, Date checkoutDate, Partner loggedInPartner) throws ReserveRoomException {

        try {

            //Retrieve information stored in stateful session bean
            Integer availableRooms = getSearchRoomResults().get(roomTypeName);
            RoomType roomType = roomTypeSessionBean.retrieveRoomTypeByName(roomTypeName);
            BigDecimal totalPrice = getRoomTypeNameAndTotalPrice().get(roomTypeName);

            List<Long> reservationIds = new ArrayList<>();

            //Check if room rate was just disabled after search
            for (RoomRate roomRate : roomRatesForReservation) {
                if (roomRate.isDisabled()) {
                    throw new ReserveRoomException("A Room Rate for your specified Room Type has been diabled."
                            + "Please search room again.");
                }
            }

            //Confirm Input With Search Results
            if (searchRoomResults.containsKey(roomTypeName) & numOfRoomsToReserve <= availableRooms) {

                //Creating Reservation & Associating Room Type
                try {

                    for (int roomCount = 0; roomCount < numOfRoomsToReserve; roomCount++) {

                        Reservation reservation = new Reservation(checkinDate, checkoutDate, totalPrice);

                        //Associate Guest
                        loggedInPartner = partnerSessionBeanLocal.retrievePartnerByPartnerId(loggedInPartner.getPartnerId());
                        loggedInPartner.getReservations().size();
                        reservation.setPartner(loggedInPartner);
                        loggedInPartner.getReservations().add(reservation);

                        //Associate Room Rate
                        for (RoomRate roomRate : roomRatesForReservation) {
                            roomRate.getReservations().add(reservation);
                        }

                        reservation = reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);

                        //Allocate Room If It Is Past 2AM On Checkin Day
                        if (handleDateTimeSessionBean.isToday(checkinDate) & handleDateTimeSessionBean.isPassed2AM()) {

                            allocationSessionBean.allocateRoom(reservation, roomAllocationReportSessionBeanLocal.viewTodayRoomAllocationReport());

                        }

                        reservationIds.add(reservation.getReservationId());
                    }

                    return reservationIds;

                } catch (InputDataValidationException | UnknownPersistenceException | CannotGetTodayDateException | PartnerNotFoundException | RoomAllocationReportNotFoundException ex) {

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

            throw new ReserveRoomException("Invalid Room Type Entered");

        }

    }

    @Override
    public void saveSearchResults(String roomTypeName, Integer numOfAvailablerooms) {
        getSearchRoomResults().put(roomTypeName, numOfAvailablerooms);
    }

    //Can refactor with JPQL to improve complexity
    @Override
    public List<Room> checkinGuest(Long guestId) throws CheckinGuestException {

        List<Room> rooms = roomSessionBeanLocal.viewAllRooms();
        List<Room> guestRooms = new ArrayList<>();

        //Loop through rooms to find if guest has an allocated room today
        for (Room room : rooms) {
            if (room.getCurrentReservation() == null) {
                continue;
            } else if (Objects.equals(room.getCurrentReservation().getGuest().getGuestId(), guestId)
                    & handleDateTimeSessionBean.isToday(room.getCurrentReservation().getStartDate())
                    & handleDateTimeSessionBean.isPassed2AM()) {
                guestRooms.add(room);
            }
        }

        if (guestRooms.isEmpty()) {
            throw new CheckinGuestException("Guest has no rooms available for Check-in. "
                    + "If you have a reservation starting today, it means that the hotel is fully booked"
                    + " and system could not auto allocate you a room.");
        }

        return guestRooms;

    }

    //Can refactor with JPQL to improve complexity
    @Override
    public List<Room> checkoutGuest(Long guestId) throws CheckoutGuestException {

        //Disassociate Room Rate (Guest reservations using this rate and has not passed)
        try {
            List<Reservation> guestReservations = guestSessionBeanLocal.viewGuestReservations(guestSessionBeanLocal.retrieveGuestByGuestId(guestId));
            List<RoomRate> roomRates = roomRateSessionBeanLocal.viewAllRoomRates();

            for (RoomRate roomRate : roomRates) {
                roomRate.getReservations().size();
                for (Reservation guestReservation : guestReservations) {
                    if (roomRate.getReservations().contains(guestReservation)
                            & handleDateTimeSessionBean.isToday(guestReservation.getEndDate())
                            & handleDateTimeSessionBean.isPassed2AM()) {
                        roomRate.getReservations().remove(guestReservation);
                    }
                }
            }

        } catch (GuestNotFoundException ex) {
            throw new CheckoutGuestException("Guest does not exist");
        }

        //Disassociate Rooms (Current reservation of the guest)
        List<Room> rooms = roomSessionBeanLocal.viewAllRooms();
        List<Room> checkedOut = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getCurrentReservation() == null) {
                continue;
            } else if (Objects.equals(room.getCurrentReservation().getGuest().getGuestId(), guestId)) {

                room.setCurrentReservation(null);
                room.setStatus(RoomStatusEnum.AVAILABLE);

                checkedOut.add(room);
            }
        }

        if (checkedOut.isEmpty()) {
            throw new CheckoutGuestException("Guest has no rooms to checkout");
        }

        return checkedOut;

    }

    @Override
    public HashMap<String, Integer> getSearchRoomResults() {
        return searchRoomResults;
    }

    @Override
    public HashMap<String, BigDecimal> getRoomTypeNameAndTotalPrice() {
        return roomTypeNameAndTotalPrice;
    }

}
