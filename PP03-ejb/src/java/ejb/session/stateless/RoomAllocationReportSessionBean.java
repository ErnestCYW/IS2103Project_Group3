/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.RoomAllocationReport;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.exception.CannotGetTodayDateException;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class RoomAllocationReportSessionBean implements RoomAllocationReportSessionBeanRemote, RoomAllocationReportSessionBeanLocal {

    @EJB
    private HandleDateTimeSessionBeanLocal handleDateTimeSessionBean;
    
    @PersistenceContext(unitName = "PP03-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @Override
    public RoomAllocationReport createRoomAllocationReport() throws CannotGetTodayDateException{
        
        RoomAllocationReport newRoomAllocationReportEntity = new RoomAllocationReport(handleDateTimeSessionBean.getTodayDate());
        
        em.persist(newRoomAllocationReportEntity);
        em.flush();
        
        return newRoomAllocationReportEntity;
    }
    
    @Override
    public RoomAllocationReport createRoomAllocationReport() throws CannotGetTodayDateException{
        
        RoomAllocationReport newRoomAllocationReportEntity = new RoomAllocationReport(handleDateTimeSessionBean.getTodayDate());
        
        em.persist(newRoomAllocationReportEntity);
        em.flush();
        
        return newRoomAllocationReportEntity;
    }

    @Override
    public RoomAllocationReport viewRoomAllocationReportByDate(Date date) {
        
        Query roomAllocationReportQuery = em.createQuery("SELECT rar FROM RoomAllocationReport rar WHERE rar.date = :inDate");
        roomAllocationReportQuery.setParameter("inDate", date);
        RoomAllocationReport roomAllocationReport = (RoomAllocationReport) roomAllocationReportQuery.getSingleResult();
        
        return roomAllocationReport;
    }
    
    @Override
    public RoomAllocationReport viewRoomAllocationReportById(Long roomAllocationReportId) {
        
        RoomAllocationReport roomAllocationReport = em.find(RoomAllocationReport.class, roomAllocationReportId);
        
        return roomAllocationReport;
    }
    
    @Override
    public RoomAllocationReport viewTodayRoomAllocationReport() throws CannotGetTodayDateException {
        return viewRoomAllocationReportByDate(handleDateTimeSessionBean.getTodayDate());
    }
    
    @Override
    public List<RoomAllocationReport> viewAllRoomAllocationReports() {
        
        Query query = em.createQuery("SELECT rar FROM RoomAllocationReport rar");
        
        return query.getResultList();
    }

    @Override
    public void deleteRoomAllocationReport(Long roomAllocationReportId) {
        
        RoomAllocationReport roomAllocationReportToRemove = em.find(RoomAllocationReport.class, roomAllocationReportId);
        
        em.remove(roomAllocationReportToRemove);
    }
    
}
