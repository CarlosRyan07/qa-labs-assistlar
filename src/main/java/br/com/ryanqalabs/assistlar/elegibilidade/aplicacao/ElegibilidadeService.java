package br.com.ryanqalabs.assistlar.elegibilidade.aplicacao;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.cliente.infraestrutura.ClienteRepository;
import br.com.ryanqalabs.assistlar.compartilhado.erro.RecursoNaoEncontradoException;
import br.com.ryanqalabs.assistlar.elegibilidade.api.ElegibilidadeResposta;
import br.com.ryanqalabs.assistlar.elegibilidade.dominio.AvaliadorElegibilidade;
import br.com.ryanqalabs.assistlar.elegibilidade.dominio.ContextoElegibilidade;
import br.com.ryanqalabs.assistlar.elegibilidade.dominio.ResultadoElegibilidade;
import br.com.ryanqalabs.assistlar.elegibilidade.infraestrutura.ConsultaContratacaoVigente;
import br.com.ryanqalabs.assistlar.plano.dominio.PlanoAssistencia;
import br.com.ryanqalabs.assistlar.plano.infraestrutura.PlanoAssistenciaRepository;

@Service
public class ElegibilidadeService {

    private final ClienteRepository clienteRepository;
    private final PlanoAssistenciaRepository planoRepository;
    private final ConsultaContratacaoVigente consultaContratacaoVigente;
    private final Clock relogio;
    private final AvaliadorElegibilidade avaliador = new AvaliadorElegibilidade();

    public ElegibilidadeService(ClienteRepository clienteRepository, PlanoAssistenciaRepository planoRepository,
            ConsultaContratacaoVigente consultaContratacaoVigente, Clock relogio) {
        this.clienteRepository = clienteRepository;
        this.planoRepository = planoRepository;
        this.consultaContratacaoVigente = consultaContratacaoVigente;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public ElegibilidadeResposta consultar(UUID clienteId, UUID planoId) {
        ResultadoElegibilidade resultado = avaliar(clienteId, planoId);
        return ElegibilidadeResposta.de(resultado);
    }

    @Transactional(readOnly = true)
    public ResultadoElegibilidade avaliar(UUID clienteId, UUID planoId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new RecursoNaoEncontradoException(
                "cliente-nao-encontrado", "Cliente nao encontrado."));
        PlanoAssistencia plano = planoRepository.findById(planoId).orElseThrow(() -> new RecursoNaoEncontradoException(
                "plano-nao-encontrado", "Plano de assistencia nao encontrado."));

        ContextoElegibilidade contexto = new ContextoElegibilidade(cliente.getId(), cliente.getStatus(),
                cliente.getDataNascimento(), plano.getId(), plano.isAtivo(),
                consultaContratacaoVigente.existeParaCliente(clienteId));
        return avaliador.avaliar(contexto, relogio);
    }
}
