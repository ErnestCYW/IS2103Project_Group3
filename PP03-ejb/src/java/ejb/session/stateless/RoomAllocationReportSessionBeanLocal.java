/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.RoomAllocationReport;
import java.util.Date;
import java.util.List;
import javax.ejb.Local;
import util.exception.CannotGetTodayDateException;
import util.exception.RoomAllocationReportNotFoundException;

/**
 *
 * @author ernestcyw
 */
@Local
public interface RoomAllocationReportSessionBeanLocal {

    public RoomAllocationReport createRoomAllocationReport() throws CannotGetTodayDateException;

    public RoomAllocationReport createRoomAllocationReportForFuture(Date date);

    public RoomAllocationReport viewRoomAllocationReportByDate(Date date) throws RoomAllocationReportNotFoundException;

    public RoomAllocationReport viewRoomAllocationReportById(Long roomAllocationReportId) throws RoomAllocationReportNotFoundException;

    public RoomAllocationReport viewTodayRoomAllocationReport() throws CannotGetTodayDateException, RoomAllocationReportNotFoundException;

    public List<RoomAllocationReport> viewAllRoomAllocationReports();

    public void deleteRoomAllocationReport(Long roomAllocationReportId);

}
