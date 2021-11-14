/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03holidayreservationclient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import ws.client.CannotGetOnlinePriceException_Exception;
import ws.client.InvalidLoginCredentialException;
import ws.client.InvalidLoginCredentialException_Exception;
import ws.client.ParseException_Exception;
import ws.client.Partner;
import ws.client.PartnerWebService_Service;
import ws.client.Reservation;
import ws.client.ReservationNotFoundException;
import ws.client.ReservationNotFoundException_Exception;
import ws.client.ReserveRoomException_Exception;
import ws.client.RoomType;
import ws.client.RoomTypeNotFoundException_Exception;

/**
 *
 * @author ernestcyw
 */
public class MainApp {

    private Partner loggedInPartner;
    private HashMap<String, Integer> searchRoomResults;
    private HashMap<String, BigDecimal> roomTypeNameAndTotalPrice;

    public MainApp() {
        searchRoomResults = new HashMap<>();
        roomTypeNameAndTotalPrice = new HashMap<>();

    }

    public void runApp() {
        Scanner scanner = new Scanner(System.in);
        Integer response;

        while (true) {
            System.out.println("*** Welcome to Holiday Reservation System ***\n");
            System.out.println("1: Login");
            System.out.println("2: Exit\n");
            response = 0;

            while (response < 1 || response > 2) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    doPartnerLogin();
                    //System.out.println("Login successful!\n");

                    menuMain();
                } else if (response == 2) {
                    break;
                } else {
                    System.out.print("Invalid option, please try again!\n");
                }
            }

            if (response == 2) {
                break;
            }
        }
    }

    private void doPartnerLogin() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("*** Partner Login *** \n");
        System.out.print("Enter username> ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password> ");
        String password = scanner.nextLine().trim();

        PartnerWebService_Service service = new PartnerWebService_Service();

        try {
            loggedInPartner = service.getPartnerWebServicePort().partnerLogin(username, password);
            System.out.println("Login successful!\n");
        } catch (InvalidLoginCredentialException_Exception ex) {
            System.out.println("Invalid login credential: " + ex.getMessage() + "\n");
        }
    }

    private void menuMain() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Holiday Reservation System ***\n");
            System.out.println("You are login as " + loggedInPartner.getPartnerName() + "\n");
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
                    System.out.println("*** Holiday Reservation System ***\n");
                    System.out.println("You are login as " + loggedInPartner.getPartnerName() + "\n");
                    System.out.println("1: Search Hotel Room");
                    System.out.println("2: View My Reservation Details");
                    System.out.println("3: View All My Reservation Details");
                    System.out.println("4: Logout\n");
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
            PartnerWebService_Service service = new PartnerWebService_Service();
            Scanner scanner = new Scanner(System.in);
            String response = "";

            System.out.println("*** Walk-in Search Room ***\n");
            System.out.print("Enter Check-in Date> ");
            String checkinDateStr = scanner.nextLine().trim();
            System.out.print("Enter Check-out Date> ");
            String checkoutDateStr = scanner.nextLine().trim();
            System.out.print("Enter number of rooms> ");
            Integer numOfRooms = scanner.nextInt();

            List<RoomType> roomTypes = service.getPartnerWebServicePort().viewAllRoomTypes();

            System.out.printf("%30s%30s%40s%40s\n", "Room Type ID", "Name",
                    "Available Rooms", "Reservation Amount");

            for (RoomType roomType : roomTypes) {
                try {
                    Integer availableRooms
                            = service.getPartnerWebServicePort().getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(),
                                    checkinDateStr, checkoutDateStr);

                    if (availableRooms >= numOfRooms) {
                        Double reservationAmount
                                = service.getPartnerWebServicePort().getOnlinePriceForRoomType(roomType,
                                        checkinDateStr, checkoutDateStr);

                        searchRoomResults.put(roomType.getName(), availableRooms);
                        roomTypeNameAndTotalPrice.put(roomType.getName(), BigDecimal.valueOf(reservationAmount));
                        System.out.printf("%30s%30s%40s%40s\n", roomType.getRoomTypeId(),
                                roomType.getName(), availableRooms, reservationAmount);
                    }

                } catch (RoomTypeNotFoundException_Exception
                        | CannotGetOnlinePriceException_Exception ex) {
                    System.out.println(ex.getMessage());
                }

            }

            scanner.nextLine();
            System.out.print("Press 'Y' to reserve rooms (Otherwise press any key to continue)> ");
            response = scanner.nextLine().trim();

            if (response.equals("Y")) {
                doReserveHotelRoom(checkinDateStr, checkoutDateStr);
                //checkoutDateStr

            }

        } catch (ParseException_Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doReserveHotelRoom(String checkinDate, String checkoutDate) {
        Scanner scanner = new Scanner(System.in);
        PartnerWebService_Service service = new PartnerWebService_Service();

        System.out.print("Enter Room Type Name> "); //Only from search results
        String roomTypeName = scanner.nextLine().trim();
//        System.out.print("Enter Room Type ID> "); //Only from search results
//        Long roomTypeId = scanner.nextLong();
        System.out.print("Enter number of rooms> ");
        Integer numOfRooms = scanner.nextInt();

        Integer availableRooms = searchRoomResults.get(roomTypeName);
        BigDecimal price = roomTypeNameAndTotalPrice.get(roomTypeName);

        try {
            List<Long> reservationIds = service.getPartnerWebServicePort().partnerReserveRoom(availableRooms, price, roomTypeName, numOfRooms, checkinDate, checkoutDate, loggedInPartner);

            System.out.println("Reservation successful: You have reserved " + numOfRooms + " rooms for " + roomTypeName + ". Here are your Reservation IDs: ");
            for (Long reservationId : reservationIds) {
                System.out.println("Reservation ID : " + reservationId);
            }
            System.out.println("");
        } catch (ReserveRoomException_Exception | ParseException_Exception | RoomTypeNotFoundException_Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void doViewAllMyReservationDetails() {

        System.out.println("*** View All My Reservation Details ***\n");

        Scanner scanner = new Scanner(System.in);
        PartnerWebService_Service service = new PartnerWebService_Service();

        System.out.printf("%30s%30s40s%40s%40s\n", "Reservation ID", "startDate", "endDate", "totalCost", "roomType");

        List<Reservation> reservations = service.getPartnerWebServicePort().viewAllReservations(loggedInPartner);
        System.out.println("------------------------------------------------------------------------------------------------");
        for (Reservation reservation : reservations) {

            System.out.printf("%30s%30s%40s%40s%40s\n", reservation.getReservationId(), reservation.getStartDate(), reservation.getEndDate(), reservation.getTotalCost(), "");

        }
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("1: back");
        System.out.print("> ");
        Integer response = scanner.nextInt();
    }

    private void doViewMyReservationDetails() {

        System.out.println("*** View My Reservation Details ***\n");

        Scanner scanner = new Scanner(System.in);
        PartnerWebService_Service service = new PartnerWebService_Service();

        System.out.print("Enter Reservation ID> "); //Only from search results
        Long reservationId = scanner.nextLong();

        try {
            Reservation reservation = service.getPartnerWebServicePort().viewReservationDetails(loggedInPartner, reservationId);

            System.out.println("------------------------------------------------------------------------------------------------");
            System.out.printf("%30s%30s%40s%40s\n", "Reservation ID", "startDate", "endDate", "totalCost");
            System.out.printf("%30s%30s%40s%40s\n", reservation.getReservationId(), reservation.getStartDate(), reservation.getEndDate(), reservation.getTotalCost());

            System.out.println("------------------------------------------------------------------------------------------------");
            System.out.println("1: back");
            System.out.print("> ");
            Integer response = scanner.nextInt();

        } catch (InvalidLoginCredentialException_Exception | ReservationNotFoundException_Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

}
