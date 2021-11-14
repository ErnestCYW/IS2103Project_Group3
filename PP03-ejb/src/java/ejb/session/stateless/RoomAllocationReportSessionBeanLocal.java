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

/**
 *
 * @author ernestcyw
 */
@Local
public interface RoomAllocationReportSessionBeanLocal {

    public RoomAllocationReport createRoomAllocationReport() throws CannotGetTodayDateException;

    public RoomAllocationReport viewRoomAllocationReportByDate(Date date);

    public RoomAllocationReport viewRoomAllocationReportById(Long roomAllocationReportId);

    public void deleteRoomAllocationReport(Long roomAllocationReportId);

    public List<RoomAllocationReport> viewAllRoomAllocationReports();

    public RoomAllocationReport viewTodayRoomAllocationReport() throws CannotGetTodayDateException;
    
}
