/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateless.EmployeeSessionBeanRemote;
import entity.Employee;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.enumeration.EmployeeRoleEnum;
import util.exception.EmployeeUsernameExistException;
import util.exception.InvalidEmployeeRoleException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
public class SystemAdministrationModule {
    
    private EmployeeSessionBeanRemote employeeSessionBeanRemote;
    
    private Employee loggedInEmployee;

    public SystemAdministrationModule() {
    }

    public SystemAdministrationModule(EmployeeSessionBeanRemote employeeSessionBeanRemote, Employee loggedInEmployee) {
        this.employeeSessionBeanRemote = employeeSessionBeanRemote;
        this.loggedInEmployee = loggedInEmployee;
    }
    
    public void menuSystemAdminstration() throws InvalidEmployeeRoleException
    {
        if(loggedInEmployee.getEmployeeRoleEnum() != EmployeeRoleEnum.SYSTEM_ADMIN)
        {
            throw new InvalidEmployeeRoleException("You don't have SYSTEM_ADMIN rights to access the system administration module.");
        }
        
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        
        while(true)
        {
            System.out.println("*** HoRS Management System :: System Administration ***\n");
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
                    doCreateNewEmployee();
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
    
    private void doCreateNewEmployee()
    {
        Scanner scanner = new Scanner(System.in);
        Employee newEmployee = new Employee();
        
        System.out.println("*** Create New Employee ***\n");
        System.out.print("Enter First Name> ");
        newEmployee.setFirstName(scanner.nextLine().trim());
        System.out.print("Enter Last Name> ");
        newEmployee.setLastName(scanner.nextLine().trim());
        System.out.print("Enter username> ");
        newEmployee.setUsername(scanner.nextLine().trim());
        System.out.print("Enter password> ");
        newEmployee.setPassword(scanner.nextLine().trim());
        
        while(true)
        {
            System.out.print("Select Employee Role (1: System Administrator, 2: Operation Manager, 3: Sales Manager, 4: Guest Relation Officer)> ");
            Integer employeeRoleInt = scanner.nextInt();
            
            if(employeeRoleInt >= 1 && employeeRoleInt <= 4)
            {
                newEmployee.setEmployeeRoleEnum(EmployeeRoleEnum.values()[employeeRoleInt-1]);
                break;
            }
            else
            {
                System.out.println("Invalid option, please try again\n");
            }
        }
        
        try {
            employeeSessionBeanRemote.createNewEmployee(newEmployee);
        } catch (EmployeeUsernameExistException ex) {
            System.out.println("Username already exist");
        } catch (UnknownPersistenceException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    
}
