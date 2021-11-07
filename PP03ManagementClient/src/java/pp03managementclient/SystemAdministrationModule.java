/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pp03managementclient;

import ejb.session.stateless.EmployeeSessionBeanRemote;
import ejb.session.stateless.PartnerSessionBeanRemote;
import entity.Employee;
import entity.Partner;
import java.util.List;
import java.util.Scanner;
import util.enumeration.EmployeeRoleEnum;
import util.exception.EmployeeUsernameExistException;
import util.exception.InvalidEmployeeRoleException;
import util.exception.PartnerUsernameExistException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
public class SystemAdministrationModule {
    
    private EmployeeSessionBeanRemote employeeSessionBeanRemote;
    private PartnerSessionBeanRemote partnerSessionBeanRemote;
    
    private Employee loggedInEmployee;

    public SystemAdministrationModule() {
    }

    public SystemAdministrationModule(EmployeeSessionBeanRemote employeeSessionBeanRemote, PartnerSessionBeanRemote partnerSessionBeanRemote, Employee loggedInEmployee) {
        this.employeeSessionBeanRemote = employeeSessionBeanRemote;
        this.partnerSessionBeanRemote = partnerSessionBeanRemote;
        this.loggedInEmployee = loggedInEmployee;
    }
    
    public void menuSystemAdministration() throws InvalidEmployeeRoleException
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
                    doViewAllEmployees();
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
            Long employeeId = employeeSessionBeanRemote.createNewEmployee(newEmployee);
            System.out.println("Employee ID " + employeeId + " created successfully!");
        } catch (EmployeeUsernameExistException ex) {
            System.out.println("Username already exist");
        } catch (UnknownPersistenceException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    private void doViewAllEmployees()
    {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("*** View All Employees ***\n");
        
        List<Employee> employees = employeeSessionBeanRemote.retrieveAllEmployees();
        System.out.printf("%8s%20s%20s%15s%20s%20s\n", "Employee ID", "First Name", "Last Name", "Employee Role", "Username", "Password");

        for(Employee employee:employees)
        {
            System.out.printf("%8s%20s%20s%15s%20s%20s\n", employee.getEmployeeId().toString(), employee.getFirstName(), employee.getLastName(), employee.getEmployeeRoleEnum().toString(), employee.getUsername(), employee.getPassword());
        }
        
        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }
    
    private void doCreateNewPartner()
    {
        Scanner scanner = new Scanner(System.in);
        Partner newPartner = new Partner();
        
        System.out.println("*** Create New Partner ***\n");
        System.out.print("Enter Partner Name> ");
        newPartner.setPartnerName(scanner.nextLine().trim());
        System.out.print("Enter Password> ");
        newPartner.setPassword(scanner.nextLine().trim());
        
        try {
            Long partnerId = partnerSessionBeanRemote.createNewPartner(newPartner);
            System.out.println("Partner ID " + partnerId + " created successfully!");
        } catch (PartnerUsernameExistException ex) {
            System.out.println("Username already exist");
        } catch (UnknownPersistenceException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    private void doViewAllPartners()
    {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("*** View All Partners ***\n");
        
        List<Partner> partners = partnerSessionBeanRemote.retrieveAllPartners();
        System.out.printf("%10s%50s%40s\n", "Employee ID", "Partner Name", "Password");

        for(Partner partner:partners)
        {
            System.out.printf("%10s%50s%40s\n", partner.getPartnerId().toString(), partner.getPartnerName(), partner.getPassword());
        }
        
        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }
    
    
}
