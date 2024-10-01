package com.efarm.efarmbackend.model.equipment;

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
@Embeddable
@NoArgsConstructor
public class FarmEquipmentId implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 291875531197869384L;
    @NotNull
    @Column(name = "idSprzet", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    public FarmEquipmentId(FarmEquipmentId other) {
        this.id = other.id;
        this.farmId = other.farmId;
    }

    public FarmEquipmentId(Integer id, Integer farmId) {
        this.id = id;
        this.farmId = farmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FarmEquipmentId entity)) return false;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.farmId, entity.farmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, farmId);
    }
}