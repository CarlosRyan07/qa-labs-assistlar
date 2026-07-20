package br.com.ryanqalabs.assistlar.solicitacao.api;

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

import br.com.ryanqalabs.assistlar.historico.api.HistoricoStatusResposta;
import br.com.ryanqalabs.assistlar.solicitacao.aplicacao.SolicitacaoAssistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes-assistencia")
@Tag(name = "Solicitacoes de assistencia", description = "Atendimentos residenciais solicitados pelos clientes")
public class SolicitacaoAssistenciaController {

    private final SolicitacaoAssistenciaService service;

    public SolicitacaoAssistenciaController(SolicitacaoAssistenciaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Abre uma solicitacao de assistencia")
    public ResponseEntity<SolicitacaoResposta> abrir(@Valid @RequestBody SolicitacaoCriacaoRequisicao requisicao) {
        SolicitacaoResposta resposta = service.abrir(requisicao);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(localizacao).body(resposta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma solicitacao por UUID")
    public ResponseEntity<SolicitacaoResposta> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/{id}/inicio")
    @Operation(summary = "Inicia o atendimento da solicitacao")
    public ResponseEntity<SolicitacaoResposta> iniciar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.iniciar(id));
    }

    @PostMapping("/{id}/conclusao")
    @Operation(summary = "Conclui a solicitacao em atendimento")
    public ResponseEntity<SolicitacaoResposta> concluir(@PathVariable UUID id) {
        return ResponseEntity.ok(service.concluir(id));
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela a solicitacao aberta ou em atendimento")
    public ResponseEntity<SolicitacaoResposta> cancelar(@PathVariable UUID id,
            @Valid @RequestBody SolicitacaoCancelamentoRequisicao requisicao) {
        return ResponseEntity.ok(service.cancelar(id, requisicao));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Lista o historico de status da solicitacao")
    public ResponseEntity<List<HistoricoStatusResposta>> listarHistorico(@PathVariable UUID id) {
        return ResponseEntity.ok(service.listarHistorico(id));
    }
}
