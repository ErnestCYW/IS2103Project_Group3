/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Reservation;
import entity.Room;
import entity.RoomRate;
import entity.RoomType;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
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
public class RoomTypeSessionBean implements RoomTypeSessionBeanRemote, RoomTypeSessionBeanLocal {

    @EJB
    private RoomRateSessionBeanLocal roomRateSessionBean;

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    public RoomTypeSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public RoomType createNewRoomType(RoomType newRoomTypeEntity) throws UnknownPersistenceException, InputDataValidationException {

        Set<ConstraintViolation<RoomType>> constraintViolations = validator.validate(newRoomTypeEntity);

        if (constraintViolations.isEmpty()) {
            try {
                em.persist(newRoomTypeEntity);
                em.flush();

                return newRoomTypeEntity;
            } catch (PersistenceException ex) {
                throw new UnknownPersistenceException(ex.getMessage());
            }
        } else {
            throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
        }
    }

    @Override
    public RoomType viewRoomTypeDetails(Long roomTypeId) throws RoomTypeNotFoundException {

        RoomType roomTypeEntity = em.find(RoomType.class, roomTypeId);

        if (roomTypeEntity != null) {
            return roomTypeEntity;
        } else {
            throw new RoomTypeNotFoundException("RoomType ID " + roomTypeId + " does not exists!");
        }
    }

    public RoomType updateRoomType(RoomType roomTypeEntity) throws RoomTypeNotFoundException, InputDataValidationException {
        if (roomTypeEntity != null && roomTypeEntity.getRoomTypeId() != null) {

            Set<ConstraintViolation<RoomType>> constraintViolations = validator.validate(roomTypeEntity);

            if (constraintViolations.isEmpty()) {
                RoomType roomTypeEntityToUpdate = viewRoomTypeDetails(roomTypeEntity.getRoomTypeId());

                roomTypeEntityToUpdate.setName(roomTypeEntity.getName());
                roomTypeEntityToUpdate.setDescription(roomTypeEntity.getDescription());
                roomTypeEntityToUpdate.setSize(roomTypeEntity.getSize());
                roomTypeEntityToUpdate.setBed(roomTypeEntity.getBed());
                roomTypeEntityToUpdate.setAmenities(roomTypeEntity.getAmenities());

                return roomTypeEntityToUpdate;
            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }
        } else {
            throw new RoomTypeNotFoundException("RoomType ID not provided for room type to be updated");
        }
    }

    @Override
    public void deleteRoomType(Long roomTypeId) throws RoomTypeNotFoundException, RoomRateNotFoundException {
        RoomType roomTypeEntityToRemove = viewRoomTypeDetails(roomTypeId);

        List<Room> rooms = roomTypeEntityToRemove.getRooms();
        List<Reservation> reservations = roomTypeEntityToRemove.getReservations();

        if (rooms.isEmpty() & reservations.isEmpty()) {
            List<RoomRate> roomRates = roomTypeEntityToRemove.getRoomRates();

            for (RoomRate roomRate : roomRates) {
                if (roomRate.getReservations().isEmpty()) {
                    roomRateSessionBean.deleteRoomRate(roomRate.getRoomRateId());
                } else {
                    roomTypeEntityToRemove.setDisabled(true);   //Somehow roomRate is in use, should never occur
                    return;
                }
            }                                                                   
            em.remove(roomTypeEntityToRemove);
        } else {
            roomTypeEntityToRemove.setDisabled(true);
        }
    }

    @Override
    public List<RoomType> viewAllRoomTypes() {
        Query query = em.createQuery("SELECT rt FROM RoomType rt ORDER BY rt.roomTypeId ASC");
        return query.getResultList();
    }
    
    @Override
    public RoomType retrieveRoomTypeByName(String roomTypeName) throws RoomTypeNotFoundException {
        Query query = em.createQuery("SELECT rt FROM RoomType rt WHERE rt.name = :inName");
        query.setParameter("inName", roomTypeName);
        try
        {
            return (RoomType)query.getSingleResult();
        }
        catch(NoResultException | NonUniqueResultException ex)
        {
            throw new RoomTypeNotFoundException("Room Type " + roomTypeName + " does not exist!");
        }
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<RoomType>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }

}
