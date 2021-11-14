/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateless.AllocationSessionBeanRemote;
import ejb.session.stateless.HandleDateTimeSessionBeanRemote;
import ejb.session.stateless.RoomAllocationReportSessionBeanRemote;
import ejb.session.stateless.RoomRateSessionBeanRemote;
import ejb.session.stateless.RoomSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import entity.Employee;
import entity.Reservation;
import entity.Room;
import entity.RoomAllocationReport;
import entity.RoomRate;
import entity.RoomType;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.enumeration.EmployeeRoleEnum;
import util.enumeration.RoomRateTypeEnum;
import util.enumeration.RoomStatusEnum;
import util.exception.CannotGetTodayDateException;
import util.exception.InputDataValidationException;
import util.exception.InvalidEmployeeRoleException;
import util.exception.RoomAllocationReportNotFoundException;
import util.exception.RoomNotFoundException;
import util.exception.RoomRateNotFoundException;
import util.exception.RoomTypeExistException;
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
    private HandleDateTimeSessionBeanRemote handleDateTimeSessionBeanRemote;
    private RoomAllocationReportSessionBeanRemote roomAllocationReportSessionBeanRemote;
    private AllocationSessionBeanRemote allocationSessionBeanRemote;

    private Employee loggedInEmployee;

    public HotelOperationModule() {

        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();

    }

    public HotelOperationModule(RoomSessionBeanRemote roomSessionBeanRemote,
            RoomRateSessionBeanRemote roomRateSessionBeanRemote,
            RoomTypeSessionBeanRemote roomTypeSessionBeanRemote,
            HandleDateTimeSessionBeanRemote handleDateTimeSessionBeanRemote,
            RoomAllocationReportSessionBeanRemote roomAllocationReportSessionBeanRemote,
            AllocationSessionBeanRemote allocationSessionBeanRemote,
            Employee loggedInEmployee) {
        this();
        this.roomSessionBeanRemote = roomSessionBeanRemote;
        this.roomRateSessionBeanRemote = roomRateSessionBeanRemote;
        this.roomTypeSessionBeanRemote = roomTypeSessionBeanRemote;
        this.handleDateTimeSessionBeanRemote = handleDateTimeSessionBeanRemote;
        this.roomAllocationReportSessionBeanRemote = roomAllocationReportSessionBeanRemote;
        this.allocationSessionBeanRemote = allocationSessionBeanRemote;
        this.loggedInEmployee = loggedInEmployee;
    }

    public void menuHotelOperation() throws InvalidEmployeeRoleException {
        if (loggedInEmployee.getEmployeeRoleEnum() == EmployeeRoleEnum.GUEST_RELATION) {
            throw new InvalidEmployeeRoleException("You don't have rights to access the Hotel Operation Module.");
        }

        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** HoRS Management System :: Hotel Operation ***\n");
            System.out.println("1: Create New Room Type");
            System.out.println("2: View Room Type Details");
            System.out.println("3: View All Room Types");
            System.out.println("4: Create New Room");
            System.out.println("5: Update Room ");
            System.out.println("6: Delete Room");
            System.out.println("7: View All Rooms");
            System.out.println("8: Allocate Rooms (TESTING ONLY)");
            System.out.println("9: View Room Allocation Exception Report");
            System.out.println("10: Create New Room Rate");
            System.out.println("11: View Room Rate Details");
            System.out.println("12: View All Room Rates");
            System.out.println("13: Back\n");
            response = 0;

            while (response < 1 || response > 12) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    doCreateNewRoomType();
                } else if (response == 2) {
                    doViewRoomTypeDetails();
                } else if (response == 3) {
                    doViewAllRoomTypes();
                } else if (response == 4) {
                    doCreateNewRoom();
                } else if (response == 5) {
                    doUpdateRoom();
                } else if (response == 6) {
                    doDeleteRoom();
                } else if (response == 7) {
                    doViewAllRooms();
                } else if (response == 8) {
                    doAllocateRoom();
                } else if (response == 9) {
                    doViewRoomAllocationReport();
                } else if (response == 10) {
                    doCreateNewRoomRate();
                } else if (response == 11) {
                    doViewRoomRateDetails();
                } else if (response == 12) {
                    doViewAllRoomRates();
                } else if (response == 13) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 13) {
                break;
            }
        }
    }

    private void doCreateNewRoomType() {
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

        scanner.nextLine();
        List<String> amenities = new ArrayList<>();
        while (true) {

            System.out.print("Enter Amenities (Enter q When Done) > ");
            String amenity = scanner.nextLine().trim();
            if (amenity.equals("q")) {
                break;
            } else {
                amenities.add(amenity);
            }

        }
        newRoomType.setAmenities(amenities);

        System.out.print("Enter Next Higher Room Type (If None, Enter 'blank') > ");
        String nextHigherRoomType = scanner.nextLine().trim();
        if (nextHigherRoomType.length() > 0) {
            newRoomType.setNextHigherRoomType(nextHigherRoomType);
        }

        try {
            RoomType createdRoomType = roomTypeSessionBeanRemote.createNewRoomType(newRoomType);
            System.out.println("Room Type ID " + createdRoomType.getRoomTypeId() + " created successfully!");
        } catch (UnknownPersistenceException | RoomTypeExistException ex) {
            System.out.println(ex.getMessage());
        } catch (InputDataValidationException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void doViewRoomTypeDetails() {

        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        System.out.println("*** View Room Type Details ***\n");
        System.out.print("Enter Room Type ID> ");
        Long roomTypeId = scanner.nextLong();

        try {
            RoomType roomType = roomTypeSessionBeanRemote.viewRoomTypeDetails(roomTypeId);
            System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", "Room Type ID", "Name", "Description", "Size", "Bed", "Capacity", "Number Amenities", "Disabled");
            System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", roomType.getRoomTypeId(), roomType.getName(), roomType.getDescription(), roomType.getSize(), roomType.getBed(), roomType.getCapacity(), roomType.getAmenities(), printDisabled(roomType.isDisabled()));
            System.out.println("------------------------");
            System.out.println("1: Update Room Type");
            System.out.println("2: Delete Room Type");
            System.out.println("3: Back\n");
            System.out.print("> ");
            response = scanner.nextInt();

            if (response == 1) {
                doUpdateRoomType(roomType);
            } else if (response == 2) {
                doDeleteRoomType(roomType);
            }
        } catch (RoomTypeNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private void doUpdateRoomType(RoomType roomType) {
        Scanner scanner = new Scanner(System.in);
        String input;
        Integer inputInt;

        System.out.println("*** Update Room Type Details ***\n");
        System.out.print("Enter Name (blank if no change)> ");
        input = scanner.nextLine().trim();
        if (input.length() > 0) {
            roomType.setName(input);
        }

        System.out.print("Enter Description (blank if no change)> ");
        input = scanner.nextLine().trim();
        if (input.length() > 0) {
            roomType.setDescription(input);
        }

        System.out.print("Enter Size (blank if no change)> ");
        input = scanner.nextLine().trim();
        if (input.length() > 0) {
            roomType.setSize(input);
        }

        System.out.print("Enter Bed (blank if no change)> ");
        input = scanner.nextLine().trim();
        if (input.length() > 0) {
            roomType.setBed(input);
        }

        System.out.print("Enter Capacity (-1 if no change)> ");
        inputInt = scanner.nextInt();
        if (inputInt >= 0) {
            roomType.setCapacity(inputInt);
        }

        scanner.nextLine();
        List<String> amenities = new ArrayList<>();
        while (true) {

            System.out.print("Enter Amenities (Enter q When Done) > ");
            String amenity = scanner.nextLine().trim();
            if (amenity.equals("q")) {
                break;
            } else {
                amenities.add(amenity);
            }

        }
        roomType.setAmenities(amenities);

        Set<ConstraintViolation<RoomType>> constraintViolations = validator.validate(roomType);

        if (constraintViolations.isEmpty()) {
            try {
                roomTypeSessionBeanRemote.updateRoomType(roomType);
                System.out.println("Room Type updated successfully!\n");
            } catch (RoomTypeNotFoundException | RoomTypeExistException | UnknownPersistenceException ex) {
                System.out.println(ex.getMessage() + "\n");
            } catch (InputDataValidationException ex) {
                System.out.println(ex.getMessage() + "\n");
            }
        } else {
            showInputDataValidationErrorsForRoomType(constraintViolations);
        }

    }

    private void doDeleteRoomType(RoomType roomType) {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("*** Delete Room Type ***\n");
        System.out.printf("Confirm Delete %s (Enter 'Y' to Delete)> ", roomType.getName());
        input = scanner.nextLine().trim();

        if (input.equals("Y")) {
            try {
                Long roomTypeId = roomType.getRoomTypeId();
                roomTypeSessionBeanRemote.deleteRoomType(roomTypeId);

                try {
                    roomTypeSessionBeanRemote.viewRoomTypeDetails(roomTypeId);
                    System.out.println(roomType.getName() + " disabled!\n");
                } catch (RoomTypeNotFoundException ex) {
                    System.out.println(roomType.getName() + " deleted!\n");
                }

            } catch (RoomTypeNotFoundException ex) {
                System.out.println(ex.getMessage() + "\n");
            }
        } else {
            System.out.println("Room Type NOT deleted!\n");
        }
    }

    private void doViewAllRoomTypes() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("*** View All Room Types ***\n");

        List<RoomType> roomTypes = roomTypeSessionBeanRemote.viewAllRoomTypes();
        System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", "Room Type ID", "Name", "Description", "Size", "Bed", "Capacity", "Disabled");

        for (RoomType roomType : roomTypes) {
            System.out.printf("%8s%20s%20s%15s%20s%10s%10s\n", roomType.getRoomTypeId(), roomType.getName(), roomType.getDescription(), roomType.getSize(), roomType.getBed(), roomType.getCapacity(), printDisabled(roomType.isDisabled()));
        }

        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }

    private void doCreateNewRoom() {
        Scanner scanner = new Scanner(System.in);
        Room newRoom = new Room();

        System.out.println("*** Create New Room ***\n");
        System.out.print("Enter Room Number> ");
        newRoom.setNumber(scanner.nextLine().trim());

        System.out.print("Enter Room Type Name> ");
        String roomTypeName = scanner.nextLine().trim();

        try {
            RoomType roomType = roomTypeSessionBeanRemote.retrieveRoomTypeByName(roomTypeName);

            try {
                Room createdRoom = roomSessionBeanRemote.createNewRoom(roomType.getRoomTypeId(), newRoom);
                System.out.println("Room ID " + createdRoom.getRoomId() + " created successfully!");
            } catch (UnknownPersistenceException | InputDataValidationException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (RoomTypeNotFoundException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void doUpdateRoom() {
        Scanner scanner = new Scanner(System.in);
        String input;
        Integer inputInt;

        System.out.println("*** Update Room Details ***\n");
        System.out.print("Enter Room Number (blank if no change)> ");

        try {
            Room room = roomSessionBeanRemote.retrieveRoomByRoomNumber(scanner.nextLine().trim());
            Long roomTypeId = room.getRoomType().getRoomTypeId();

            while (true) {
                System.out.print("Select Room Availability (1: Available, 2: Unavailable)> ");
                inputInt = scanner.nextInt();
                scanner.nextLine();
                //Integer previousRoomRateTypeInt = roomRate.getRoomRateType().ordinal() + 1;

                if (inputInt >= 1 && inputInt <= 2) {
                    room.setStatus(RoomStatusEnum.values()[inputInt - 1]);
                    break;
                } else {
                    System.out.println("Invalid option, please try again\n");
                }
            }

            System.out.print("Enter Room Type ID (0 if no change)> ");
            Long newRoomTypeId = scanner.nextLong();
            if (newRoomTypeId > 0) {
                roomTypeId = newRoomTypeId;
            }

            Set<ConstraintViolation<Room>> constraintViolations = validator.validate(room);

            if (constraintViolations.isEmpty()) {
                try {
                    roomSessionBeanRemote.updateRoom(roomTypeId, room);
                    System.out.println("Room updated successfully!\n");
                } catch (RoomNotFoundException | RoomTypeNotFoundException ex) {
                    System.out.println(ex.getMessage() + "\n");
                } catch (InputDataValidationException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
            } else {
                showInputDataValidationErrorsForRoom(constraintViolations);
            }
        } catch (RoomNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void doDeleteRoom() {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("*** Delete Room ***\n");
        System.out.print("Enter Room Number to Delete> ");
        String roomNum = scanner.nextLine().trim();

        try {
            Room roomToDelete = roomSessionBeanRemote.retrieveRoomByRoomNumber(roomNum);
            Long roomToDeleteId = roomToDelete.getRoomId();

            System.out.print("Enter 'Y' to confirm deletion> ");
            input = scanner.nextLine().trim();

            if (input.equals("Y")) {
                try {
                    roomSessionBeanRemote.deleteRoom(roomToDeleteId);

                    try {
                        roomSessionBeanRemote.retrieveRoomByRoomNumber(roomNum);
                        System.out.println(roomNum + " disabled!\n");
                    } catch (RoomNotFoundException ex) {
                        System.out.println(roomNum + " deleted!\n");
                    }

                } catch (RoomNotFoundException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
            } else {
                System.out.println("Room NOT deleted!\n");
            }
        } catch (RoomNotFoundException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void doViewAllRooms() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("*** View All Rooms ***\n");

        List<Room> rooms = roomSessionBeanRemote.viewAllRooms();
        System.out.printf("%20s%20s%20s%20s%20s\n", "Room ID", "Room Number", "Room Type", "Availability", "Disabled");

        for (Room room : rooms) {
            System.out.printf("%20s%20s%20s%20s%20s\n", room.getRoomId(), room.getNumber(), room.getRoomType().getName(), room.getStatus().toString(), printDisabled(room.isDisabled()));
        }

        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }

    private void doCreateNewRoomRate() {

        Scanner scanner = new Scanner(System.in);
        RoomRate newRoomRate = new RoomRate();

        System.out.println("*** Create New Room Rate ***\n");
        System.out.print("Enter Name> ");
        newRoomRate.setName(scanner.nextLine().trim());

        while (true) {
            System.out.print("Select Room Rate Type (1: Published, 2: Normal, 3: Peak, 4: Promotion)> ");
            Integer roomRateInt = scanner.nextInt();
            scanner.nextLine();

            if (roomRateInt >= 1 && roomRateInt <= 4) {
                newRoomRate.setRoomRateType(RoomRateTypeEnum.values()[roomRateInt - 1]);

                if (roomRateInt >= 3) {
                    try {
                        System.out.print("Enter start date> ");
                        String startDateStr = scanner.nextLine().trim();
                        newRoomRate.setStartDate(handleDateTimeSessionBeanRemote.convertStringInputToDate(startDateStr));

                        System.out.print("Enter end date> ");
                        String endDateStr = scanner.nextLine().trim();
                        newRoomRate.setEndDate(handleDateTimeSessionBeanRemote.convertStringInputToDate(endDateStr));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }

                }
                break;
            } else {
                System.out.println("Invalid option, please try again\n");
            }
        }

        System.out.print("Enter Room Rate Price> ");
        newRoomRate.setRate(scanner.nextBigDecimal());
        scanner.nextLine();

        System.out.print("Enter Room Type Name> ");
        String roomTypeName = scanner.nextLine().trim();

        try {
            RoomType roomType = roomTypeSessionBeanRemote.retrieveRoomTypeByName(roomTypeName);

            try {
                RoomRate createdRoomRate = roomRateSessionBeanRemote.createNewRoomRate(roomType.getRoomTypeId(), newRoomRate);
                System.out.println("Room Rate ID " + createdRoomRate.getRoomRateId() + " created successfully!");
            } catch (UnknownPersistenceException | InputDataValidationException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (RoomTypeNotFoundException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void doViewRoomRateDetails() {

        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        System.out.println("*** View Room Rate Details ***\n");
        System.out.print("Enter Room Rate ID> ");
        Long roomRateId = scanner.nextLong();

        try {
            RoomRate roomRate = roomRateSessionBeanRemote.viewRoomRateDetails(roomRateId);

            System.out.printf("%8s%20s%20s%15s%20s%10s%5s%5s\n", "Room Rate ID", "Name", "Room Type", "Room Rate Type", "Start Date", "End Date", "Rate", "Disabled");
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String startDateStr = formatter.format(roomRate.getStartDate());
            String endDateStr = formatter.format(roomRate.getEndDate());

            System.out.printf("%8s%20s%20s%15s%20s%10s%5s%5s\n", roomRate.getRoomRateId(), roomRate.getName(), roomRate.getRoomType().getName(), roomRate.getRoomRateType().toString(), startDateStr, endDateStr, NumberFormat.getCurrencyInstance().format(roomRate.getRate()), printDisabled(roomRate.isDisabled()));

            System.out.println("------------------------");
            System.out.println("1: Update Room Rate");
            System.out.println("2: Delete Room Rate");
            System.out.println("3: Back\n");
            System.out.print("> ");
            response = scanner.nextInt();

            if (response == 1) {
                doUpdateRoomRate(roomRate);
            } else if (response == 2) {
                doDeleteRoomRate(roomRate);
            }
        } catch (RoomRateNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private void doUpdateRoomRate(RoomRate roomRate) {
        Scanner scanner = new Scanner(System.in);
        String input;
        Integer inputInt;
        Long roomTypeId = roomRate.getRoomType().getRoomTypeId();

        System.out.println("*** Update Room Rate Details ***\n");
        System.out.print("Enter Name (blank if no change)> ");
        input = scanner.nextLine().trim();
        if (input.length() > 0) {
            roomRate.setName(input);
        }

        while (true) {
            System.out.print("Select Room Rate Type (1: Published, 2: Normal, 3: Peak, 4: Promotion)> ");
            inputInt = scanner.nextInt();
            scanner.nextLine();
            //Integer previousRoomRateTypeInt = roomRate.getRoomRateType().ordinal() + 1;

            if (inputInt >= 1 && inputInt <= 4) {
                roomRate.setRoomRateType(RoomRateTypeEnum.values()[inputInt - 1]);

                if (inputInt >= 3) {
                    //set start and end date
                    try {
                        System.out.print("Enter Start Date> ");
                        input = scanner.nextLine().trim();
                        roomRate.setStartDate(handleDateTimeSessionBeanRemote.convertStringInputToDate(input));

                        System.out.print("Enter End Date> ");
                        input = scanner.nextLine().trim();
                        roomRate.setEndDate(handleDateTimeSessionBeanRemote.convertStringInputToDate(input));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }

                } else {
                    roomRate.setStartDate(new Date());
                    roomRate.setEndDate(new Date(Long.MAX_VALUE));
                }

                break;
            } else {
                System.out.println("Invalid option, please try again\n");
            }
        }

        System.out.print("Enter Rate (0 if no change)> ");
        BigDecimal newRate = scanner.nextBigDecimal();
        if (newRate.compareTo(BigDecimal.ZERO) > 0) {
            roomRate.setRate(newRate);
        }

        System.out.print("Enter Room Type ID (0 if no change)> ");
        Long newRoomTypeId = scanner.nextLong();
        if (newRoomTypeId > 0) {
            roomTypeId = newRoomTypeId;
        }

        Set<ConstraintViolation<RoomRate>> constraintViolations = validator.validate(roomRate);

        if (constraintViolations.isEmpty()) {
            try {
                roomRateSessionBeanRemote.updateRoomRate(roomTypeId, roomRate);
                System.out.println("Room Rate updated successfully!\n");
            } catch (RoomRateNotFoundException | RoomTypeNotFoundException ex) {
                System.out.println(ex.getMessage() + "\n");
            } catch (InputDataValidationException ex) {
                System.out.println(ex.getMessage() + "\n");
            }
        } else {
            showInputDataValidationErrorsForRoomRate(constraintViolations);
        }

    }

    private void doDeleteRoomRate(RoomRate roomRate) {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("*** Delete Room Rate ***\n");
        System.out.printf("Confirm Delete %s (Enter 'Y' to Delete)> ", roomRate.getName());
        input = scanner.nextLine().trim();

        if (input.equals("Y")) {
            try {
                Long roomRateId = roomRate.getRoomRateId();
                roomRateSessionBeanRemote.deleteRoomRate(roomRateId);

                try {
                    roomRateSessionBeanRemote.viewRoomRateDetails(roomRateId);
                    System.out.println(roomRate.getName() + " disabled!\n");
                } catch (RoomRateNotFoundException ex) {
                    System.out.println(roomRate.getName() + " deleted!\n");
                }

            } catch (RoomRateNotFoundException ex) {
                System.out.println(ex.getMessage() + "\n");
            }
        } else {
            System.out.println("Room Rate NOT deleted!\n");
        }
    }

    private void doViewAllRoomRates() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("*** View All Room Rates ***\n");

        List<RoomRate> roomRates = roomRateSessionBeanRemote.viewAllRoomRates();
        System.out.printf("%8s%20s%20s%15s%20s%10s%5s%5s\n", "Room Rate ID", "Name", "Room Type", "Room Rate Type", "Start Date", "End Date", "Rate", "Disabled");

        for (RoomRate roomRate : roomRates) {

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            String startDateStr = formatter.format(roomRate.getStartDate());

            String endDateStr = formatter.format(roomRate.getEndDate());

            System.out.printf("%8s%20s%20s%15s%20s%10s%5s%5s\n", roomRate.getRoomRateId(), roomRate.getName(), roomRate.getRoomType().getName(), roomRate.getRoomRateType().toString(), startDateStr, endDateStr, NumberFormat.getCurrencyInstance().format(roomRate.getRate()), printDisabled(roomRate.isDisabled()));

        }

        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }

    private String printDisabled(boolean disabled) {
        if (disabled) {
            return "disabled";
        } else {
            return "";
        }
    }

    private void showInputDataValidationErrorsForRoomType(Set<ConstraintViolation<RoomType>> constraintViolations) {
        System.out.println("\nInput data validation error!:");

        for (ConstraintViolation constraintViolation : constraintViolations) {
            System.out.println("\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage());
        }

        System.out.println("\nPlease try again......\n");
    }

    private void showInputDataValidationErrorsForRoom(Set<ConstraintViolation<Room>> constraintViolations) {
        System.out.println("\nInput data validation error!:");

        for (ConstraintViolation constraintViolation : constraintViolations) {
            System.out.println("\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage());
        }

        System.out.println("\nPlease try again......\n");
    }

    private void showInputDataValidationErrorsForRoomRate(Set<ConstraintViolation<RoomRate>> constraintViolations) {
        System.out.println("\nInput data validation error!:");

        for (ConstraintViolation constraintViolation : constraintViolations) {
            System.out.println("\t" + constraintViolation.getPropertyPath() + " - " + constraintViolation.getInvalidValue() + "; " + constraintViolation.getMessage());
        }

        System.out.println("\nPlease try again......\n");
    }

    private void doViewRoomAllocationReport() {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("*** View Todays Room Allocation Report ***\n");
            System.out.print("Enter Report Date (TO TEST FUTURE KEY IN FUTURE DATE)> ");
            String reportDateStr = scanner.nextLine().trim();

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date reportDate = formatter.parse(reportDateStr);

            RoomAllocationReport roomAllocationReport = roomAllocationReportSessionBeanRemote.viewRoomAllocationReportByDate(reportDate);

            System.out.print("\n Report for Date: " + reportDateStr + "\n");

            List<String> noAvailableRoomUpgrade = roomAllocationReport.getNoAvailableRoomUpgrade();
            for (String ex : noAvailableRoomUpgrade) {
                System.out.println(ex);
            }

            List<String> noAvailableRoomNoUpgrade = roomAllocationReport.getNoAvailableRoomNoUpgrade();
            for (String ex : noAvailableRoomNoUpgrade) {
                System.out.println(ex);
            }

            System.out.println("");

        } catch (ParseException | RoomAllocationReportNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private void doAllocateRoom() {

        try {

            Scanner scanner = new Scanner(System.in);

            System.out.println("*** Allocate Rooms (TESTING ONLY) ***\n");
            System.out.print("Enter Trigger Date> ");
            String triggerDateStr = scanner.nextLine().trim();

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date triggerDate = formatter.parse(triggerDateStr);

            HashMap<Reservation, Room> allocation = allocationSessionBeanRemote.allocateRoomToFutureDayReservations(triggerDate);

            for (Reservation key : allocation.keySet()) {
                System.out.println("Reservation Id: " + key.getReservationId() + " was allocated room number: " + allocation.get(key).getRoomId());    
            }
            
            System.out.println("");
            
        } catch (ParseException | CannotGetTodayDateException | RoomAllocationReportNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }

    }

}
