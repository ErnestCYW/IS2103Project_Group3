/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateful.BookingReservationSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import entity.Employee;
import entity.Room;
import entity.RoomType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import util.enumeration.EmployeeRoleEnum;
import util.exception.CannotGetWalkInPriceException;
import util.exception.CheckinGuestException;
import util.exception.InvalidEmployeeRoleException;
import util.exception.ReserveRoomException;
import util.exception.RoomTypeNotFoundException;

/**
 *
 * @author elgin
 */
public class FrontOfficeModule {

    private RoomTypeSessionBeanRemote roomTypeSessionBeanRemote;
    private BookingReservationSessionBeanRemote bookingReservationSessionBeanRemote;
    private Employee loggedInEmployee;

    public FrontOfficeModule() {
    }

    public FrontOfficeModule(RoomTypeSessionBeanRemote roomTypeSessionBeanRemote, BookingReservationSessionBeanRemote bookingSessionBeanRemote, Employee loggedInEmployee) {
        this.roomTypeSessionBeanRemote = roomTypeSessionBeanRemote;
        this.bookingReservationSessionBeanRemote = bookingReservationSessionBeanRemote;
        this.loggedInEmployee = loggedInEmployee;
    }

    public void menuFrontOffice() throws InvalidEmployeeRoleException {
        if (loggedInEmployee.getEmployeeRoleEnum() != EmployeeRoleEnum.GUEST_RELATION) {
            throw new InvalidEmployeeRoleException("You don't have GUEST_RELATION rights to access the front office module.");
        }

        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** HoRS Management System :: Front Office ***\n");
            System.out.println("1: Walk-in Search Room");

            System.out.println("5: Back\n");
            response = 0;

            while (response < 1 || response > 4) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    doSearchRoom();
                } else if (response == 2) {
                    doCheckinGuest();
                } else if (response == 3) {
                    doCheckoutGuest();
                } else if (response == 4) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 4) {
                break;
            }
        }
    }

    private void doSearchRoom() {

        try {
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println("*** Walk-in Search Room ***\n");
            System.out.print("Enter Check-in Date> ");
            String checkinDateStr = scanner.nextLine().trim();
            System.out.print("Enter Check-out Date> ");
            String checkoutDateStr = scanner.nextLine().trim();
            System.out.print("Enter number of rooms> ");
            Integer numOfRooms = scanner.nextInt();

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            Date checkinDate = formatter.parse(checkinDateStr);
            Date checkoutDate = formatter.parse(checkoutDateStr);

            List<RoomType> roomTypes = roomTypeSessionBeanRemote.viewAllRoomTypes();
            //Maybe don't need available rooms
            System.out.printf("%20s%20s%30s%30s\n", "Room Type ID", "Name", "Available Rooms", "Reservation Amount");

            for (RoomType roomType : roomTypes) {
                try {
                    Integer availableRooms = bookingReservationSessionBeanRemote.getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(), checkinDate, checkoutDate);

                    if (availableRooms >= numOfRooms) {
                        Double reservationAmount = bookingReservationSessionBeanRemote.getWalkInPriceForRoomType(roomType, checkinDate, checkoutDate);
                        //save search results to Session Bean
                        bookingReservationSessionBeanRemote.saveSearchResults(roomType.getName(), availableRooms);
                        System.out.printf("%20s%20s%30s%30s\n", roomType.getRoomTypeId(), roomType.getName(), availableRooms, reservationAmount);
                    }
                } catch (RoomTypeNotFoundException | CannotGetWalkInPriceException ex) {
                    System.out.println(ex.getMessage());
                }

            }

            scanner.nextLine();
            System.out.print("Press 'Y' to reserve rooms (Otherwise press any key to continue)> ");
            response = scanner.nextLine().trim();

            if (response.equals("Y")) {
                doReserveRoom(checkinDate, checkoutDate);
                //doReserveRoom(numOfRooms);
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    private void doReserveRoom(Date checkinDate, Date checkoutDate) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Room Type for Reservation> "); //Only from search results
        String roomType = scanner.nextLine().trim();
        System.out.print("Enter number of rooms> ");
        Integer numOfRooms = scanner.nextInt();

        try {
            List<Long> reservationIds = bookingReservationSessionBeanRemote.walkInReserveRoom(roomType, numOfRooms, checkinDate, checkoutDate);
            
            System.out.println("Reservation successful: You have reserved " + numOfRooms + " rooms for " + roomType + ". Here are your Reservation IDs: ");
            for(Long reservationId:reservationIds) {
                System.out.println("Reservation ID " + reservationId);
            }
        } catch (ReserveRoomException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void doCheckinGuest() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("*** Check-in Guest ***\n");
        System.out.print("Enter Guest ID> ");
        Long guestId = scanner.nextLong();

        try {
            List<Room> rooms = bookingReservationSessionBeanRemote.checkinGuest(guestId);
            System.out.println("Check-in succesful! The guest's rooms are: ");
            for (Room room : rooms) {
                System.out.println(room.getNumber());
            }
        } catch (CheckinGuestException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void doCheckoutGuest() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("*** Check-out Guest ***\n");
        System.out.print("Enter Guest ID> ");
        Long guestId = scanner.nextLong();

        bookingReservationSessionBeanRemote.checkoutGuest(guestId);
        System.out.println("Check-out successful!");
    }
}
