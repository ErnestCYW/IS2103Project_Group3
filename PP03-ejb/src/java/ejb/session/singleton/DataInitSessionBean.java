/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.EmployeeSessionBeanLocal;
import entity.Employee;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.enumeration.EmployeeRoleEnum;
import util.exception.EmployeeNotFoundException;
import util.exception.EmployeeUsernameExistException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author ernestcyw
 */
@Singleton
@LocalBean
@Startup
public class DataInitSessionBean {

    @EJB
    private EmployeeSessionBeanLocal employeeSessionBeanLocal;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @PostConstruct
    public void postConstruct() {
        try
        {
            employeeSessionBeanLocal.retrieveEmployeeByUsername("master");
        }
        catch(EmployeeNotFoundException ex)
        {
            initializeData();
        }
    }
    
    private void initializeData() {
        try 
        {
            employeeSessionBeanLocal.createNewEmployee(new Employee("Default", "Admin", "master", "password", EmployeeRoleEnum.SYSTEM_ADMIN));
        } 
        catch (EmployeeUsernameExistException | UnknownPersistenceException ex) 
        {
            ex.printStackTrace();
        }
    }
}
