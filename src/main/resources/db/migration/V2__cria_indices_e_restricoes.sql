CREATE UNIQUE INDEX uq_cliente_email_normalizado ON cliente (LOWER(email));

CREATE UNIQUE INDEX uq_contratacao_vigente_cliente
    ON contratacao (cliente_id)
    WHERE status IN ('PENDENTE', 'ATIVA');

CREATE UNIQUE INDEX uq_solicitacao_em_andamento_tipo
    ON solicitacao_assistencia (contratacao_id, tipo_assistencia)
    WHERE status IN ('ABERTA', 'EM_ATENDIMENTO');

CREATE INDEX ix_contratacao_cliente ON contratacao (cliente_id);
CREATE INDEX ix_solicitacao_contratacao ON solicitacao_assistencia (contratacao_id);
CREATE INDEX ix_historico_entidade ON historico_status (tipo_entidade, entidade_id, registrado_em);
