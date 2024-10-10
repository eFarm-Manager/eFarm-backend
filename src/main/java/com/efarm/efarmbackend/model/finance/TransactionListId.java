package com.efarm.efarmbackend.model.finance;

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
public class TransactionListId implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = -1335169215874405028L;

    @NotNull
    @Column(name = "idListaTransakcji", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "Gospodarstwo_idGospodarstwo", nullable = false)
    private Integer farmId;

    public TransactionListId(TransactionListId transactionListId) {
        this.id = transactionListId.id;
        this.farmId = transactionListId.farmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionListId entity = (TransactionListId) o;
        return Objects.equals(this.farmId, entity.farmId) &&
                Objects.equals(this.id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(farmId, id);
    }
}