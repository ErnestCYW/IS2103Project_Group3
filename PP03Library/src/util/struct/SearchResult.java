package util.struct;

import entity.RoomRate;
import entity.RoomType;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ernestcyw
 */
public class SearchResult {

    private RoomType roomType;

    private Integer numberOfRooms;

    private Double totalPrice;
    
    /**
     *
     * @param roomType
     * @param numberOfRooms
     * @param totalPrice
     */
    public SearchResult(RoomType roomType, Integer numberOfRooms, Double totalPrice) {
        this.roomType = roomType;
        this.numberOfRooms = numberOfRooms;
        this.totalPrice = totalPrice;
    }

    /**
     * @return the roomType
     */
    public RoomType getRoomType() {
        return roomType;
    }

    /**
     * @return the numberOfRooms
     */
    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    /**
     * @return the totalPrice
     */
    public Double getRoomRate() {
        return totalPrice;
    }

}
