package br.com.ryanqalabs.assistlar.plano.aplicacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ryanqalabs.assistlar.compartilhado.erro.RecursoNaoEncontradoException;
import br.com.ryanqalabs.assistlar.plano.api.PlanoResposta;
import br.com.ryanqalabs.assistlar.plano.infraestrutura.PlanoAssistenciaRepository;

@Service
public class PlanoAssistenciaService {

    private final PlanoAssistenciaRepository repository;

    public PlanoAssistenciaService(PlanoAssistenciaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PlanoResposta> listarAtivos() {
        return repository.findAllByAtivoTrueOrderByNomeAsc().stream()
                .map(PlanoResposta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanoResposta buscarAtivo(UUID id) {
        return repository.findByIdAndAtivoTrue(id)
                .map(PlanoResposta::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "plano-nao-encontrado", "Plano de assistencia nao encontrado."));
    }
}
