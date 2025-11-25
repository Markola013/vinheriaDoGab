package org.acme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
public class Vinho extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(readOnly = true)
    public Long id;

    @NotBlank(message = "O nome do vinho não pode ser vazio")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    public String nome;

    @Past(message = "A data de produção deve ser no passado")
    public LocalDate dataDeProducao;

    @NotBlank(message = "A origem (país/região) é obrigatória")
    @Size(max = 80)
    public String origem;

    // One-to-One: um vinho tem uma ficha detalhada
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "ficha_vinho_id")
    public FichaVinho ficha;

    // One-to-Many: um vinho pode ter várias solicitações de pedido
    @OneToMany(mappedBy = "vinho", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<PedidoVinho> pedidos = new ArrayList<>();

    public Vinho() {}

    public Vinho(Long id, String nome, LocalDate dataDeProducao, String origem, FichaVinho ficha) {
        this.id = id;
        this.nome = nome;
        this.dataDeProducao = dataDeProducao;
        this.origem = origem;
        this.ficha = ficha;
    }
}