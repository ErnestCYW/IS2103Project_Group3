/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03reservationclient;

import ejb.session.stateful.BookingReservationSessionBeanRemote;
import ejb.session.stateless.GuestSessionBeanRemote;
import ejb.session.stateless.HandleDateTimeSessionBeanRemote;
import ejb.session.stateless.RoomRateSessionBeanRemote;
import ejb.session.stateless.RoomSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import entity.Guest;
import entity.Reservation;
import entity.RoomType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import util.exception.CannotGetOnlinePriceException;
import util.exception.GuestEmailExistException;
import util.exception.InvalidLoginCredentialException;
import util.exception.ReservationNotFoundException;
import util.exception.ReserveRoomException;
import util.exception.RoomTypeNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
public class MainApp {

    private GuestSessionBeanRemote guestSessionBeanRemote;
    private RoomTypeSessionBeanRemote roomTypeSessionBeanRemote;
    private BookingReservationSessionBeanRemote bookingReservationSessionBeanRemote;

    private Guest loggedInGuest;

    public MainApp() {
    }

    public MainApp(GuestSessionBeanRemote guestSessionBeanRemote, RoomTypeSessionBeanRemote roomTypeSessionBeanRemote, BookingReservationSessionBeanRemote bookingReservationSessionBeanRemote) {
        this.guestSessionBeanRemote = guestSessionBeanRemote;
        this.roomTypeSessionBeanRemote = roomTypeSessionBeanRemote;
        this.bookingReservationSessionBeanRemote = bookingReservationSessionBeanRemote;
    }

    public void runApp() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Welcome to HoRS Reservation System ***\n");
            System.out.println("1: Login");
            System.out.println("2: Register");
            System.out.println("3: Exit");
            response = 0;

            while (response < 1 || response > 3) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    try {
                        doLogin();
                        System.out.println("Login successful!\n");

                        menuMain();
                    } catch (InvalidLoginCredentialException ex) {
                        System.out.println("Invalid login credential: " + ex.getMessage() + "\n");
                    }
                } else if (response == 2) {
                    doRegister();
                } else if (response == 3) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 3) {
                break;
            }

        }

    }

    private void doLogin() throws InvalidLoginCredentialException {
        Scanner scanner = new Scanner(System.in);
        String username = "";
        String password = "";

        System.out.println("*** Login ***\n");
        System.out.print("Enter username> ");
        username = scanner.nextLine().trim();
        System.out.print("Enter password> ");
        password = scanner.nextLine().trim();

        if (username.length() > 0 && password.length() > 0) {
            Guest guest = guestSessionBeanRemote.guestLogin(username, password);
            //guest.getReservations().size();
            this.loggedInGuest = guest;
        } else {
            throw new InvalidLoginCredentialException("Missing login credential!");
        }

    }

    private void doRegister() {
        Scanner scanner = new Scanner(System.in);
        Guest newGuest = new Guest();

        System.out.println("*** Create New Guest ***\n");
        System.out.print("Enter Email> ");
        newGuest.setEmail(scanner.nextLine().trim());
        System.out.print("Enter Password> ");
        newGuest.setPassword(scanner.nextLine().trim());
        System.out.println(newGuest.getEmail());

        try {
            Long guestId = guestSessionBeanRemote.createNewGuest(newGuest);
            System.out.println("Guest ID " + guestId + " created successfully!");
            System.out.println("Please Login!");
        } catch (GuestEmailExistException ex) {
            System.out.println("Email already exist");
        } catch (UnknownPersistenceException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void menuMain() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** HoRS Management System ***\n");
            System.out.println("You are login as " + loggedInGuest.getEmail() + "\n");
            System.out.println("1: Search Hotel Room");
            System.out.println("2: View My Reservation Details");
            System.out.println("3: View All My Reservation Details");
            System.out.println("4: Logout\n");
            response = 0;

            while (response < 1 || response > 2) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    doSearchHotelRoom();
                } else if (response == 2) {
                    doViewMyReservationDetails();
                } else if (response == 3) {
                    doViewAllMyReservationDetails();
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

    private void doSearchHotelRoom() {

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
            System.out.printf("%20s%20s%30s%30s\n", "Room Type ID", "Name", "Available Rooms", "Reservation Amount");

            for (RoomType roomType : roomTypes) {
                try {
                    Integer availableRooms = bookingReservationSessionBeanRemote.getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(), checkinDate, checkoutDate);

                    if (availableRooms >= numOfRooms) {
                        Double reservationAmount = bookingReservationSessionBeanRemote.getOnlinePriceForRoomType(roomType, checkinDate, checkoutDate);
                        //save search results to Session Bean
                        bookingReservationSessionBeanRemote.saveSearchResults(roomType.getName(), availableRooms);
                        System.out.printf("%20s%20s%30s%30s\n", roomType.getRoomTypeId(), roomType.getName(), availableRooms, reservationAmount);
                    }
                } catch (RoomTypeNotFoundException | CannotGetOnlinePriceException ex) {
                    System.out.println(ex.getMessage());
                }

            }

            scanner.nextLine();
            System.out.print("Press 'Y' to reserve rooms (Otherwise press any key to continue)> ");
            response = scanner.nextLine().trim();

            if (response.equals("Y")) {
                doReserveHotelRoom(checkinDate, checkoutDate);
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    private void doReserveHotelRoom(Date checkinDate, Date checkoutDate) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Room Type Name> "); //Only from search results
        String roomType = scanner.nextLine().trim();
        System.out.print("Enter number of rooms> ");
        Integer numOfRooms = scanner.nextInt();

        try {
            List<Long> reservationIds = bookingReservationSessionBeanRemote.onlineReserveRoom(roomType, numOfRooms, checkinDate, checkoutDate, loggedInGuest);

            System.out.println("Reservation successful: You have reserved " + numOfRooms + " rooms for " + roomType + ". Here are your Reservation IDs: ");
            for (Long reservationId : reservationIds) {
                System.out.println("Reservation ID " + reservationId);
            }
        } catch (ReserveRoomException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void doViewMyReservationDetails() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Reservation ID> "); //Only from search results
        Long reservationId = scanner.nextLong();

        try {
            Reservation reservation = guestSessionBeanRemote.viewGuestReservation(reservationId, loggedInGuest);

            System.out.printf("%20s%20s%30s%30s%30s\n", "Reservation ID", "startDate", "endDate", "totalCost", "roomType");
            System.out.printf("%20s%20s%30s%30s%30s\n", reservation.getReservationId(), reservation.getStartDate(), reservation.getEndDate(), reservation.getTotalCost(), reservation.getRoomType().getName());

            System.out.println("------------------------");
            System.out.println("1: back");
            System.out.print("> ");
            Integer response = scanner.nextInt();

        } catch (InvalidLoginCredentialException | ReservationNotFoundException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void doViewAllMyReservationDetails() {

        Scanner scanner = new Scanner(System.in);
        
        System.out.printf("%20s%20s%30s%30s%30s\n", "Reservation ID", "startDate", "endDate", "totalCost", "roomType");

        List<Reservation> reservations = guestSessionBeanRemote.viewGuestReservations(loggedInGuest);

        for (Reservation reservation : reservations) {

            System.out.printf("%20s%20s%30s%30s%30s\n", reservation.getReservationId(), reservation.getStartDate(), reservation.getEndDate(), reservation.getTotalCost(), reservation.getRoomType().getName());

        }
        System.out.println("------------------------");
        System.out.println("1: back");
        System.out.print("> ");
        Integer response = scanner.nextInt();
    }
}
