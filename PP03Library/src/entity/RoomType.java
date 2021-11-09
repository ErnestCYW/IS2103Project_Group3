/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import util.enumeration.RoomTypeNameEnum;

/**
 *
 * @author ernestcyw
 */
@Entity
public class RoomType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RoomTypeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) 
    @NotNull
    private RoomTypeNameEnum RoomTypeName;
    
    public RoomType() {
    }
    
    public RoomType(RoomTypeNameEnum RoomTypeName){
        this();
        this.RoomTypeName = RoomTypeName;
    }

    public Long getRoomTypeId() {
        return RoomTypeId;
    }

    public void setRoomTypeId(Long RoomTypeId) {
        this.RoomTypeId = RoomTypeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (RoomTypeId != null ? RoomTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the RoomTypeId fields are not set
        if (!(object instanceof RoomType)) {
            return false;
        }
        RoomType other = (RoomType) object;
        if ((this.RoomTypeId == null && other.RoomTypeId != null) || (this.RoomTypeId != null && !this.RoomTypeId.equals(other.RoomTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.RoomType[ id=" + RoomTypeId + " ]";
    }

    /**
     * @return the RoomTypeName
     */
    public RoomTypeNameEnum getRoomTypeName() {
        return RoomTypeName;
    }

    /**
     * @param RoomTypeName the RoomTypeName to set
     */
    public void setRoomTypeName(RoomTypeNameEnum RoomTypeName) {
        this.setRoomTypeName(RoomTypeName);
    }
    
}
