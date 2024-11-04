package com.efarm.efarmbackend.payload.request.agriculturalrecord;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
    @DecimalMin(value = "0.0001", message = "Nie możesz podać wartości mniejszej niż 0.0001 ha")
    @Digits(integer = 7, fraction = 4, message = "Możesz podać maksymalnie 4 cyfry po przecinku")
    private Double area;

    private String description;
}
