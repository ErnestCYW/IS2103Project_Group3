/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import javax.ejb.Stateless;
import util.exception.CannotGetTodayDateException;

/**
 *
 * @author ernestcyw
 */
@Stateless
public class HandleDateTimeSessionBean implements HandleDateTimeSessionBeanLocal, HandleDateTimeSessionBeanRemote {
    
    @Override
    public Date getTodayDate() throws CannotGetTodayDateException {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date todayDate = formatter.parse(formatter.format(new Date()));
            return todayDate;
        } catch (Exception ex) {
            throw new CannotGetTodayDateException();
        }
    }

    @Override
    public boolean isToday(Date date) {
        LocalDate localDate1 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = (new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.isEqual(localDate2);
    }

    @Override
    public boolean isPassed2AM() {
        LocalTime now = LocalTime.now();
        return now.getHour() > 2;
    }
    
    public Date convertStringInputToDate(String stringInput) throws ParseException{
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date toReturn = formatter.parse(stringInput);
        return toReturn;
    }

}
