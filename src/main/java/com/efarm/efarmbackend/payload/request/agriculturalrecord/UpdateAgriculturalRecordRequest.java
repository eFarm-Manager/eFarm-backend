package com.efarm.efarmbackend.payload.request.agriculturalrecord;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAgriculturalRecordRequest {

    private String cropName;

    @DecimalMin(value = "0.01", message = "Nie możesz podać wartości mniejszej niż 0.01 ha")
    @Digits(integer = 7, fraction = 2, message = "Możesz podać maksymalnie dwie cyfry po przecinku")
    private Double area;

    private String description;
}

