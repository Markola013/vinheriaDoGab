package org.acme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
public class FichaVinho extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(readOnly = true)
    public Long id;

    @Size(max = 2000, message = "A descrição de aroma não pode ultrapassar 2000 caracteres")
    @Column(length = 2000)
    public String descricaoAroma;

    @Size(max = 200, message = "O tipo de uva principal não pode ultrapassar 200 caracteres")
    public String tipoUva;

    public String harmonizacao;

    // One-to-One: uma ficha pertence a um vinho
    @OneToOne(mappedBy = "ficha", fetch = FetchType.LAZY)
    @JsonIgnore
    public Vinho vinho;

    public FichaVinho() {}

    public FichaVinho(String descricaoAroma, String tipoUva, String harmonizacao) {
        this.descricaoAroma = descricaoAroma;
        this.tipoUva = tipoUva;
        this.harmonizacao = harmonizacao;
    }
}