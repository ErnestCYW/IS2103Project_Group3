/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Guest;
import entity.Reservation;
import java.util.List;
import javax.ejb.Local;
import util.exception.GuestEmailExistException;
import util.exception.GuestNotFoundException;
import util.exception.InvalidLoginCredentialException;
import util.exception.ReservationNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
@Local
public interface GuestSessionBeanLocal {

    public Long createNewGuest(Guest newGuest) throws GuestEmailExistException, UnknownPersistenceException;

    public List<Guest> retrieveAllGuest();

    public Guest retrieveGuestByGuestId(Long guestId) throws GuestNotFoundException;

    public Guest retrieveGuestByEmail(String email) throws GuestNotFoundException;

    public Guest guestLogin(String email, String password) throws InvalidLoginCredentialException;

    public Reservation viewGuestReservation(long reservationId, Guest guest) throws ReservationNotFoundException, InvalidLoginCredentialException;

    public List<Reservation> viewGuestReservations(Guest guest);
    
}
