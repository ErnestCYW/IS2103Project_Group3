/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Guest;
import entity.Partner;
import entity.Reservation;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import util.exception.InvalidLoginCredentialException;
import util.exception.PartnerNotFoundException;
import util.exception.PartnerUsernameExistException;
import util.exception.ReservationNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
@Stateless
public class PartnerSessionBean implements PartnerSessionBeanRemote, PartnerSessionBeanLocal {

    @EJB(name = "ReservationSessionBeanLocal")
    private ReservationSessionBeanLocal reservationSessionBeanLocal;

    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;
    
    

    @Override
    public Long createNewPartner(Partner newPartner) throws PartnerUsernameExistException, UnknownPersistenceException
    {
        try
        {
            em.persist(newPartner);
            em.flush();

            return newPartner.getPartnerId();
        }
        catch(PersistenceException ex)
        {
            if(ex.getCause() != null && ex.getCause().getClass().getName().equals("org.eclipse.persistence.exceptions.DatabaseException"))
            {
                if(ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getName().equals("java.sql.SQLIntegrityConstraintViolationException"))
                {
                    throw new PartnerUsernameExistException();
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
    public List<Partner> retrieveAllPartners()
    {
        Query query = em.createQuery("SELECT p FROM Partner p");
        
        return query.getResultList();
    }
    
    
    @Override
    public Partner retrieveEmployeeByPartnerId(Long partnerId) throws PartnerNotFoundException
    {
        Partner partner = em.find(Partner.class, partnerId);
        
        if(partner != null)
        {
            return partner;
        }
        else
        {
            throw new PartnerNotFoundException("Partner ID " + partnerId + " does not exist!");
        }
    }
    
    
    
    @Override
    public Partner retrievePartnerByUsername(String username) throws PartnerNotFoundException
    {
        Query query = em.createQuery("SELECT p FROM Partner p WHERE p.partnerName = :inUsername");
        query.setParameter("inUsername", username);
        
        try
        {
            return (Partner)query.getSingleResult();
        }
        catch(NoResultException | NonUniqueResultException ex)
        {
            throw new PartnerNotFoundException("Partner " + username + " does not exist!");
        }
    }
    
    
    
    public Partner partnerLogin(String username, String password) throws InvalidLoginCredentialException
    {
        try
        {
            Partner partner = retrievePartnerByUsername(username);
            
            if(partner.getPassword().equals(password))
            {
                return partner;
            }
            else
            {
                throw new InvalidLoginCredentialException("Username does not exist or invalid password!");
            }
        }
        catch(PartnerNotFoundException ex)
        {
            throw new InvalidLoginCredentialException("Username does not exist or invalid password!");
        }
    }
    
    
    
    @Override
    public Reservation viewPartnerReservation(Long reservationId, Partner partner) throws ReservationNotFoundException, InvalidLoginCredentialException {
        
        Reservation reservation = reservationSessionBeanLocal.viewReservation(reservationId);
        
        if (reservation.getPartner().equals(partner)) {
            return reservation;
        } else {
            throw new InvalidLoginCredentialException("Reservation does not exist or does not belong to you");
        }
        
    }
    
   
    
    
    @Override
    public List<Reservation> viewReservations(Partner partner) {
        
        return partner.getReservations();
    
    }
}
