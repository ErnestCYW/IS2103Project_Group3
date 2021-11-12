/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateful;

import ejb.session.stateless.ReservationSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import entity.Reservation;
import entity.Room;
import entity.RoomRate;
import entity.RoomType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.naming.directory.SearchResult;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomRateTypeEnum;
import util.exception.InputDataValidationException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
@Stateful
public class BookingReservationSessionBean implements BookingReservationSessionBeanRemote, BookingReservationSessionBeanLocal {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    import ejb.session.stateless.ReservationSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import entity.Reservation;
import entity.Room;
import entity.RoomRate;
import entity.RoomType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.naming.directory.SearchResult;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.RoomRateTypeEnum;
import util.exception.InputDataValidationException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Stateful
public class BookingSessionBean implements BookingSessionBeanRemote, BookingSessionBeanLocal {

    @EJB
    private ReservationSessionBeanLocal reservationSessionBean;

    @EJB
    private RoomTypeSessionBeanLocal roomTypeSessionBean;

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    // Need to integrate with customer?
    // Walkin search room -> same as search room on HoRs except one local one remote. Can this be done in stateless instead?
    public List<SearchResult> WalkInSearchRoom(Date checkinDate, Date checkoutDate) {

        List<SearchResult> searchResults = new ArrayList<>();
        List<RoomType> roomTypes = roomTypeSessionBean.viewAllRoomTypes();

        for (RoomType roomType : roomTypes) {
            
            //Number of Rooms Logic
            Query numberReservationsQuery = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE"
                    + " (r.startDate BETWEEN :checkinDate AND :checkoutDate)"
                    + " OR (r.endDate BETWEEN :checkinDate AND :checkoutDate");
            numberReservationsQuery.setParameter("checkinDate", checkinDate);
            numberReservationsQuery.setParameter("checkoutDate", checkoutDate);
            Integer numRoomsAvailable = (Integer) numberReservationsQuery.getSingleResult();

            //Pricing Logic
            Double totalPrice = 0.0;
            for (RoomRate roomRate : roomType.getRoomRates()) {
                if (roomRate.getRoomRateType().equals(RoomRateTypeEnum.PUBLISHED)) {
                    long diff = checkoutDate.getTime() - checkinDate.getTime();
                    totalPrice = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS) * roomRate.getRate().doubleValue();
                }
            }
        }
        return searchResults;
    }

    // Walkin reserve room -> same as reserve room on HoRs except one local one remote
    public Reservation WalkInReserveRoom(RoomType roomType, Date checkinDate, Date checkoutDate) throws InputDataValidationException, UnknownPersistenceException, RoomTypeNotFoundException {

        //Creating Reservation & Associating Room Type
        Reservation reservation = new Reservation(checkinDate, checkoutDate);
        reservationSessionBean.createReservation(roomType.getRoomTypeId(), reservation);

        //Associating Room Rate
        for (RoomRate roomRate : roomType.getRoomRates()) {
            if (roomRate.getRoomRateType().equals(RoomRateTypeEnum.PUBLISHED)) {
                reservation.setRoomRate(roomRate);
            }
        }

        //Allocation Logic if past 2am and checkindate
        if (isToday(checkinDate) & isPassed2AM()) {
            // Call alloacte room logic
        };
        
        return reservation;
    }

    // Check-in Guest? Can this be done in stateless instead?
    // Handle the exception produced
    // Print out the associate / room allocated to guest
    public Room CheckInGuest(Reservation reservation) {

        Room room = reservation.getRoom();

        if (room == null)  {
            //Allocation Logic
            //No Room No Upgrade Available
            
            
            
            
        } else {
            //Room has already been allocated
            //Inform if upgrade has been made
            return room;
        }
    }

    // Check-out Guest? Can this be done in stateless instead?
    // Must marked reservation as passed
    // Must unassociate reservation with the room
    public Room CheckOutGuest(Reservation reservation) {

        Room room = reservation.getRoom();

        if (room == null)  {
            //Allocation Logic
        } else {
            return room;
        }
    }
    
    private boolean isToday(Date date) {
        LocalDate localDate1 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = (new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.isEqual(localDate2);
    }
    
    private boolean isPassed2AM() {
        LocalTime now = LocalTime.now();
        return now.getHour() > 2;
    }

    public void persist(Object object) {
        em.persist(object);
    }

}

}
