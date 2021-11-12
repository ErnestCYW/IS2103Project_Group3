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
public class ReserveRoomException extends Exception {

    public ReserveRoomException() {
    }
    
    public ReserveRoomException(String msg) {
        super(msg);
    }
}
