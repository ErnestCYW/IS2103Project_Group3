/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Partner;
import java.util.List;
import javax.ejb.Remote;
import util.exception.PartnerNotFoundException;
import util.exception.PartnerUsernameExistException;
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
}
