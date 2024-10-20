package com.efarm.efarmbackend.payload.request.farmfield;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SplitFarmfieldRequest {

    private Integer farmfieldId;

    private List<ParcelSplitData> parcels;

    @Setter
    @Getter
    public static class ParcelSplitData {
        private Integer id;
        private Double area;
    }
}
