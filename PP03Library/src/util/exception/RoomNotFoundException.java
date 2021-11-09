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
public class RoomNotFoundException extends Exception {

    public RoomNotFoundException() {
    }

    public RoomNotFoundException(String msg) {
        super(msg);
    }

}
