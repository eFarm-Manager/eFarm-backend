package com.efarm.efarmbackend.payload.request.agriculturalrecord;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewAgriculturalRecordRequest {
    @NotNull
    private Integer landparcelId;

    @NotNull
    private String cropName;

    private String season;

    @NotNull
    @Min(0)
    private Double area;

    private String description;
}
