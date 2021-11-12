/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.RoomRate;
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
import util.exception.RoomRateNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class RoomRateSessionBean implements RoomRateSessionBeanRemote, RoomRateSessionBeanLocal {

    @EJB
    private RoomTypeSessionBeanLocal roomTypeSessionBean;
    
    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    public RoomRateSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public RoomRate createNewRoomRate(Long roomTypeId, RoomRate newRoomRateEntity) throws InputDataValidationException, RoomTypeNotFoundException, UnknownPersistenceException {

        Set<ConstraintViolation<RoomRate>> constraintViolations = validator.validate(newRoomRateEntity);

        if (constraintViolations.isEmpty()) {
            try {
                RoomType roomType = roomTypeSessionBean.viewRoomTypeDetails(roomTypeId);

                roomType.getRoomRates().add(newRoomRateEntity);

                em.persist(newRoomRateEntity);

                return newRoomRateEntity;
            } catch (PersistenceException ex) {
                throw new UnknownPersistenceException(ex.getMessage());
            }

        } else {
            throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
        }
    }

    @Override
    public RoomRate viewRoomRateDetails(Long roomRateId) throws RoomRateNotFoundException {

        RoomRate roomRateEntity = em.find(RoomRate.class, roomRateId);

        if (roomRateEntity != null) {
            return roomRateEntity;
        } else {
            throw new RoomRateNotFoundException("RoomRate ID " + roomRateId + " does not exists!");
        }

    }

    @Override
    public RoomRate updateRoomRate(Long roomTypeId, RoomRate roomRateEntity) throws InputDataValidationException, RoomRateNotFoundException, RoomTypeNotFoundException {
        if (roomRateEntity != null && roomRateEntity.getRoomRateId() != null) {

            Set<ConstraintViolation<RoomRate>> constraintViolations = validator.validate(roomRateEntity);

            if (constraintViolations.isEmpty()) {

                RoomRate roomRateEntityToUpdate = viewRoomRateDetails(roomRateEntity.getRoomRateId());

                roomRateEntityToUpdate.setName(roomRateEntity.getName());
                roomRateEntityToUpdate.setRoomRateType(roomRateEntity.getRoomRateType());
                roomRateEntityToUpdate.setRate(roomRateEntity.getRate());
                roomRateEntityToUpdate.setStartDate(roomRateEntity.getStartDate());
                roomRateEntityToUpdate.setEndDate(roomRateEntity.getEndDate());

                RoomType roomType = roomTypeSessionBean.viewRoomTypeDetails(roomTypeId);

                if (!roomType.getRoomRates().contains(roomRateEntityToUpdate)) {
                    roomType.getRoomRates().add(roomRateEntityToUpdate);
                }

                return roomRateEntityToUpdate;
            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }

        } else {
            throw new RoomRateNotFoundException("RoomRate ID not provided for room rate to be updated");
        }
    }

    @Override
    public void deleteRoomRate(Long roomRateId) throws RoomRateNotFoundException {
        RoomRate roomRateEntityToRemove = viewRoomRateDetails(roomRateId);

        List<Reservation> reservations = roomRateEntityToRemove.getReservations();

        if (reservations.isEmpty()) {               //Check that all reservations using this rate has passed then can delete
            em.remove(roomRateEntityToRemove);
        } else {
            roomRateEntityToRemove.setDisabled(true);
        }
    }

    @Override
    public List<RoomRate> viewAllRoomRates() {
        Query query = em.createQuery("SELECT rr FROM RoomRate rr");
        return query.getResultList();
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<RoomRate>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }

}
