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
import util.exception.RoomAllocationReportNotFoundException;

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

    @Override
    public RoomAllocationReport createRoomAllocationReport() throws CannotGetTodayDateException {

        RoomAllocationReport newRoomAllocationReportEntity = new RoomAllocationReport(handleDateTimeSessionBean.getTodayDate());

        em.persist(newRoomAllocationReportEntity);
        em.flush();

        return newRoomAllocationReportEntity;
    }

    @Override
    public RoomAllocationReport createRoomAllocationReportForFuture(Date date) {

        RoomAllocationReport newRoomAllocationReportEntity = new RoomAllocationReport(date);

        em.persist(newRoomAllocationReportEntity);
        em.flush();

        return newRoomAllocationReportEntity;
    }

    @Override
    public List<RoomAllocationReport> viewAllRoomAllocationReports() {

        Query query = em.createQuery("SELECT rar FROM RoomAllocationReport rar");

        return query.getResultList();

    }

    @Override
    public RoomAllocationReport viewTodayRoomAllocationReport() throws CannotGetTodayDateException, RoomAllocationReportNotFoundException {

        return viewRoomAllocationReportByDate(handleDateTimeSessionBean.getTodayDate());

    }

    @Override
    public RoomAllocationReport viewRoomAllocationReportByDate(Date date) throws RoomAllocationReportNotFoundException {

        Query query = em.createQuery("SELECT rar FROM RoomAllocationReport rar WHERE rar.date = :inDate ORDER BY rar.RoomAllocationReportId DESC");
        query.setParameter("inDate", date);
        List<RoomAllocationReport> temp = query.setMaxResults(1).getResultList();

        if (!temp.isEmpty()) {
            RoomAllocationReport roomAllocationReport = temp.get(0);
            return roomAllocationReport;
        } else {
            throw new RoomAllocationReportNotFoundException("Cannot Find By Date");
        }
    }

    @Override
    public RoomAllocationReport viewRoomAllocationReportById(Long roomAllocationReportId) throws RoomAllocationReportNotFoundException {

        RoomAllocationReport roomAllocationReport = em.find(RoomAllocationReport.class, roomAllocationReportId);

        if (roomAllocationReport != null) {
            return roomAllocationReport;
        } else {
            throw new RoomAllocationReportNotFoundException("RoomAllocationReport ID " + roomAllocationReportId + " does not exists!");
        }
    }

    @Override
    public void deleteRoomAllocationReport(Long roomAllocationReportId
    ) {

        RoomAllocationReport roomAllocationReportToRemove = em.find(RoomAllocationReport.class, roomAllocationReportId);

        em.remove(roomAllocationReportToRemove);
    }

}
