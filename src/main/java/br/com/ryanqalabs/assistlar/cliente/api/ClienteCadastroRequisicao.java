package br.com.ryanqalabs.assistlar.cliente.api;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteCadastroRequisicao(
        @NotBlank(message = "O nome e obrigatorio.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres.")
        @Schema(description = "Nome do cliente", example = "Ana Silva", minLength = 3, maxLength = 120)
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio.")
        @Email(message = "O e-mail deve ter um formato valido.")
        @Size(max = 254, message = "O e-mail deve ter no maximo 254 caracteres.")
        @Schema(description = "E-mail unico do cliente", example = "ana.silva@example.com", maxLength = 254)
        String email,

        @NotNull(message = "A data de nascimento e obrigatoria.")
        @Schema(description = "Data de nascimento; o cliente deve ter entre 18 e 120 anos",
                example = "1990-05-10")
        LocalDate dataNascimento) {
}
