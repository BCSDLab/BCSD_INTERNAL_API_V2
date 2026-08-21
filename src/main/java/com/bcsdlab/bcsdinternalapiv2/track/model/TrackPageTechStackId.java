package com.bcsdlab.bcsdinternalapiv2.track.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TrackPageTechStackId implements Serializable {

    private Long trackPageId;
    private Long techStackId;
}
