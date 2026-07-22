package br.com.ryanqalabs.assistlar.cliente.aplicacao;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ryanqalabs.assistlar.cliente.api.ClienteCadastroRequisicao;
import br.com.ryanqalabs.assistlar.cliente.api.ClienteResposta;
import br.com.ryanqalabs.assistlar.cliente.dominio.Cliente;
import br.com.ryanqalabs.assistlar.cliente.infraestrutura.ClienteRepository;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.compartilhado.erro.RecursoNaoEncontradoException;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final Clock relogio;

    public ClienteService(ClienteRepository repository, Clock relogio) {
        this.repository = repository;
        this.relogio = relogio;
    }

    @Transactional
    public ClienteResposta cadastrar(ClienteCadastroRequisicao requisicao) {
        String emailNormalizado = requisicao.email().strip().toLowerCase(Locale.ROOT);
        validarEmailDisponivel(emailNormalizado);

        Cliente cliente = Cliente.cadastrar(requisicao.nome(), emailNormalizado, requisicao.dataNascimento(), relogio);
        try {
            return ClienteResposta.de(repository.saveAndFlush(cliente));
        } catch (DataIntegrityViolationException excecao) {
            throw emailJaCadastrado();
        }
    }

    @Transactional(readOnly = true)
    public ClienteResposta buscar(UUID id) {
        return ClienteResposta.de(buscarEntidade(id));
    }

    @Transactional
    public ClienteResposta inativar(UUID id) {
        Cliente cliente = buscarEntidade(id);
        cliente.inativar(Instant.now(relogio));
        return ClienteResposta.de(cliente);
    }

    @Transactional
    public ClienteResposta reativar(UUID id) {
        Cliente cliente = buscarEntidade(id);
        cliente.reativar(Instant.now(relogio));
        return ClienteResposta.de(cliente);
    }

    private void validarEmailDisponivel(String email) {
        if (repository.existsByEmailIgnoreCase(email)) {
            throw emailJaCadastrado();
        }
    }

    private Cliente buscarEntidade(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(
                "cliente-nao-encontrado", "Cliente nao encontrado."));
    }

    private ExcecaoConflito emailJaCadastrado() {
        return new ExcecaoConflito("email-ja-cadastrado", "Ja existe um cliente cadastrado com este e-mail.");
    }
}
