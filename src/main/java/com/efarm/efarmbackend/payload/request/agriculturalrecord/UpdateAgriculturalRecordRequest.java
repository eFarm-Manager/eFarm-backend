package com.efarm.efarmbackend.payload.request.agriculturalrecord;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAgriculturalRecordRequest {

    private String cropName;

    @Min(0)
    private Double area;

    private String description;
}

