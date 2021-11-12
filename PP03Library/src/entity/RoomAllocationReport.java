/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author ernestcyw
 */
@Entity
public class RoomAllocationReport implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RoomAllocationReportId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @NotNull
    private Date date;
    
    private List<String> noAvailableRoomUpgrade;
    
    private List<String> noAvailableRoomNoUpgrade;
    
    public RoomAllocationReport() {
        this.date = new Date();
    }

    public Long getRoomAllocationReportId() {
        return RoomAllocationReportId;
    }

    public void setRoomAllocationReportId(Long RoomAllocationReportId) {
        this.RoomAllocationReportId = RoomAllocationReportId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (RoomAllocationReportId != null ? RoomAllocationReportId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the RoomAllocationReportId fields are not set
        if (!(object instanceof RoomAllocationReport)) {
            return false;
        }
        RoomAllocationReport other = (RoomAllocationReport) object;
        if ((this.RoomAllocationReportId == null && other.RoomAllocationReportId != null) || (this.RoomAllocationReportId != null && !this.RoomAllocationReportId.equals(other.RoomAllocationReportId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.RoomAllocationReport[ id=" + RoomAllocationReportId + " ]";
    }
    
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the noAvailableRoomUpgrade
     */
    public List<String> getNoAvailableRoomUpgrade() {
        return noAvailableRoomUpgrade;
    }

    /**
     * @param noAvailableRoomUpgrade the noAvailableRoomUpgrade to set
     */
    public void setNoAvailableRoomUpgrade(List<String> noAvailableRoomUpgrade) {
        this.noAvailableRoomUpgrade = noAvailableRoomUpgrade;
    }

    /**
     * @return the noAvailableRoomNoUpgrade
     */
    public List<String> getNoAvailableRoomNoUpgrade() {
        return noAvailableRoomNoUpgrade;
    }

    /**
     * @param noAvailableRoomNoUpgrade the noAvailableRoomNoUpgrade to set
     */
    public void setNoAvailableRoomNoUpgrade(List<String> noAvailableRoomNoUpgrade) {
        this.noAvailableRoomNoUpgrade = noAvailableRoomNoUpgrade;
    }
    
}
