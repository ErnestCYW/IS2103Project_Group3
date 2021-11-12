/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateful.BookingReservationSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import entity.Employee;
import entity.RoomType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.enumeration.EmployeeRoleEnum;
import util.exception.InvalidEmployeeRoleException;
import util.exception.ReserveRoomException;

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

    
    
    public void menuFrontOffice() throws InvalidEmployeeRoleException
    {
        if(loggedInEmployee.getEmployeeRoleEnum() != EmployeeRoleEnum.GUEST_RELATION)
        {
            throw new InvalidEmployeeRoleException("You don't have GUEST_RELATION rights to access the front office module.");
        }
        
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        while(true)
        {
            System.out.println("*** HoRS Management System :: Front Office ***\n");
            System.out.println("1: Walk-in Search Room");
            
            System.out.println("5: Back\n");
            response = 0;
            
            while(response < 1 || response > 5)
            {
                System.out.print("> ");
                
                response = scanner.nextInt();
                
                if(response == 1)
                {
                    doSearchRoom();
                }
//                else if(response == 2)
//                {
//                    doViewAllEmployees();
//                }
//                else if(response == 3)
//                {
//                    doCreateNewPartner();
//                }
//                else if(response == 4)
//                {
//                    doViewAllPartners();
//                }
                else if(response == 5)
                {
                    break;
                }
                else
                {
                    System.out.println("Invalid option, please try again!\n");
                }
            }
            
            if(response == 5)
            {
                break;
            }
        }   
    }
    
    private void doSearchRoom() {
        
        try {
            Scanner scanner = new Scanner(System.in);
            String response = "";
            
            System.out.print("Enter Check-in Date> ");
            String checkinDateStr = scanner.nextLine().trim();
            System.out.print("Enter Check-out Date> ");
            String checkoutDateStr = scanner.nextLine().trim();
            System.out.print("Enter number of rooms> ");
            Integer numOfRooms = scanner.nextInt();
            
            SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy");
            
            Date checkinDate = formatter.parse(checkinDateStr);
            Date checkoutDate = formatter.parse(checkoutDateStr);
            
            
            List<RoomType> roomTypes = roomTypeSessionBeanRemote.viewAllRoomTypes();
            //Maybe don't need available rooms
            System.out.printf("%20s%20s%30s%30s\n", "Room Type ID", "Name", "Available Rooms", "Reservation Amount");
            
            
            for (RoomType roomType:roomTypes)
            {
                Integer totalRooms = roomType.getRooms().size();
                Integer availableRooms = bookingReservationSessionBeanRemote.getNumOfAvailableRoomsForRoomType(roomType.getRoomTypeId(), checkinDate, checkoutDate, totalRooms);
                
                if (availableRooms >= numOfRooms)
                {
                    Double reservationAmount = bookingReservationSessionBeanRemote.getWalkInPriceForRoomType(roomType, checkinDate, checkoutDate);
                    //save search results to Session Bean
                    bookingReservationSessionBeanRemote.saveSearchResults(roomType.getName(), availableRooms);
                    System.out.printf("%20s%20s%30s%30s\n", roomType.getRoomTypeId(), roomType.getName(), availableRooms, reservationAmount);
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
            Long bookingId = bookingReservationSessionBeanRemote.doReserveRoom(roomType, numOfRooms, checkinDate, checkoutDate);
            System.out.println("Reservation ID " + bookingId + " successful");
        } catch (ReserveRoomException ex) {
            System.out.println(ex.getMessage());
        }
        
        
//        do 
//        {
//            System.out.print("Enter Room Type for Reservation " + counter + "> ");
//            String roomType = scanner.nextLine().trim();
//            
//            try {
//                bookingSessionBeanRemote.doReserveRoom(roomType);
//                System.out.println("Reservation successful");
//            } catch (OutOfRoomsException ex) {
//                System.out.println(ex.getMessage());
//            }
//            
//            counter++;
//            System.out.print("Enter 'Y' to reserve more rooms> ");
//            response = scanner.nextLine().trim();
//        }
//        while(response.equals("Y"));
    }
}

