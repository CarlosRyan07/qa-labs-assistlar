package br.com.ryanqalabs.assistlar.compartilhado.configuracao;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TempoConfiguracao {

    public static final ZoneId FUSO_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    @Bean
    Clock relogio() {
        return Clock.system(FUSO_NEGOCIO);
    }
}
