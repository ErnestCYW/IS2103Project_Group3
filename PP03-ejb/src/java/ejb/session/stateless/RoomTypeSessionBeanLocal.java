/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.RoomType;
import java.util.List;
import javax.ejb.Local;
import util.exception.InputDataValidationException;
import util.exception.RoomRateNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Local
public interface RoomTypeSessionBeanLocal {

    public RoomType createNewRoomType(RoomType newRoomTypeEntity) throws UnknownPersistenceException, InputDataValidationException;

    public RoomType viewRoomTypeDetails(Long roomTypeId) throws RoomTypeNotFoundException;

    public void deleteRoomType(Long roomTypeId) throws RoomTypeNotFoundException, RoomRateNotFoundException;

    public List<RoomType> viewAllRoomTypes();

    public RoomType updateRoomType(RoomType roomTypeEntity) throws RoomTypeNotFoundException, InputDataValidationException;
    
}
