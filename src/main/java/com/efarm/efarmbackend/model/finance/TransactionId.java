package com.efarm.efarmbackend.model.finance;

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
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TransactionId implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = -1335169215874405028L;

    @NotNull
    @Column(name = "idListaTransakcji", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    public TransactionId(TransactionId transactionId) {
        this.id = transactionId.id;
        this.farmId = transactionId.farmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId entity = (TransactionId) o;
        return Objects.equals(this.farmId, entity.farmId) &&
                Objects.equals(this.id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(farmId, id);
    }
}