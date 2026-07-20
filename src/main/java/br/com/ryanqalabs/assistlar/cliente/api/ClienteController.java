package br.com.ryanqalabs.assistlar.cliente.api;

import java.net.URI;
import java.util.UUID;

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
    @Operation(summary = "Cadastra um cliente")
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
