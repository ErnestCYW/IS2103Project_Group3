/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.exception;

/**
 *
 * @author elgin
 */
public class InvalidEmployeeRoleException extends Exception {

    public InvalidEmployeeRoleException() {
    }
    
    public InvalidEmployeeRoleException(String msg) {
        super(msg);
    }
}
