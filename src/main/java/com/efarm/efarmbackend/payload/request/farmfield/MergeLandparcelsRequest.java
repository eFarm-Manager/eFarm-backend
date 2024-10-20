package com.efarm.efarmbackend.payload.request.farmfield;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MergeLandparcelsRequest {

    @NotNull
    private String name;

    @NotNull
    private List<Integer> landparcelIds;
}

