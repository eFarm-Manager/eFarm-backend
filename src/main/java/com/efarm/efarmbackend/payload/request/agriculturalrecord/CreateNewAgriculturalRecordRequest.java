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
    @DecimalMin(value = "0.01", message = "Nie możesz podać wartości mniejszej niż 0.01 ha")
    @Digits(integer = 5, fraction = 2, message = "Możesz podać maksymalnie dwie cyfry po przecinku")
    private Double area;

    private String description;
}
