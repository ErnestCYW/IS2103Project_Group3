/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Employee;
import entity.Guest;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import util.exception.EmployeeNotFoundException;
import util.exception.GuestEmailExistException;
import util.exception.GuestNotFoundException;
import util.exception.InvalidLoginCredentialException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
@Stateless
public class GuestSessionBean implements GuestSessionBeanRemote, GuestSessionBeanLocal {

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    
    @Override
    public Long createNewGuest(Guest newGuest) throws GuestEmailExistException, UnknownPersistenceException
    {
        try
        {
            em.persist(newGuest);
            em.flush();

            return newGuest.getGuestId();
        }
        catch(PersistenceException ex)
        {
            if(ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException"))
            {
                if(ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException"))
                {
                    throw new GuestEmailExistException();
                }
                else
                {
                    throw new UnknownPersistenceException(ex.getMessage());
                }
            }
            else
            {
                throw new UnknownPersistenceException(ex.getMessage());
            }
        }
    }
    
    
    
    @Override
    public List<Guest> retrieveAllGuest()
    {
        Query query = em.createQuery("SELECT g FROM Guest g");
        
        return query.getResultList();
    }
    
    
    @Override
    public Guest retrieveGuestByGuestId(Long guestId) throws GuestNotFoundException
    {
        Guest guest = em.find(Guest.class, guestId);
        
        if(guest != null)
        {
            return guest;
        }
        else
        {
            throw new GuestNotFoundException("Guest ID " + guestId + " does not exist!");
        }
    }
    
    
    
    @Override
    public Guest retrieveGuestByEmail(String email) throws GuestNotFoundException
    {
        Query query = em.createQuery("SELECT g FROM Guest g WHERE g.email = :inEmail");
        query.setParameter("inEmail", email);
        
        try
        {
            return (Guest)query.getSingleResult();
        }
        catch(NoResultException | NonUniqueResultException ex)
        {
            throw new GuestNotFoundException("Guest email " + email + " does not exist!");
        }
    }
    
    
    
    
    @Override
    public Guest guestLogin(String email, String password) throws InvalidLoginCredentialException
    {
        try
        {
            Guest guest = retrieveGuestByEmail(email);
            
            if(guest.getPassword().equals(password))
            {
                return guest;
            }
            else
            {
                throw new InvalidLoginCredentialException("Email does not exist or invalid password!");
            }
        }
        catch(GuestNotFoundException ex)
        {
            throw new InvalidLoginCredentialException("Email does not exist or invalid password!");
        }
    }

    
}
