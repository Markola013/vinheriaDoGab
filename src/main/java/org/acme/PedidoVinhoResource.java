package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.headers.Header;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import org.acme.idempotency.Idempotent;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.net.URI;
import jakarta.ws.rs.core.UriBuilder;

@Path("/v1/pedidos")
@Consumes("application/json")
@Produces("application/json")
public class PedidoVinhoResource {

    @GET
    @Operation(summary = "Retorna todos os pedidos de vinho")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoVinho.class, type = SchemaType.ARRAY)))
    @Timeout(3000)
    public Response getAll(){
        return Response.ok(PedidoVinho.listAll()).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Retorna um pedido por ID")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoVinho.class)))
    @APIResponse(responseCode = "404", description = "Não encontrado")
    public Response getById(@PathParam("id") long id){
        PedidoVinho entity = PedidoVinho.findById(id);
        if(entity == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(entity).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Pesquisa pedidos de vinho")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoVinho.class, type = SchemaType.ARRAY)))
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("4") int size
    ){
        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        PanacheQuery<PedidoVinho> query;
        if (q == null || q.isBlank()) {
            query = PedidoVinho.findAll(sortObj);
        } else {
            try {
                // Tenta buscar por data de pedido
                query = PedidoVinho.find("dataPedido = ?1", sortObj, LocalDate.parse(q));
            } catch (Exception e) {
                // Caso contrário, busca por status ou observações
                query = PedidoVinho.find("lower(status) like ?1 or lower(observacoes) like ?1", sortObj, "%" + q.toLowerCase() + "%");
            }
        }
        List<PedidoVinho> pedidos = query.page(Math.max(page, 0), size).list();

        var response = new SearchPedidoVinhoResponse();
        response.Pedidos = pedidos;
        response.TotalPedidos = (int) query.count();
        response.TotalPages = query.pageCount();
        response.HasMore = page < query.pageCount() - 1;
        response.NextPage = response.HasMore ? UriBuilder.fromPath("/v1/pedidos/search").queryParam("q", q).queryParam("page", page + 1).queryParam("size", size).build().toString() : "";

        return Response.ok(response).build();
    }

    @POST
    @Operation(summary = "Cria um pedido de vinho", description = "Requer chave de idempotência")
    @Parameter(name = "X-Idempotency-Key", in = ParameterIn.HEADER, required = true, description = "Chave única para garantir idempotência")
    @APIResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = PedidoVinho.class)))
    @APIResponse(responseCode = "200", description = "Replay", headers = @Header(name = "X-Idempotency-Status", description = "IDEMPOTENT_REPLAY"))
    @Idempotent(expireAfter = 7200)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.75, delay = 10000)
    @Retry(maxRetries = 2, delay = 500)
    @Transactional
    public Response insert(@Valid PedidoVinho pedido){
        if(pedido.vinho != null && pedido.vinho.id != null){
            Vinho v = Vinho.findById(pedido.vinho.id);
            if(v == null) return Response.status(Response.Status.BAD_REQUEST).entity("Vinho inexistente").build();
            pedido.vinho = v;
        }
        if(pedido.varietais != null){
            Set<Varietal> resolved = new HashSet<>();
            for(Varietal r : pedido.varietais){
                if(r.id == null) continue;
                Varietal fetched = Varietal.findById(r.id);
                if(fetched == null) return Response.status(Response.Status.BAD_REQUEST).entity("Varietal inexistente").build();
                resolved.add(fetched);
            }
            pedido.varietais = resolved;
        }
        PedidoVinho.persist(pedido);
        URI location = UriBuilder.fromPath("/v1/pedidos/{id}").build(pedido.id);
        return Response.created(location).entity(pedido).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Deleta um pedido de vinho", description = "Requer chave de idempotência")
    @Parameter(name = "X-Idempotency-Key", in = ParameterIn.HEADER, required = true, description = "Chave única para garantir idempotência")
    @Idempotent
    @Transactional
    public Response delete(@PathParam("id") long id){
        PedidoVinho entity = PedidoVinho.findById(id);
        if(entity == null) return Response.status(Response.Status.NOT_FOUND).build();
        entity.varietais.clear();
        entity.persist();
        PedidoVinho.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Atualiza um pedido de vinho", description = "Requer chave de idempotência")
    @Parameter(name = "X-Idempotency-Key", in = ParameterIn.HEADER, required = true, description = "Chave única para garantir idempotência")
    @Idempotent
    @Transactional
    public Response update(@PathParam("id") long id, @Valid PedidoVinho newPedido){
        PedidoVinho entity = PedidoVinho.findById(id);
        if(entity == null) return Response.status(Response.Status.NOT_FOUND).build();

        entity.dataPedido = newPedido.dataPedido;
        entity.observacoes = newPedido.observacoes;
        entity.status = newPedido.status;

        if(newPedido.vinho != null && newPedido.vinho.id != null){
            Vinho v = Vinho.findById(newPedido.vinho.id);
            if(v == null) return Response.status(Response.Status.BAD_REQUEST).entity("Vinho inexistente").build();
            entity.vinho = v;
        } else {
            entity.vinho = null;
        }

        if(newPedido.varietais != null){
            Set<Varietal> resolved = new HashSet<>();
            for(Varietal r : newPedido.varietais){
                if(r.id == null) continue;
                Varietal fetched = Varietal.findById(r.id);
                if(fetched == null) return Response.status(Response.Status.BAD_REQUEST).entity("Varietal inexistente").build();
                resolved.add(fetched);
            }
            entity.varietais = resolved;
        } else {
            entity.varietais = new HashSet<>();
        }

        return Response.status(Response.Status.OK).entity(entity).build();
    }
}