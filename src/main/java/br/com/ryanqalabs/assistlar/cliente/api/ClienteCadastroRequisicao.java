package br.com.ryanqalabs.assistlar.cliente.api;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteCadastroRequisicao(
        @NotBlank(message = "O nome e obrigatorio.")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio.")
        @Email(message = "O e-mail deve ter um formato valido.")
        @Size(max = 254, message = "O e-mail deve ter no maximo 254 caracteres.")
        String email,

        @NotNull(message = "A data de nascimento e obrigatoria.")
        LocalDate dataNascimento) {
}
