# Regras de trabalho — AssistLar

Estas regras valem para todo o repositorio e para todos os marcos do MVP.

## Linguagem e escopo

- Use portugues no dominio, no codigo da aplicacao, na API, no banco, nos testes e na documentacao.
- Nao use acentos em identificadores tecnicos, nomes de classes, metodos, atributos, pacotes, endpoints ou tabelas.
- Preserve nomes originais de ferramentas, bibliotecas e padroes tecnicos.
- Nao copie codigo, nomes, endpoints, tabelas, regras, dados ou referencias de empresas reais.
- Nao implemente funcionalidades fora do MVP sem aprovacao.

## Stack obrigatoria

- Java 21 e Spring Boot 4.1.0.
- Maven Wrapper; o build deve falhar quando nao estiver usando JDK 21.
- PostgreSQL `postgres:17.10-alpine3.24` no Docker Compose e no Testcontainers.
- Flyway, Springdoc OpenAPI 3.0.3, REST Assured 6.0.0 e Testcontainers gerenciado pelo Spring Boot.
- Nao use H2.

## Dependencias

- Use o dependency management do Spring Boot sempre que disponivel.
- Antes de adicionar uma dependencia nao prevista, interrompa e apresente: problema resolvido, insuficiencia da solucao atual e impacto no build e na manutencao.
- Nao adicione abstracoes ou frameworks apenas para reduzir codigo repetido pequeno.

## Seguranca e dados

- Nao versione credenciais, segredos, dados pessoais, logs, arquivos de IDE ou relatorios temporarios.
- O MVP nao possui autenticacao. Nao use UUID ou headers ficticios como substitutos de autorizacao.
- `tipoResponsavel` de historico deve ser definido internamente pelo caso de uso; nunca deve ser aceito livremente no payload.
- Respostas de erro nao podem expor stack trace, SQL, classes ou detalhes internos.

## Testes e validacao

- `mvn test` executa testes `*Test`.
- `mvn verify` executa a suite completa, incluindo `*IT` e `*ApiIT`.
- O JaCoCo exige 80% de instrucoes e 70% de branches no codigo relevante.
- Nao remova testes, reduza validacoes ou crie contornos quando Docker, rede, ferramenta ou ambiente impedir uma validacao obrigatoria.
- Uma validacao obrigatoria indisponivel bloqueia commit e avancao do marco.
- Testes concorrentes devem usar sincronizacao deterministica e timeout, nunca `sleep`.

## Git e commits

- Trabalhe na branch `feat/mvp-assistlar`.
- Use Conventional Commits com tipo em ingles e descricao em portugues.
- Revise o diff antes de cada commit.
- Nao crie commit com build ou testes relacionados falhando.
- Cada commit deve ser coeso; nao crie um commit por arquivo nem misture refatoracoes nao relacionadas.
- Nao execute push, merge, release, amend, rebase, force push ou reset destrutivo sem autorizacao expressa.
- Revise e valide cada marco antes de iniciar o seguinte.
