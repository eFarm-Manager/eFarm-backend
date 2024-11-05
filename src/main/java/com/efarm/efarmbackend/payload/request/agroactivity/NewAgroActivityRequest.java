package com.efarm.efarmbackend.payload.request.agroactivity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class NewAgroActivityRequest {

    @NotNull(message = "Kategoria zabiegu nie może być pusta")
    @NotBlank(message = "Kategoria zabiegu nie może być pusta")
    @Size(max = 45, message = "Nazwa kategroii zabiegu nie może przekraczać 45 znaków.")
    private String activityCategoryName;

    @NotNull(message = "Kategoria zabiegu nie może być pusta")
    @NotBlank(message = "Nazwa zabiegu zabiegu nie może być pusta")
    @Size(max = 45, message = "Nazwa zabiegu nie może przekraczać 45 znaków.")
    private String name;

    private Instant date;

    private Boolean isCompleted;

    @Size(max = 100, message = "Nazwa użytych środków nie może przekraczać 100 znaków")
    private String usedSubstances;

    @Size(max = 45, message = "Opis zastosowanej dawki nie może przekraczać 45 znaków")
    private String appliedDose;

    @Size(max = 150, message = "Opis zabiegu nie może przekraczać 150 znaków")
    private String description;

    private Integer agriculturalRecordId;

    private List<Integer> operatorIds;

    private List<Integer> equipmentIds;
}

