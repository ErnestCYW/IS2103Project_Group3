/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.EmployeeSessionBeanLocal;
import ejb.session.stateless.PartnerSessionBeanLocal;
import ejb.session.stateless.ReservationSessionBeanLocal;
import ejb.session.stateless.RoomAllocationReportSessionBeanLocal;
import ejb.session.stateless.RoomRateSessionBeanLocal;
import ejb.session.stateless.RoomTypeSessionBeanLocal;
import ejb.session.stateless.RoomSessionBeanLocal;
import entity.Employee;
import entity.Partner;
import entity.Reservation;
import entity.RoomRate;
import entity.RoomType;
import java.math.BigDecimal;
import entity.Room;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.enumeration.EmployeeRoleEnum;
import util.enumeration.RoomRateTypeEnum;
import util.exception.CannotGetTodayDateException;
import util.exception.EmployeeNotFoundException;
import util.exception.EmployeeUsernameExistException;
import util.exception.InputDataValidationException;
import util.exception.PartnerNotFoundException;
import util.exception.PartnerUsernameExistException;
import util.exception.RoomTypeExistException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Singleton
@LocalBean
@Startup
public class DataInitSessionBean {
  
    @EJB
    private RoomAllocationReportSessionBeanLocal roomAllocationReportSessionBeanLocal;

    @EJB
    private RoomSessionBeanLocal roomSessionBeanLocal;

    @EJB
    private EmployeeSessionBeanLocal employeeSessionBeanLocal;
    
    @EJB
    private RoomTypeSessionBeanLocal roomTypeSessionBean;

    @EJB
    private RoomRateSessionBeanLocal roomRateSessionBean;
    
    @PostConstruct
    public void postConstruct() {
        try {
            employeeSessionBeanLocal.retrieveEmployeeByUsername("sysadmin");
        } catch (EmployeeNotFoundException ex) {
            initializeData();
        }
    }

    private void initializeData() {

        try {
            employeeSessionBeanLocal.createNewEmployee(new Employee("Employee", "1", "sysadmin", "password", EmployeeRoleEnum.SYSTEM_ADMIN));
            employeeSessionBeanLocal.createNewEmployee(new Employee("Employee", "2", "opmanager", "password", EmployeeRoleEnum.OPERATION));
            employeeSessionBeanLocal.createNewEmployee(new Employee("Employee", "3", "salesmanager", "password", EmployeeRoleEnum.SALES));
            employeeSessionBeanLocal.createNewEmployee(new Employee("Employee", "4", "guestrelo", "password", EmployeeRoleEnum.GUEST_RELATION));

            List<String> amenities = new ArrayList<>();
            amenities.add("Bath");
            Long deluxeRoomId = roomTypeSessionBean.createNewRoomType(new RoomType("Deluxe Room", "A Nice Room", "Small", "Single", 2, amenities, "Premier Room")).getRoomTypeId();
            amenities.add("Rain Shower");
            Long premierRoomId = roomTypeSessionBean.createNewRoomType(new RoomType("Premier Room", "A Nicer Room", "Medium", "Super Single", 2, amenities, "Family Room")).getRoomTypeId();
            amenities.add("Karaoke");
            Long familyRoomId = roomTypeSessionBean.createNewRoomType(new RoomType("Family Room", "An Even Nicer Room", "Large", "Queen", 4, amenities, "Junior Suite")).getRoomTypeId();
            amenities.add("Pool Table");
            Long juniorSuiteId = roomTypeSessionBean.createNewRoomType(new RoomType("Junior Suite", "An Almost Nicest Room", "Huge", "King", 4, amenities, "Grand Suite")).getRoomTypeId();
            amenities.add("Hot Tub");
            Long grandSuiteId = roomTypeSessionBean.createNewRoomType(new RoomType("Grand Suite", "THE NICEST ROOM", "ENORMOUS", "SUPER KING", 8, amenities)).getRoomTypeId();

            roomRateSessionBean.createNewRoomRate(deluxeRoomId, new RoomRate("Deluxe Room Published", RoomRateTypeEnum.PUBLISHED, new BigDecimal("100")));
            roomRateSessionBean.createNewRoomRate(deluxeRoomId, new RoomRate("Deluxe Room Normal", RoomRateTypeEnum.NORMAL, new BigDecimal("50")));
            roomRateSessionBean.createNewRoomRate(premierRoomId, new RoomRate("Premier Room Published", RoomRateTypeEnum.PUBLISHED, new BigDecimal("200")));
            roomRateSessionBean.createNewRoomRate(premierRoomId, new RoomRate("Premier Room Published", RoomRateTypeEnum.NORMAL, new BigDecimal("100")));
            roomRateSessionBean.createNewRoomRate(familyRoomId, new RoomRate("Family Room Published", RoomRateTypeEnum.PUBLISHED, new BigDecimal("300")));
            roomRateSessionBean.createNewRoomRate(familyRoomId, new RoomRate("Family Room Published", RoomRateTypeEnum.NORMAL, new BigDecimal("150")));
            roomRateSessionBean.createNewRoomRate(juniorSuiteId, new RoomRate("Junior Suite Published", RoomRateTypeEnum.PUBLISHED, new BigDecimal("400")));
            roomRateSessionBean.createNewRoomRate(juniorSuiteId, new RoomRate("Junior Suite Published", RoomRateTypeEnum.NORMAL, new BigDecimal("200")));
            roomRateSessionBean.createNewRoomRate(grandSuiteId, new RoomRate("Grand Suite Published", RoomRateTypeEnum.PUBLISHED, new BigDecimal("500")));
            roomRateSessionBean.createNewRoomRate(grandSuiteId, new RoomRate("Grand Suite Published", RoomRateTypeEnum.NORMAL, new BigDecimal("250")));

            roomSessionBeanLocal.createNewRoom(deluxeRoomId, new Room("0101"));
            roomSessionBeanLocal.createNewRoom(deluxeRoomId, new Room("0201"));
            roomSessionBeanLocal.createNewRoom(deluxeRoomId, new Room("0301"));
            roomSessionBeanLocal.createNewRoom(deluxeRoomId, new Room("0401"));
            roomSessionBeanLocal.createNewRoom(deluxeRoomId, new Room("0501"));
            roomSessionBeanLocal.createNewRoom(premierRoomId, new Room("0102"));
            roomSessionBeanLocal.createNewRoom(premierRoomId, new Room("0202"));
            roomSessionBeanLocal.createNewRoom(premierRoomId, new Room("0302"));
            roomSessionBeanLocal.createNewRoom(premierRoomId, new Room("0402"));
            roomSessionBeanLocal.createNewRoom(premierRoomId, new Room("0502"));
            roomSessionBeanLocal.createNewRoom(familyRoomId, new Room("0103")); 
            roomSessionBeanLocal.createNewRoom(familyRoomId, new Room("0203"));
            roomSessionBeanLocal.createNewRoom(familyRoomId, new Room("0303"));
            roomSessionBeanLocal.createNewRoom(familyRoomId, new Room("0403"));
            roomSessionBeanLocal.createNewRoom(familyRoomId, new Room("0503"));
            roomSessionBeanLocal.createNewRoom(juniorSuiteId, new Room("0104"));
            roomSessionBeanLocal.createNewRoom(juniorSuiteId, new Room("0204"));
            roomSessionBeanLocal.createNewRoom(juniorSuiteId, new Room("0304"));
            roomSessionBeanLocal.createNewRoom(juniorSuiteId, new Room("0404"));
            roomSessionBeanLocal.createNewRoom(juniorSuiteId, new Room("0504"));
            roomSessionBeanLocal.createNewRoom(grandSuiteId, new Room("0105"));
            roomSessionBeanLocal.createNewRoom(grandSuiteId, new Room("0205"));
            roomSessionBeanLocal.createNewRoom(grandSuiteId, new Room("0305"));
            roomSessionBeanLocal.createNewRoom(grandSuiteId, new Room("0405"));
            roomSessionBeanLocal.createNewRoom(grandSuiteId, new Room("0505"));
            
            roomAllocationReportSessionBeanLocal.createRoomAllocationReport();
           
            
        } catch (EmployeeUsernameExistException | UnknownPersistenceException | InputDataValidationException | RoomTypeExistException | RoomTypeNotFoundException | CannotGetTodayDateException ex) {

            ex.printStackTrace();
        }
    }
}
