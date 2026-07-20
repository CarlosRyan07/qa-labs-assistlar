package br.com.ryanqalabs.assistlar.contratacao.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.ryanqalabs.assistlar.contratacao.aplicacao.ContratacaoService;
import br.com.ryanqalabs.assistlar.historico.api.HistoricoStatusResposta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contratacoes")
@Tag(name = "Contratacoes", description = "Adesao de clientes aos planos de assistencia")
public class ContratacaoController {

    private final ContratacaoService service;

    public ContratacaoController(ContratacaoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria uma contratacao pendente")
    public ResponseEntity<ContratacaoResposta> criar(@Valid @RequestBody ContratacaoCriacaoRequisicao requisicao) {
        ContratacaoResposta resposta = service.criar(requisicao);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(localizacao).body(resposta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma contratacao por UUID")
    public ResponseEntity<ContratacaoResposta> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/ativacao")
    @Operation(summary = "Ativa uma contratacao pendente")
    public ResponseEntity<ContratacaoResposta> ativar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.ativar(id));
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela uma contratacao pendente ou ativa")
    public ResponseEntity<ContratacaoResposta> cancelar(@PathVariable UUID id,
            @Valid @RequestBody ContratacaoCancelamentoRequisicao requisicao) {
        return ResponseEntity.ok(service.cancelar(id, requisicao));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Lista o historico de status da contratacao")
    public ResponseEntity<List<HistoricoStatusResposta>> listarHistorico(@PathVariable UUID id) {
        return ResponseEntity.ok(service.listarHistorico(id));
    }
}
