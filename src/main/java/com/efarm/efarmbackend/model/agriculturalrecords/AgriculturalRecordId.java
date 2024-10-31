package com.efarm.efarmbackend.model.agriculturalrecords;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class AgriculturalRecordId implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 9111979005692721600L;

    @NotNull
    @Column(name = "idEwidencja", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgriculturalRecordId entity = (AgriculturalRecordId) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.farmId, entity.farmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, farmId);
    }

}