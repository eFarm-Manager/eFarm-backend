package com.efarm.efarmbackend.model.agroactivity;

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
public class AgroActivityId implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 7303703925714351179L;

    @NotNull
    @Column(name = "idZabieg", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    public AgroActivityId(Integer id, Integer farmId) {
        this.id = id;
        this.farmId = farmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgroActivityId entity = (AgroActivityId) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.farmId, entity.farmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, farmId);
    }
}