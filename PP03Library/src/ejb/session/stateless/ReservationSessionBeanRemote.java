/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import java.util.List;
import javax.ejb.Remote;
import util.exception.InputDataValidationException;
import util.exception.ReservationNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Remote
public interface ReservationSessionBeanRemote {

    public void deleteReservation(Long reservationId) throws ReservationNotFoundException;

    public Reservation updateReservation(Long roomTypeId, Reservation reservationEntity) throws ReservationNotFoundException, RoomTypeNotFoundException, InputDataValidationException;

    public Reservation viewReservation(Long reservationId) throws ReservationNotFoundException;

    public Reservation createReservation(Long roomTypeId, Reservation newReservationEntity) throws InputDataValidationException, UnknownPersistenceException, RoomTypeNotFoundException;

    public List<Reservation> viewAllReservations();

}
