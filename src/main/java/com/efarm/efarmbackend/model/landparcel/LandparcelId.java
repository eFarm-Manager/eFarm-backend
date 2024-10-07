package com.efarm.efarmbackend.model.landparcel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class LandparcelId implements java.io.Serializable {

    private static final long serialVersionUID = -282994760793441189L;

    @NotNull
    @Column(name = "idDzialka", nullable = false)
    private Integer idDzialka;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandparcelId that = (LandparcelId) o;
        return Objects.equals(farmId, that.farmId) &&
                Objects.equals(idDzialka, that.idDzialka);
    }

    @Override
    public int hashCode() {
        return Objects.hash(farmId, idDzialka);
    }

}