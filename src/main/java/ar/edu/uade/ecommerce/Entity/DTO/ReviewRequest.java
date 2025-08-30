package ar.edu.uade.ecommerce.Entity.DTO;

import lombok.Data;

@Data
public class ReviewRequest {
    private float calification;
    private String description;

    public float getCalification() {
        return calification;
    }

    public void setCalification(float calification) {
        this.calification = calification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

