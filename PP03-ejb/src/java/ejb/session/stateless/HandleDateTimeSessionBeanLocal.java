/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.ejb.Local;
import util.exception.CannotGetTodayDateException;

/**
 *
 * @author ernestcyw
 */
@Local
public interface HandleDateTimeSessionBeanLocal {

    public Date getTodayDate() throws CannotGetTodayDateException;

    public boolean isToday(Date date);

    public boolean isPassed2AM();

    public Date convertStringInputToDate(String stringInput) throws ParseException;

    public List<Date> retrieveDatesBetween(Date startDate, Date endDate);
    
}
