/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.exception;

/**
 *
 * @author ernestcyw
 */
public class CannotGetOnlinePriceException extends Exception {

    public CannotGetOnlinePriceException() {
    }

    public CannotGetOnlinePriceException(String msg) {
        super(msg);
    }

}
