package com.efarm.efarmbackend.model.equipment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class FarmEquipmentId implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 291875531197869384L;
    @NotNull
    @Column(name = "idSprzet", nullable = false)
    private Integer idSprzet;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer gospodarstwoIdgospodarstwo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FarmEquipmentId entity = (FarmEquipmentId) o;
        return Objects.equals(this.idSprzet, entity.idSprzet) &&
                Objects.equals(this.gospodarstwoIdgospodarstwo, entity.gospodarstwoIdgospodarstwo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSprzet, gospodarstwoIdgospodarstwo);
    }

}