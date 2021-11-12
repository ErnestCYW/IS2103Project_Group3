/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.RoomType;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.InputDataValidationException;
import util.exception.ReservationNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class ReservationSessionBean implements ReservationSessionBeanRemote, ReservationSessionBeanLocal {

    @EJB
    private RoomTypeSessionBeanLocal roomTypeSessionBean;

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public ReservationSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public Reservation createReservation(Long roomTypeId, Reservation newReservationEntity) throws InputDataValidationException, UnknownPersistenceException, RoomTypeNotFoundException {

        Set<ConstraintViolation<Reservation>> constraintViolations = validator.validate(newReservationEntity);

        if (constraintViolations.isEmpty()) {

            try {
                RoomType roomType = roomTypeSessionBean.viewRoomTypeDetails(roomTypeId);
                newReservationEntity.setRoomType(roomType);
                roomType.getReservations().add(newReservationEntity);

                em.persist(newReservationEntity);
                em.flush();

                return newReservationEntity;
            } catch (PersistenceException ex) {
                throw new UnknownPersistenceException(ex.getMessage());
            }

        } else {
            throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
        }
    }

    @Override
    public Reservation viewReservation(Long reservationId) throws ReservationNotFoundException {
        Reservation reservationEntity = em.find(Reservation.class, reservationId);

        if (reservationEntity != null) {
            return reservationEntity;
        } else {
            throw new ReservationNotFoundException("Reservation ID " + reservationId + " does not exists!");
        }
    }

    @Override
    public Reservation updateReservation(Long roomTypeId, Reservation reservationEntity) throws ReservationNotFoundException, RoomTypeNotFoundException, InputDataValidationException {
        if (reservationEntity != null && reservationEntity.getReservationId() != null) {

            Set<ConstraintViolation<Reservation>> constraintViolations = validator.validate(reservationEntity);

            if (constraintViolations.isEmpty()) {
                Reservation reservationEntityToUpdate = viewReservation(reservationEntity.getReservationId());

                reservationEntityToUpdate.setStartDate(reservationEntity.getStartDate());
                reservationEntityToUpdate.setEndDate(reservationEntity.getEndDate());
                reservationEntityToUpdate.setTotalCost(reservationEntity.getTotalCost());

                RoomType roomType = roomTypeSessionBean.viewRoomTypeDetails(roomTypeId);

                reservationEntityToUpdate.setRoomType(roomType);
                roomType.getReservations().add(reservationEntity);

                return reservationEntityToUpdate;
            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }
        } else {
            throw new ReservationNotFoundException("Reservation ID not provided for reservation to be updated");
        }
    }

    @Override
    public void deleteReservation(Long reservationId) throws ReservationNotFoundException {
        
        Reservation reservationEntityToRemove = viewReservation(reservationId);
        reservationEntityToRemove.getRoomType().getReservations().remove(reservationEntityToRemove);

        em.remove(reservationEntityToRemove);
        em.flush();
    }

    @Override
    public List<Reservation> viewAllReservations() {
        Query query = em.createQuery("SELECT r FROM Reservation r ORDER BY r.startDate ASC");

        return query.getResultList();
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<Reservation>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }

}
