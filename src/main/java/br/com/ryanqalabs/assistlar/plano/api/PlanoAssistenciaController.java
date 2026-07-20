package br.com.ryanqalabs.assistlar.plano.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ryanqalabs.assistlar.plano.aplicacao.PlanoAssistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/planos")
@Tag(name = "Planos", description = "Consulta dos planos de assistencia cadastrados por migration")
public class PlanoAssistenciaController {

    private final PlanoAssistenciaService service;

    public PlanoAssistenciaController(PlanoAssistenciaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os planos ativos")
    public ResponseEntity<List<PlanoResposta>> listar() {
        return ResponseEntity.ok(service.listarAtivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um plano ativo por UUID")
    public ResponseEntity<PlanoResposta> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarAtivo(id));
    }
}
