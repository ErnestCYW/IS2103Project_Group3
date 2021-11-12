/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.RoomType;
import java.util.List;
import javax.ejb.Remote;
import util.exception.InputDataValidationException;
import util.exception.RoomRateNotFoundException;
import util.exception.RoomTypeExistException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Remote
public interface RoomTypeSessionBeanRemote {

    public RoomType createNewRoomType(RoomType newRoomTypeEntity) throws UnknownPersistenceException, InputDataValidationException, RoomTypeExistException;

    public RoomType viewRoomTypeDetails(Long roomTypeId) throws RoomTypeNotFoundException;

    public void deleteRoomType(Long roomTypeId) throws RoomTypeNotFoundException;

    public List<RoomType> viewAllRoomTypes();

    public RoomType updateRoomType(RoomType roomTypeEntity) throws RoomTypeNotFoundException, InputDataValidationException, RoomTypeExistException, UnknownPersistenceException;

    public RoomType retrieveRoomTypeByName(String roomTypeName) throws RoomTypeNotFoundException;
}
