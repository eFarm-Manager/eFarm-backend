package com.efarm.efarmbackend.model.landparcel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class LandparcelId implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = -282994760793441189L;

    @NotNull
    @Column(name = "idDzialka", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandparcelId that = (LandparcelId) o;
        return Objects.equals(id, that.id) && Objects.equals(farmId, that.farmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, farmId);
    }

    public LandparcelId(LandparcelId landparcelId) {
        this.id = landparcelId.id;
        this.farmId = landparcelId.farmId;
    }

    public LandparcelId(Integer id, Integer farmId) {
        this.id = id;
        this.farmId = farmId;
    }
}