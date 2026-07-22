package br.com.ryanqalabs.assistlar.elegibilidade.dominio;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.ryanqalabs.assistlar.cliente.dominio.StatusCliente;

public class AvaliadorElegibilidade {

    public static final int IDADE_MINIMA = 18;

    public ResultadoElegibilidade avaliar(ContextoElegibilidade contexto, Clock relogio) {
        List<MotivoInelegibilidade> motivos = new ArrayList<>();

        if (contexto.statusCliente() != StatusCliente.ATIVO) {
            motivos.add(MotivoInelegibilidade.CLIENTE_INATIVO);
        }
        LocalDate dataLimite = LocalDate.now(relogio).minusYears(IDADE_MINIMA);
        if (contexto.dataNascimento().isAfter(dataLimite)) {
            motivos.add(MotivoInelegibilidade.CLIENTE_MENOR_DE_IDADE);
        }
        if (!contexto.planoAtivo()) {
            motivos.add(MotivoInelegibilidade.PLANO_INATIVO);
        }
        if (contexto.possuiContratacaoVigente()) {
            motivos.add(MotivoInelegibilidade.CLIENTE_POSSUI_CONTRATACAO_VIGENTE);
        }

        return new ResultadoElegibilidade(contexto.clienteId(), contexto.planoId(), motivos.isEmpty(), motivos);
    }
}
