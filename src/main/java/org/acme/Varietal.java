package org.acme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Varietal extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "O nome do varietal não pode ser vazio")
    @Size(min = 2, max = 50, message = "Nome do varietal deve ter entre 2 e 50 caracteres")
    public String nome;

    @Size(max = 200, message = "A descrição não pode ultrapassar 200 caracteres")
    public String descricao;

    // Many-to-Many inverso: vários varietais podem estar associados a vários pedidos
    @ManyToMany(mappedBy = "varietais", fetch = FetchType.LAZY)
    @JsonIgnore
    public Set<PedidoVinho> pedidos = new HashSet<>();

    public Varietal() {}

    public Varietal(long id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }
}