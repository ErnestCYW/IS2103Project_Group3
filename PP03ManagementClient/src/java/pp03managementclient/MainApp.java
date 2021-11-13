/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateful.BookingReservationSessionBeanRemote;
import ejb.session.stateless.EmployeeSessionBeanRemote;
import ejb.session.stateless.HandleDateTimeSessionBeanRemote;
import ejb.session.stateless.PartnerSessionBeanRemote;
import ejb.session.stateless.RoomRateSessionBeanRemote;
import ejb.session.stateless.RoomSessionBeanRemote;
import ejb.session.stateless.RoomTypeSessionBeanRemote;
import entity.Employee;
import java.util.Scanner;
import util.exception.InvalidEmployeeRoleException;
import util.exception.InvalidLoginCredentialException;

/**
 *
 * @author elgin
 */
public class MainApp {
    private EmployeeSessionBeanRemote employeeSessionBeanRemote;
    private PartnerSessionBeanRemote partnerSessionBeanRemote;
    private RoomSessionBeanRemote roomSessionBeanRemote;
    private RoomRateSessionBeanRemote roomRateSessionBeanRemote;
    private RoomTypeSessionBeanRemote roomTypeSessionBeanRemote;
    private BookingReservationSessionBeanRemote bookingReservationSessionBeanRemote;
    private HandleDateTimeSessionBeanRemote handleDateTimeSessionBeanRemote;

    private SystemAdministrationModule systemAdministrationModule;
    private HotelOperationModule hotelOperationModule;
    private FrontOfficeModule frontOfficeModule;
    
    private Employee loggedInEmployee;
    
    public MainApp() {
    }

    public MainApp(EmployeeSessionBeanRemote employeeSessionBeanRemote, PartnerSessionBeanRemote partnerSessionBeanRemote, RoomSessionBeanRemote roomSessionBeanRemote, RoomRateSessionBeanRemote roomRateSessionBeanRemote, RoomTypeSessionBeanRemote roomTypeSessionBeanRemote, BookingReservationSessionBeanRemote bookingReservationSessionBeanRemote, HandleDateTimeSessionBeanRemote handleDateTimeSessionBeanRemote) {
        this.employeeSessionBeanRemote = employeeSessionBeanRemote;
        this.partnerSessionBeanRemote = partnerSessionBeanRemote;
        this.roomSessionBeanRemote = roomSessionBeanRemote;
        this.roomRateSessionBeanRemote = roomRateSessionBeanRemote;
        this.roomTypeSessionBeanRemote = roomTypeSessionBeanRemote;
        this.bookingReservationSessionBeanRemote = bookingReservationSessionBeanRemote;
        this.handleDateTimeSessionBeanRemote = handleDateTimeSessionBeanRemote;
    }

    

    

    
    
    public void runApp()
    {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        while(true)
        {
            System.out.println("*** Welcome to HoRS Management System ***\n");
            System.out.println("1: Login");
            System.out.println("2: Exit");
            response = 0;
            
            while(response < 1 || response > 2)
            {
                System.out.print("> ");
            
                response = scanner.nextInt();

                if(response == 1)
                {
                    try {
                        doLogin();
                        System.out.println("Login successful!\n");

                        systemAdministrationModule = new SystemAdministrationModule(employeeSessionBeanRemote, partnerSessionBeanRemote, loggedInEmployee);
                        hotelOperationModule = new HotelOperationModule(roomSessionBeanRemote, roomRateSessionBeanRemote, roomTypeSessionBeanRemote, handleDateTimeSessionBeanRemote, loggedInEmployee);
                        frontOfficeModule = new FrontOfficeModule(roomTypeSessionBeanRemote, bookingReservationSessionBeanRemote, loggedInEmployee);
                        menuMain();
                    } 
                    catch (InvalidLoginCredentialException ex) {
                        System.out.println("Invalid login credential: " + ex.getMessage() + "\n");
                    }
                }
                else if(response == 2)
                {
                    break;
                }
                else
                {
                    System.out.println("Invalid option, please try again!\n");
                }
            }
            
            if(response == 2)
            {
                break;
            }
            
        }
        
        
        
    }
    
    private void doLogin() throws InvalidLoginCredentialException
    {
        Scanner scanner = new Scanner(System.in);
        String username = "";
        String password = "";
        
        System.out.println("*** Login ***\n");
        System.out.print("Enter username> ");
        username = scanner.nextLine().trim();
        System.out.print("Enter password> ");
        password = scanner.nextLine().trim();
        
        if(username.length() > 0 && password.length() > 0)
        {
            loggedInEmployee = employeeSessionBeanRemote.employeeLogin(username, password);
        }
        else 
        {
            throw new InvalidLoginCredentialException("Missing login credential!");
        }
                       
    }
    
    private void menuMain()
    {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        while(true)
        {
            System.out.println("*** HoRS Management System ***\n");
            System.out.println("You are login as " + loggedInEmployee.getFirstName() + " " + loggedInEmployee.getLastName() + "\n");
            System.out.println("1: System Administration");
            System.out.println("2: Hotel Operation");
            System.out.println("3: Front Office");
            System.out.println("4: Logout\n");
            response = 0;
            
            while(response < 1 || response > 2)
            {
                System.out.print("> ");

                response = scanner.nextInt();

                if(response == 1)
                {
                    try
                    {
                        systemAdministrationModule.menuSystemAdministration();
                    }
                    catch (InvalidEmployeeRoleException ex)
                    {
                        System.out.println("Invalid option, please try again!: " + ex.getMessage() + "\n");
                    }
                }
                else if (response == 2)
                {
                    try {
                        hotelOperationModule.menuHotelOperation();
                    } catch (InvalidEmployeeRoleException ex) {
                        System.out.println("Invalid option, please try again!: " + ex.getMessage() + "\n");
                    }
                }
                else if (response == 3)
                {
                    try {
                        frontOfficeModule.menuFrontOffice();
                    } catch (InvalidEmployeeRoleException ex) {
                        System.out.println("Invalid option, please try again!: " + ex.getMessage() + "\n");
                    }
                }
                else if (response == 4)
                {
                    break;
                }
                else
                {
                    System.out.println("Invalid option, please try again!\n");                
                }
            }
            
            if(response == 4)
            {
                break;
            }
        }
    }
    
    
}
