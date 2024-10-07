package com.efarm.efarmbackend.model.farmfield;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class FarmfieldId implements java.io.Serializable {

    private static final long serialVersionUID = 2422031663969504233L;

    @NotNull
    @Column(name = "idPole", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    public FarmfieldId(FarmfieldId id) {
        this.id = id.id;
        this.farmId = id.farmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FarmfieldId entity = (FarmfieldId) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.farmId, entity.farmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, farmId);
    }

}