package br.com.ryanqalabs.assistlar.cliente.dominio;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoConflito;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoDadosInvalidos;
import br.com.ryanqalabs.assistlar.compartilhado.erro.ExcecaoRegraNegocio;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")
public class Cliente {

    public static final int IDADE_MINIMA = 18;
    public static final int IDADE_MAXIMA = 120;
    public static final int TAMANHO_MINIMO_NOME = 3;
    public static final int TAMANHO_MAXIMO_NOME = 120;

    @Id
    private UUID id;
    private String nome;
    private String email;
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private StatusCliente status;

    private Instant criadoEm;
    private Instant atualizadoEm;

    protected Cliente() {
    }

    private Cliente(UUID id, String nome, String email, LocalDate dataNascimento, Instant instante) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.status = StatusCliente.ATIVO;
        this.criadoEm = instante;
        this.atualizadoEm = instante;
    }

    public static Cliente cadastrar(String nome, String email, LocalDate dataNascimento, Clock relogio) {
        String nomeNormalizado = normalizarNome(nome);
        validarDataNascimento(dataNascimento, relogio);
        return new Cliente(UUID.randomUUID(), nomeNormalizado, email.strip().toLowerCase(Locale.ROOT), dataNascimento,
                Instant.now(relogio));
    }

    private static String normalizarNome(String nome) {
        String nomeNormalizado = nome == null ? "" : nome.strip();
        if (nomeNormalizado.length() < TAMANHO_MINIMO_NOME
                || nomeNormalizado.length() > TAMANHO_MAXIMO_NOME) {
            throw new ExcecaoDadosInvalidos("nome-tamanho-invalido",
                    "O nome deve ter entre 3 e 120 caracteres.");
        }
        return nomeNormalizado;
    }

    public static void validarDataNascimento(LocalDate dataNascimento, Clock relogio) {
        LocalDate hoje = LocalDate.now(relogio);
        if (dataNascimento.isAfter(hoje)) {
            throw new ExcecaoDadosInvalidos("data-nascimento-futura", "A data de nascimento nao pode estar no futuro.");
        }
        if (dataNascimento.isBefore(hoje.minusYears(IDADE_MAXIMA))) {
            throw new ExcecaoDadosInvalidos("idade-maxima-excedida", "A idade do cliente nao pode ser superior a 120 anos.");
        }
        if (dataNascimento.isAfter(hoje.minusYears(IDADE_MINIMA))) {
            throw new ExcecaoRegraNegocio("idade-minima-nao-atendida",
                    "O cliente deve ter pelo menos 18 anos.");
        }
    }

    public void inativar(Instant instante) {
        if (status == StatusCliente.INATIVO) {
            throw new ExcecaoConflito("transicao-cliente-invalida", "O cliente ja esta inativo.");
        }
        status = StatusCliente.INATIVO;
        atualizadoEm = instante;
    }

    public void reativar(Instant instante) {
        if (status == StatusCliente.ATIVO) {
            throw new ExcecaoConflito("transicao-cliente-invalida", "O cliente ja esta ativo.");
        }
        status = StatusCliente.ATIVO;
        atualizadoEm = instante;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public StatusCliente getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
