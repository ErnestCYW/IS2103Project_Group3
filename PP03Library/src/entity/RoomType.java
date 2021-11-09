/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

/**
 *
 * @author ernestcyw
 */
@Entity
public class RoomType implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomTypeId;
    
    @Column(nullable = false) 
    @NotNull
    private String name;
    
    @Column(nullable = false)
    @NotNull
    private String description;
    
    @Column(nullable = false)
    @NotNull
    private String size;
    
    @Column(nullable = false)
    @NotNull
    private String bed;
    
    @Column(nullable = false)
    @NotNull
    private Integer capacity;
    
    @Column(nullable = false)
    @NotNull
    private List<String> amenities;
    
    @Column(nullable = false)
    @NotNull
    private boolean disabled;
    
    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms;
    
    @OneToMany(mappedBy = "roomType")
    private List<Reservation> reservations;
    
    @OneToMany(mappedBy = "roomType")
    private List<RoomRate> roomRates;
    
    @OneToOne(optional = false)
    @NotNull
    private RoomRate currentRoomRate;
    
    public RoomType() {
    }
    
    public RoomType(String name, String description, String size, String bed, Integer capacity, List<String> amenities){
        this();
        this.name = name;
        this.description = description;
        this.size = size;
        this.bed = bed;
        this.capacity = capacity;
        this.amenities = amenities;
        this.disabled = false;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (roomTypeId != null ? roomTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the roomTypeId fields are not set
        if (!(object instanceof RoomType)) {
            return false;
        }
        RoomType other = (RoomType) object;
        if ((this.roomTypeId == null && other.roomTypeId != null) || (this.roomTypeId != null && !this.roomTypeId.equals(other.roomTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.RoomType[ id=" + roomTypeId + " ]";
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.setName(name);
    }

    /**
     * @return the rooms
     */
    public List<Room> getRooms() {
        return rooms;
    }

    /**
     * @param rooms the rooms to set
     */
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    /**
     * @return the roomRates
     */
    public List<RoomRate> getRoomRates() {
        return roomRates;
    }

    /**
     * @param roomRates the roomRates to set
     */
    public void setRoomRates(List<RoomRate> roomRates) {
        this.roomRates = roomRates;
    }

    /**
     * @return the currentRoomRate
     */
    public RoomRate getCurrentRoomRate() {
        return currentRoomRate;
    }

    /**
     * @param currentRoomRate the currentRoomRate to set
     */
    public void setCurrentRoomRate(RoomRate currentRoomRate) {
        this.currentRoomRate = currentRoomRate;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the size
     */
    public String getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * @return the bed
     */
    public String getBed() {
        return bed;
    }

    /**
     * @param bed the bed to set
     */
    public void setBed(String bed) {
        this.bed = bed;
    }

    /**
     * @return the capacity
     */
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * @param capacity the capacity to set
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the amenities
     */
    public List<String> getAmenities() {
        return amenities;
    }

    /**
     * @param amenities the amenities to set
     */
    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
    
        /**
     * @return the disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    /**
     * @return the reservations
     */
    public List<Reservation> getReservations() {
        return reservations;
    }

    /**
     * @param reservations the reservations to set
     */
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
    
}
