package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class PedidoVinho extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "A data do pedido não pode ser nula")
    public LocalDate dataPedido;

    @NotBlank(message = "A observação é obrigatória")
    @Size(max = 2000)
    public String observacoes;

    @NotBlank(message = "O status do pedido é obrigatório")
    @Size(max = 50)
    public String status; // Ex: Pendente, Enviado, Cancelado

    // Many-to-One: vários pedidos para um vinho
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vinho_id")
    public Vinho vinho;

    // Many-to-Many: um pedido pode envolver vários varietais (se for um blend)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pedidovinho_varietal",
            joinColumns = @JoinColumn(name = "pedidovinho_id"),
            inverseJoinColumns = @JoinColumn(name = "varietal_id")
    )
    public Set<Varietal> varietais = new HashSet<>();

    public PedidoVinho() {}

    public PedidoVinho(Long id, LocalDate dataPedido, String observacoes, String status) {
        this.id = id;
        this.dataPedido = dataPedido;
        this.observacoes = observacoes;
        this.status = status;
    }
}