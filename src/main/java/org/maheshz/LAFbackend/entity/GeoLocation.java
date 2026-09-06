package org.maheshz.LAFbackend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GeoLocation {
    private Double latitude;
    private Double longitude;
    private String addressText;
}
