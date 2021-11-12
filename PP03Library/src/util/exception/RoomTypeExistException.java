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
public class RoomTypeExistException extends Exception {

    public RoomTypeExistException() {
    }

    public RoomTypeExistException(String msg) {
        super(msg);
    }

}
