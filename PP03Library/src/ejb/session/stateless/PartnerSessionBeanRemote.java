/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Partner;
import entity.Reservation;
import java.util.List;
import javax.ejb.Remote;
import util.exception.InvalidLoginCredentialException;
import util.exception.PartnerNotFoundException;
import util.exception.PartnerUsernameExistException;
import util.exception.ReservationNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author elgin
 */
@Remote
public interface PartnerSessionBeanRemote {
    
    public Long createNewPartner(Partner newPartner) throws PartnerUsernameExistException, UnknownPersistenceException;

    public List<Partner> retrieveAllPartners();
    
    public Partner retrieveEmployeeByPartnerId(Long partnerId) throws PartnerNotFoundException;
    
    public Partner retrievePartnerByUsername(String username) throws PartnerNotFoundException;

    public Partner partnerLogin(String username, String password) throws InvalidLoginCredentialException;

    public Reservation viewPartnerReservation(Long reservationId, Partner partner) throws ReservationNotFoundException, InvalidLoginCredentialException;

    public List<Reservation> viewReservations(Partner partner);
}
