package br.com.ryanqalabs.assistlar.elegibilidade.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.ryanqalabs.assistlar.elegibilidade.aplicacao.ElegibilidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/elegibilidades")
@Tag(name = "Elegibilidades", description = "Avaliacao informativa para contratacao de planos")
public class ElegibilidadeController {

    private final ElegibilidadeService service;

    public ElegibilidadeController(ElegibilidadeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Avalia a elegibilidade de um cliente para um plano")
    public ResponseEntity<ElegibilidadeResposta> consultar(
            @RequestParam UUID clienteId,
            @RequestParam UUID planoId) {
        return ResponseEntity.ok(service.consultar(clienteId, planoId));
    }
}
