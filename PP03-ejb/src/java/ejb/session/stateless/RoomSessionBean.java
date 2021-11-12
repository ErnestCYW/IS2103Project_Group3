/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Room;
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
import util.exception.RoomNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class RoomSessionBean implements RoomSessionBeanRemote, RoomSessionBeanLocal {

    @EJB
    private RoomTypeSessionBeanLocal roomTypeSessionBean;

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public RoomSessionBean() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public Room createNewRoom(Long roomTypeId, Room newRoomEntity) throws UnknownPersistenceException, InputDataValidationException, RoomTypeNotFoundException {

        Set<ConstraintViolation<Room>> constraintViolations = validator.validate(newRoomEntity);

        if (constraintViolations.isEmpty()) {

            try {

                RoomType roomType = roomTypeSessionBean.viewRoomTypeDetails(roomTypeId);
                
                if (roomType.isDisabled()) {
                    //Exception
                }
                else {
                    newRoomEntity.setRoomType(roomType);
                    roomType.getRooms().add(newRoomEntity);

                    em.persist(newRoomEntity);
                    em.flush();
                }
                
                return newRoomEntity;
            } catch (PersistenceException ex) {
                throw new UnknownPersistenceException(ex.getMessage());
            }
        } else {
            throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
        }
    }

    private Room viewRoom(Long roomId) throws RoomNotFoundException {
        Room roomEntity = em.find(Room.class, roomId);

        if (roomEntity != null) {
            return roomEntity;
        } else {
            throw new RoomNotFoundException("Room ID " + roomId + " does not exists!");
        }
    }

    @Override
    public Room updateRoom(Long roomTypeId, Room roomEntity) throws RoomTypeNotFoundException, InputDataValidationException, RoomNotFoundException {
        if (roomEntity != null && roomEntity.getRoomId() != null) {

            Set<ConstraintViolation<Room>> constraintViolations = validator.validate(roomEntity);

            if (constraintViolations.isEmpty()) {
                Room roomEntityToUpdate = viewRoom(roomEntity.getRoomId());

                roomEntityToUpdate.setNumber(roomEntity.getNumber());
                roomEntityToUpdate.setStatus(roomEntity.getStatus());

                RoomType roomType = roomTypeSessionBean.viewRoomTypeDetails(roomTypeId);

                roomEntityToUpdate.setRoomType(roomType);
                roomType.getRooms().add(roomEntityToUpdate);

                return roomEntityToUpdate;
            } else {
                throw new InputDataValidationException(prepareInputDataValidationErrorsMessage(constraintViolations));
            }
        } else {
            throw new RoomTypeNotFoundException("RoomType ID not provided for room type to be updated");
        }
    }

    @Override
    public void deleteRoom(Long roomId) throws RoomNotFoundException {
        Room roomEntityToRemove = viewRoom(roomId);

        if (roomEntityToRemove.getCurrentReservation() == null) {
            RoomType roomType = roomEntityToRemove.getRoomType();
            roomType.getRooms().remove(roomEntityToRemove);

            em.remove(roomEntityToRemove);
        } else {
            roomEntityToRemove.setDisabled(true);
        }
    }

    @Override
    public List<Room> viewAllRooms() {
        Query query = em.createQuery("SELECT r FROM Room r ORDER BY r.roomId ASC");

        return query.getResultList();
    }

    private String prepareInputDataValidationErrorsMessage(Set<ConstraintViolation<Room>> constraintViolations) {
        String msg = "Input data validation error!:";

        for (ConstraintViolation constraintViolation : constraintViolations) {
            msg += "\n\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage();
        }

        return msg;
    }

}
