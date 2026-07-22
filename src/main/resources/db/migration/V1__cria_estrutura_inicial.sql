CREATE TABLE cliente (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    data_nascimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_cliente_status CHECK (status IN ('ATIVO', 'INATIVO'))
);

CREATE TABLE plano_assistencia (
    id UUID PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    ativo BOOLEAN NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_plano_assistencia_codigo UNIQUE (codigo)
);

CREATE TABLE cobertura_assistencia (
    id UUID PRIMARY KEY,
    plano_assistencia_id UUID NOT NULL REFERENCES plano_assistencia(id),
    tipo_assistencia VARCHAR(30) NOT NULL,
    limite_utilizacoes INTEGER NOT NULL,
    CONSTRAINT ck_cobertura_tipo CHECK (tipo_assistencia IN ('ELETRICISTA', 'ENCANADOR', 'CHAVEIRO')),
    CONSTRAINT ck_cobertura_limite_positivo CHECK (limite_utilizacoes > 0),
    CONSTRAINT uq_cobertura_plano_tipo UNIQUE (plano_assistencia_id, tipo_assistencia)
);

CREATE TABLE contratacao (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL REFERENCES cliente(id),
    plano_assistencia_id UUID NOT NULL REFERENCES plano_assistencia(id),
    status VARCHAR(20) NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL,
    ativada_em TIMESTAMPTZ,
    cancelada_em TIMESTAMPTZ,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_contratacao_status CHECK (status IN ('PENDENTE', 'ATIVA', 'CANCELADA'))
);

CREATE TABLE solicitacao_assistencia (
    id UUID PRIMARY KEY,
    contratacao_id UUID NOT NULL REFERENCES contratacao(id),
    tipo_assistencia VARCHAR(30) NOT NULL,
    descricao_problema VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    motivo_cancelamento VARCHAR(500),
    aberta_em TIMESTAMPTZ NOT NULL,
    iniciada_em TIMESTAMPTZ,
    concluida_em TIMESTAMPTZ,
    cancelada_em TIMESTAMPTZ,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_solicitacao_tipo CHECK (tipo_assistencia IN ('ELETRICISTA', 'ENCANADOR', 'CHAVEIRO')),
    CONSTRAINT ck_solicitacao_status CHECK (status IN ('ABERTA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE TABLE historico_status (
    id UUID PRIMARY KEY,
    tipo_entidade VARCHAR(40) NOT NULL,
    entidade_id UUID NOT NULL,
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30) NOT NULL,
    motivo VARCHAR(500),
    tipo_responsavel VARCHAR(20) NOT NULL,
    registrado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_historico_tipo_entidade CHECK (tipo_entidade IN ('CONTRATACAO', 'SOLICITACAO_ASSISTENCIA')),
    CONSTRAINT ck_historico_responsavel CHECK (tipo_responsavel IN ('CLIENTE', 'OPERADOR', 'SISTEMA'))
);
