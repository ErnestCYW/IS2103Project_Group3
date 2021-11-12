/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import java.text.ParseException;
import java.util.Date;
import javax.ejb.Remote;
import util.exception.CannotGetTodayDateException;

/**
 *
 * @author ernestcyw
 */
@Remote
public interface HandleDateTimeSessionBeanRemote {

    public Date getTodayDate() throws CannotGetTodayDateException;

    public boolean isToday(Date date);

    public boolean isPassed2AM();

    public Date convertStringInputToDate(String stringInput) throws ParseException;

}
