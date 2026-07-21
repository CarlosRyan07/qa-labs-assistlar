package br.com.ryanqalabs.assistlar.suporte;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import br.com.ryanqalabs.assistlar.compartilhado.configuracao.TempoConfiguracao;

@TestConfiguration(proxyBeanMethods = false)
public class TempoFixoTestesConfiguracao {

    public static final Instant INSTANTE_FIXO = Instant.parse("2026-07-20T15:00:00Z");
    public static final LocalDate DATA_CIVIL_FIXA = LocalDate.ofInstant(
            INSTANTE_FIXO, TempoConfiguracao.FUSO_NEGOCIO);

    @Bean
    @Primary
    Clock relogioFixo() {
        return Clock.fixed(INSTANTE_FIXO, TempoConfiguracao.FUSO_NEGOCIO);
    }
}
