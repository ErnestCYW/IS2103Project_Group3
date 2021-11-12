/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateless.RoomTypeSessionBeanRemote;
import entity.Employee;
import java.util.Scanner;
import util.enumeration.EmployeeRoleEnum;
import util.exception.InvalidEmployeeRoleException;

/**
 *
 * @author elgin
 */
public class HotelOperationModule {
    
    private RoomTypeSessionBeanRemote roomTypeSessionBeanRemote;
    private Employee loggedInEmployee;

    public HotelOperationModule() {
    }

    public HotelOperationModule(RoomTypeSessionBeanRemote roomTypeSessionBeanRemote, Employee loggedInEmployee) {
        this.roomTypeSessionBeanRemote = roomTypeSessionBeanRemote;
        this.loggedInEmployee = loggedInEmployee;
    }
    
    public void menuHotelOperation() throws InvalidEmployeeRoleException
    {
        if(loggedInEmployee.getEmployeeRoleEnum() != EmployeeRoleEnum.SYSTEM_ADMIN)
        {
            throw new InvalidEmployeeRoleException("You don't have SYSTEM_ADMIN rights to access the system administration module.");
        }
        
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        while(true)
        {
            System.out.println("*** HoRS Management System :: Hotel Operation ***\n");
            System.out.println("1: Create New Employee");
            System.out.println("2: View All Employees");
            System.out.println("3: Create New Partner");
            System.out.println("4: View All Partners");
            System.out.println("5: Back\n");
            response = 0;
            
            while(response < 1 || response > 5)
            {
                System.out.print("> ");
                
                response = scanner.nextInt();
                
                if(response == 1)
                {
                    //doCreateNewEmployee();
                }
                else if(response == 2)
                {
                    //doViewAllEmployees();
                }
                else if(response == 3)
                {
                    //doCreateNewPartner();
                }
                else if(response == 4)
                {
                    //doViewAllPartners();
                }
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
    
}

