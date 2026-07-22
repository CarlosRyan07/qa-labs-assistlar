package br.com.ryanqalabs.assistlar.cliente.api;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.ryanqalabs.assistlar.cliente.aplicacao.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Cadastro e ciclo de vida dos clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um cliente",
            description = "Exige nome e e-mail validos e cliente com idade entre 18 e 120 anos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado"),
            @ApiResponse(responseCode = "400", description = "Payload, nome, e-mail ou data invalida",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Cliente menor de 18 anos",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ClienteResposta> cadastrar(@Valid @RequestBody ClienteCadastroRequisicao requisicao) {
        ClienteResposta resposta = service.cadastrar(requisicao);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resposta.id())
                .toUri();
        return ResponseEntity.created(localizacao).body(resposta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um cliente por UUID")
    public ResponseEntity<ClienteResposta> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/inativacao")
    @Operation(summary = "Inativa um cliente")
    public ResponseEntity<ClienteResposta> inativar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.inativar(id));
    }

    @PostMapping("/{id}/reativacao")
    @Operation(summary = "Reativa um cliente")
    public ResponseEntity<ClienteResposta> reativar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.reativar(id));
    }
}
