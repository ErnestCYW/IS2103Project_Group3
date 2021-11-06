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
import util.exception.InvalidEmployeeRoleException;
import util.exception.InvalidLoginCredentialException;

/**
 *
 * @author elgin
 */
public class MainApp {
    private EmployeeSessionBeanRemote employeeSessionBeanRemote;

    private SystemAdministrationModule systemAdministrationModule;
    
    private Employee loggedInEmployee;
    
    public MainApp() {
    }

    public MainApp(EmployeeSessionBeanRemote employeeSessionBeanRemote) {
        this.employeeSessionBeanRemote = employeeSessionBeanRemote;
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

                        systemAdministrationModule = new SystemAdministrationModule(employeeSessionBeanRemote, loggedInEmployee);
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
        try {
            systemAdministrationModule.menuSystemAdminstration();
        } catch (InvalidEmployeeRoleException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    
}
