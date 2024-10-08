package com.efarm.efarmbackend.model.landparcel;

import com.efarm.efarmbackend.model.farm.Farm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "Dzialka")
public class Landparcel {

    @EmbeddedId
    private LandparcelId id;

    @MapsId("gospodarstwoIdgospodarstwo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Gospodarstwo_idGospodarstwo", nullable = false, referencedColumnName = "idGospodarstwo")
    private Farm farm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "StanWlasnosciDzialki_idStanWlasnosciDzialki", nullable = false)
    private LandOwnershipStatus landOwnershipStatus;

    @Size(max = 45)
    @NotNull
    @Column(name = "wojewodztwo", nullable = false, length = 45)
    private String voivodeship;

    @Size(max = 60)
    @NotNull
    @Column(name = "powiat", nullable = false, length = 60)
    private String district;

    @Size(max = 60)
    @NotNull
    @Column(name = "gmina", nullable = false, length = 60)
    private String commune;

    @Size(max = 10)
    @NotNull
    @Column(name = "numerObrebuEwidencyjnego", nullable = false, length = 10)
    private String geodesyRegistrationDistrictNumber;

    @Size(max = 10)
    @NotNull
    @Column(name = "numerDzialki", nullable = false, length = 10)
    private String landparcelNumber;

    @NotNull
    @Column(name = "dlugoscGeograficzna", nullable = false)
    private Double longitude;

    @NotNull
    @Column(name = "szerokoscGeograficzna", nullable = false)
    private Double latitude;

    @NotNull
    @Column(name = "powierzchniaDzialki", nullable = false)
    private Double area;

    @NotNull
    @Column(name = "czyDostepna", nullable = false)
    private Boolean isAvailable = false;

    public Landparcel(Landparcel landparcel) {
        this.id = landparcel.getId();
        this.farm = landparcel.getFarm();
        this.landOwnershipStatus = landparcel.getLandOwnershipStatus();
        this.voivodeship = landparcel.getVoivodeship();
        this.district = landparcel.getDistrict();
        this.commune = landparcel.getCommune();
        this.geodesyRegistrationDistrictNumber = landparcel.getGeodesyRegistrationDistrictNumber();
        this.landparcelNumber = landparcel.getLandparcelNumber();
        this.longitude = landparcel.getLongitude();
        this.latitude = landparcel.getLatitude();
        this.area = landparcel.getArea();
        this.isAvailable = landparcel.getIsAvailable();
    }

    public Landparcel(LandparcelId id, Farm farm) {
        this.id = id;
        this.farm = farm;
        this.isAvailable = true;
    }

    public LandparcelId getId() {
        return id == null ? null : new LandparcelId(id);
    }

    public void setId(LandparcelId id) {
        this.id = id == null ? null : new LandparcelId(id);
    }

    public Farm getFarm() {
        return farm == null ? null : new Farm(farm);
    }

    public void setFarm(Farm farm) {
        this.farm = farm == null ? null : new Farm(farm);
    }

    public LandOwnershipStatus getLandOwnershipStatus() {
        return landOwnershipStatus == null ? null : new LandOwnershipStatus(landOwnershipStatus);
    }

    public void setLandOwnershipStatus(LandOwnershipStatus landOwnershipStatus) {
        this.landOwnershipStatus = landOwnershipStatus == null ? null : new LandOwnershipStatus(landOwnershipStatus);
    }
}