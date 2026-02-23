package edts.week8_practice1.entity;

import jakarta.persistence.*;

/**
 * Base entity with soft delete capability.
 * Extends BaseEntity and adds active field for soft delete functionality.
 */
@MappedSuperclass
public abstract class BaseEntityWithSoftDelete extends BaseEntity {

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Check if this entity is active (not soft deleted).
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    /**
     * Soft delete this entity by setting active to false.
     */
    public void softDelete() {
        this.active = false;
    }
}
