/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateless.RoomRateSessionBeanRemote;
import ejb.session.stateless.RoomSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import entity.Employee;
import entity.Room;
import entity.RoomRate;
import entity.RoomType;
import java.util.Scanner;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.enumeration.EmployeeRoleEnum;
import util.enumeration.RoomRateTypeEnum;
import util.exception.InputDataValidationException;
import util.exception.InvalidEmployeeRoleException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
public class HotelOperationModule {
    
    private final ValidatorFactory validatorFactory;
    private final Validator validator;
    
    private RoomSessionBeanRemote roomSessionBeanRemote;
    private RoomRateSessionBeanRemote roomRateSessionBeanRemote;
    private RoomTypeSessionBeanRemote roomTypeSessionBeanRemote;
    
    private Employee loggedInEmployee;

    public HotelOperationModule() {
        
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    
    }

    public HotelOperationModule(RoomSessionBeanRemote roomSessionBeanRemote, RoomRateSessionBeanRemote roomRateSessionBeanRemote, RoomTypeSessionBeanRemote roomTypeSessionBeanRemote, Employee loggedInEmployee) {
        this.roomSessionBeanRemote = roomSessionBeanRemote;
        this.roomRateSessionBeanRemote = roomRateSessionBeanRemote;
        this.roomTypeSessionBeanRemote = roomTypeSessionBeanRemote;
        this.loggedInEmployee = loggedInEmployee;
    }

    
    
    public void menuHotelOperation() throws InvalidEmployeeRoleException
    {
        if(loggedInEmployee.getEmployeeRoleEnum() == EmployeeRoleEnum.GUEST_RELATION)
        {
            throw new InvalidEmployeeRoleException("You don't have rights to access the Hotel Operation Module.");
        }
        
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        while(true)
        {
            System.out.println("*** HoRS Management System :: Hotel Operation ***\n");
            System.out.println("1: Create New Room Type");
            System.out.println("2: View Room Type Details");
            System.out.println("3: View All Room Types");
            System.out.println("4: Create New Room");
            System.out.println("5: Update Room");
            System.out.println("6: Delete Room");
            System.out.println("7: View All Rooms");
            System.out.println("8: View Room Allocation Exception Report");
            System.out.println("9: Create New Room Rate");
            System.out.println("10: View Room Rate Details");
            System.out.println("11: View All Room Rates");
            System.out.println("12: Back\n");
            response = 0;
            
            while(response < 1 || response > 12)
            {
                System.out.print("> ");
                
                response = scanner.nextInt();
                
                if(response == 1)
                {
                    doCreateNewRoomType();
                }
                else if(response == 2)
                {
                    doRoomTypeDetails();
                }
                else if(response == 3)
                {
                    //doCreateNewPartner();
                }
                else if(response == 4)
                {
                    doCreateNewRoom();
                }
                else if(response == 5)
                {
                    break;
                }
                else if(response == 6)
                {
                    //doViewAllEmployees();
                }
                else if(response == 7)
                {
                    //doCreateNewPartner();
                }
                else if(response == 8)
                {
                    //doViewAllPartners();
                }
                else if(response == 9)
                {
                    doCreateNewRoomRate();
                }
                else if(response == 10)
                {
                    //doViewAllEmployees();
                }
                else if(response == 11)
                {
                    //doCreateNewPartner();
                }
                else if(response == 12)
                {
                    break;
                }
                else
                {
                    System.out.println("Invalid option, please try again!\n");
                }
            }
            
            if(response == 12)
            {
                break;
            }
        }   
    }
    
    private void doCreateNewRoomType()
    {
        Scanner scanner = new Scanner(System.in);
        RoomType newRoomType = new RoomType();
        
        System.out.println("*** Create New Room Type ***\n");
        System.out.print("Enter Room Type Name> ");
        newRoomType.setName(scanner.nextLine().trim());
        System.out.print("Enter Description> ");
        newRoomType.setDescription(scanner.nextLine().trim());
        System.out.print("Enter Size> ");
        newRoomType.setSize(scanner.nextLine().trim());
        System.out.print("Enter Bed> ");
        newRoomType.setBed(scanner.nextLine().trim());
        System.out.print("Enter Capacity> ");
        newRoomType.setCapacity(scanner.nextInt());
 
        
        try {
            RoomType createdRoomType = roomTypeSessionBeanRemote.createNewRoomType(newRoomType);
            System.out.println("Room Type ID " + createdRoomType.getRoomTypeId() + " created successfully!");
        } catch (UnknownPersistenceException ex) {
            System.out.println(ex.getMessage());
        } catch (InputDataValidationException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    private void doViewRoomTypeDetails() 
    {
        
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        System.out.println("*** View Room Type Details ***\n");
        System.out.print("Enter Room Type ID> ");
        Long roomTypeId = scanner.nextLong();
        
        try
        {
            RoomType roomType = roomTypeSessionBeanRemote.viewRoomTypeDetails(roomTypeId);
            System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", "Room Type ID", "Name", "Description", "Size", "Bed", "Capacity", "Disabled");
            System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", roomType.getRoomTypeId(), roomType.getName(), roomType.getDescription(), roomType.getSize(), roomType.getBed(), roomType.getCapacity(), roomType.isDisabled());         
            System.out.println("------------------------");
            System.out.println("1: Update Room Type");
            System.out.println("2: Delete Room Type");
            System.out.println("3: Back\n");
            System.out.print("> ");
            response = scanner.nextInt();

            if(response == 1)
            {
                doUpdateRoomType(roomType);
            }
            else if(response == 2)
            {
                doDeleteRoomType(roomType);
            }
        }
        catch(RoomTypeNotFoundException ex)
        {
            System.out.println(ex.getMessage() + "\n");
        }
    }
        
    private void doUpdateRoomType(RoomType roomType)
    {
        Scanner scanner = new Scanner(System.in);        
        String input;
        Integer inputInt;
        
        System.out.println("*** Update Room Type Details ***\n");
        System.out.print("Enter Name (blank if no change)> ");
        input = scanner.nextLine().trim();
        if(input.length() > 0)
        {
            roomType.setName(input);
        }
                
        System.out.print("Enter Description (blank if no change)> ");
        input = scanner.nextLine().trim();
        if(input.length() > 0)
        {
            roomType.setDescription(input);
        }
        
        System.out.print("Enter Size (blank if no change)> ");
        input = scanner.nextLine().trim();
        if(input.length() > 0)
        {
            roomType.setSize(input);
        }
        
        System.out.print("Enter Bed (blank if no change)> ");
        input = scanner.nextLine().trim();
        if(input.length() > 0)
        {
            roomType.setBed(input);
        }
        
        System.out.print("Enter Capacity (-1 if no change)> ");
        inputInt = scanner.nextInt();
        if(inputInt >= 0)
        {
            roomType.setCapacity(inputInt);
        }
        
        System.out.print("Enable / Disable (blank if no change)> ");
        input = scanner.nextLine().trim();
        if(input.length() > 0)
        {
            roomType.setDisabled(!roomType.isDisabled());
        }
        
        Set<ConstraintViolation<RoomType>>constraintViolations = validator.validate(roomType);
        
        if(constraintViolations.isEmpty())
        {
            try
            {
                roomTypeSessionBeanRemote.updateRoomType(roomType);
                System.out.println("Staff updated successfully!\n");
            }
            catch (RoomTypeNotFoundException ex) 
            {
                System.out.println(ex.getMessage() + "\n");
            }
            catch(InputDataValidationException ex)
            {
                System.out.println(ex.getMessage() + "\n");
            }
        }
        else
        {
            showInputDataValidationErrorsForRoomType(constraintViolations);
        }
    
    }
    
    
    
    
    
    // Newly added in v4.2
    
    private void showInputDataValidationErrorsForProductEntity(Set<ConstraintViolation<ProductEntity>>constraintViolations)
    {
        System.out.println("\nInput data validation error!:");
            
        for(ConstraintViolation constraintViolation:constraintViolations)
        {
            System.out.println("\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage());
        }

        System.out.println("\nPlease try again......\n");
    }
    
    
    
    private void doDeleteRoomType(RoomType roomType)
    {
        
    }
    
    private void doCreateNewRoom()
    {
        Scanner scanner = new Scanner(System.in);
        Room newRoom = new Room();
        
        System.out.println("*** Create New Room ***\n");
        System.out.print("Enter Room Number> ");
        newRoom.setNumber(scanner.nextLine().trim());
        
        System.out.print("Enter Room Type Name> ");
        String roomTypeName = scanner.nextLine().trim();
        
        try
        {
            RoomType roomType = roomTypeSessionBeanRemote.retrieveRoomTypeByName(roomTypeName);
        
            try 
            {
                Room createdRoom = roomSessionBeanRemote.createNewRoom(roomType.getRoomTypeId(), newRoom);
                System.out.println("Room ID " + createdRoom.getRoomId() + " created successfully!");
            }
            catch(UnknownPersistenceException | InputDataValidationException ex)
            {
                System.out.println(ex.getMessage());
            }
        }
        catch(RoomTypeNotFoundException ex)
        {
            System.out.println(ex.getMessage());
        }
        
    }
    
    private void doCreateNewRoomRate() {
    
        Scanner scanner = new Scanner(System.in);
        RoomRate newRoomRate = new RoomRate();
        
        System.out.println("*** Create New Room Rate ***\n");
        System.out.print("Enter Name> ");
        newRoomRate.setName(scanner.nextLine().trim());
        
        while(true)
        {
            System.out.print("Select Room Rate Type (1: Published, 2: Normal, 3: Peak, 4: Promotion)> ");
            Integer roomRateInt = scanner.nextInt();
            
            if(roomRateInt >= 1 && roomRateInt <= 4)
            {
                newRoomRate.setRoomRateType(RoomRateTypeEnum.values()[roomRateInt-1]);
                
                if(roomRateInt >= 3)
                {
                    System.out.print("Enter start date> ");
                    String startDateStr = scanner.nextLine().trim();
                    //convert string to date
                    
                    System.out.print("Enter end date> ");
                    String endDateStr = scanner.nextLine().trim();
                    //convert string to date
                }
                break;
            }
            else
            {
                System.out.println("Invalid option, please try again\n");
            }
        }
        
        System.out.print("Enter Room Rate Price> ");
        newRoomRate.setRate(scanner.nextBigDecimal());
        
        
        System.out.print("Enter Room Type Name> ");
        String roomTypeName = scanner.nextLine().trim();
        
        try
        {
            RoomType roomType = roomTypeSessionBeanRemote.retrieveRoomTypeByName(roomTypeName);
        
            try 
            {
                RoomRate createdRoomRate = roomRateSessionBeanRemote.createNewRoomRate(roomType.getRoomTypeId(), newRoomRate);
                System.out.println("Room Rate ID " + createdRoomRate.getRoomRateId() + " created successfully!");
            }
            catch(UnknownPersistenceException | InputDataValidationException ex)
            {
                System.out.println(ex.getMessage());
            }
        }
        catch(RoomTypeNotFoundException ex)
        {
            System.out.println(ex.getMessage());
        }
        
    }
    
    private void doViewRoomRateDetails() 
    {
        
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        System.out.println("*** View Room Rate Details ***\n");
        System.out.print("Enter Room Rate ID> ");
        Long roomRateId = scanner.nextLong();
        
        try
        {
            RoomRate roomRate = roomRateSessionBeanRemote.viewRoomRateDetails(roomRateId);
            if (roomRate.getRoomRateType() == RoomRateTypeEnum.NORMAL && roomRate.getRoomRateType() == RoomRateTypeEnum.PUBLISHED) 
            {
                System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", "Room Rate ID", "Name", "Type", );
                System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", roomRate.getRoomRateId(), roomRate.getName(), roomRate.getRoomRateType().toString(), );         
            }
            System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", "Room Rate ID", "Name", "Type", );
            System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", roomRate.getRoomRateId(), roomRate.getName(), roomRate.getRoomRateType().toString(), );         
            System.out.println("------------------------");
            System.out.println("1: Update Room Type");
            System.out.println("2: Delete Room Type");
            System.out.println("3: Back\n");
            System.out.print("> ");
            response = scanner.nextInt();

            if(response == 1)
            {
                doUpdateRoomType(roomType);
            }
            else if(response == 2)
            {
                doDeleteRoomType(roomType);
            }
        }
        catch(RoomTypeNotFoundException ex)
        {
            System.out.println(ex.getMessage() + "\n");
        }
    }
        
    private void showInputDataValidationErrorsForRoomType(Set<ConstraintViolation<RoomType>>constraintViolations)
    {
        System.out.println("\nInput data validation error!:");
            
        for(ConstraintViolation constraintViolation:constraintViolations)
        {
            System.out.println("\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage());
        }

        System.out.println("\nPlease try again......\n");
    }
    
        
        
    
}

