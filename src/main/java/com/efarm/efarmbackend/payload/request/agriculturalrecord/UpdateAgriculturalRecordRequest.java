package com.efarm.efarmbackend.payload.request.agriculturalrecord;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class UpdateAgriculturalRecordRequest {

    private String cropName;

    @Min(0)
    private Double area;
}

