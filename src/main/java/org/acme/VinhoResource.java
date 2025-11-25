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

import java.util.List;
import java.net.URI;
import jakarta.ws.rs.core.UriBuilder;

@Path("/v1/vinhos")
@Consumes("application/json")
@Produces("application/json")
public class VinhoResource {

    @GET
    @Operation(summary = "Retorna todos os vinhos")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Vinho.class, type = SchemaType.ARRAY)))
    @Timeout(3000)
    public Response getAll(){
        return Response.ok(Vinho.listAll()).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Retorna um vinho por ID")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Vinho.class)))
    @APIResponse(responseCode = "404", description = "Não encontrado")
    public Response getById(@PathParam("id") long id){
        Vinho entity = Vinho.findById(id);
        if(entity == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(entity).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Pesquisa vinhos")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Vinho.class, type = SchemaType.ARRAY)))
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("4") int size
    ){
        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        PanacheQuery<Vinho> query;
        if (q == null || q.isBlank()) {
            query = Vinho.findAll(sortObj);
        } else {
            // Busca por nome ou origem
            query = Vinho.find("lower(nome) like ?1 or lower(origem) like ?1", sortObj, "%" + q.toLowerCase() + "%");
        }
        List<Vinho> vinhos = query.page(Math.max(page, 0), size).list();

        var response = new SearchVinhoResponse();
        response.Vinhos = vinhos;
        response.TotalVinhos = (int) query.count();
        response.TotalPages = query.pageCount();
        response.HasMore = page < query.pageCount() - 1;
        response.NextPage = response.HasMore ? UriBuilder.fromPath("/v1/vinhos/search").queryParam("q", q).queryParam("page", page + 1).queryParam("size", size).build().toString() : "";

        return Response.ok(response).build();
    }

    @POST
    @Operation(summary = "Cria um vinho", description = "Requer chave de idempotência")
    @Parameter(name = "X-Idempotency-Key", in = ParameterIn.HEADER, required = true, description = "Chave única para garantir idempotência")
    @APIResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Vinho.class)))
    @APIResponse(responseCode = "200", description = "Replay", headers = @Header(name = "X-Idempotency-Status", description = "IDEMPOTENT_REPLAY"))
    @Idempotent(expireAfter = 7200)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.75, delay = 10000)
    @Retry(maxRetries = 2, delay = 500)
    @Transactional
    public Response insert(@Valid Vinho vinho){
        Vinho.persist(vinho);
        URI location = UriBuilder.fromPath("/v1/vinhos/{id}").build(vinho.id);
        return Response.created(location).entity(vinho).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Deleta um vinho", description = "Requer chave de idempotência")
    @Parameter(name = "X-Idempotency-Key", in = ParameterIn.HEADER, required = true, description = "Chave única para garantir idempotência")
    @Idempotent
    @Transactional
    public Response delete(@PathParam("id") long id){
        Vinho entity = Vinho.findById(id);
        if(entity == null) return Response.status(Response.Status.NOT_FOUND).build();

        if(PedidoVinho.count("vinho.id = ?1", id) > 0) {
            return Response.status(Response.Status.CONFLICT).entity("Possui pedidos vinculados").build();
        }

        Vinho.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Atualiza um vinho", description = "Requer chave de idempotência")
    @Parameter(name = "X-Idempotency-Key", in = ParameterIn.HEADER, required = true, description = "Chave única para garantir idempotência")
    @Idempotent
    @Transactional
    public Response update(@PathParam("id") long id, @Valid Vinho newVinho){
        Vinho entity = Vinho.findById(id);
        if(entity == null) return Response.status(Response.Status.NOT_FOUND).build();

        entity.nome = newVinho.nome;
        entity.dataDeProducao = newVinho.dataDeProducao;
        entity.origem = newVinho.origem;

        if(newVinho.ficha != null){
            if(entity.ficha == null) entity.ficha = new FichaVinho();
            entity.ficha.descricaoAroma = newVinho.ficha.descricaoAroma;
            entity.ficha.tipoUva = newVinho.ficha.tipoUva;
            entity.ficha.harmonizacao = newVinho.ficha.harmonizacao;
        } else {
            entity.ficha = null;
        }
        return Response.ok(entity).build();
    }
}