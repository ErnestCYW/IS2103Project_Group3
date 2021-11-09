/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.RoomRate;
import java.util.List;
import javax.ejb.Remote;
import util.exception.InputDataValidationException;
import util.exception.RoomRateNotFoundException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Remote
public interface RoomRateSessionBeanRemote {

    public RoomRate createNewRoomRate(Long roomTypeId, RoomRate newRoomRateEntity) throws InputDataValidationException, RoomTypeNotFoundException, UnknownPersistenceException;

    public RoomRate viewRoomRateDetails(Long roomRateId) throws RoomRateNotFoundException;

    public RoomRate updateRoomRate(Long roomTypeId, RoomRate roomRateEntity) throws InputDataValidationException, RoomRateNotFoundException, RoomTypeNotFoundException;

    public void deleteRoomRate(Long roomRateId) throws RoomRateNotFoundException;

    public List<RoomRate> viewAllRoomRates();

}
