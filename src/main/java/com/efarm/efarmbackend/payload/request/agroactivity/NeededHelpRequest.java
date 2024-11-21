package com.efarm.efarmbackend.payload.request.agroactivity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NeededHelpRequest {
    private String name;
    private String description;
    private Integer landparcelId;
    private List<Integer> operatorIds;
    private List<Integer> equipmentIds;
}
