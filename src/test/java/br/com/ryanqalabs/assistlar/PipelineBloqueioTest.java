package br.com.ryanqalabs.assistlar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class PipelineBloqueioTest {

    @Test
    void deveFalharIntencionalmenteParaValidarBloqueioDaPipeline() {
        fail("Falha intencional para validar o bloqueio da main pela CI.");
    }
}
