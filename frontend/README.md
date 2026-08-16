# Frontend do AssistLar

Aplicação Web da evolução `v0.2.0`, construída como uma jornada guiada sobre o
contrato existente do backend.

## Ambiente

- Node.js `24.18.0` LTS;
- npm `11.16.0`;
- React `19.2.8`;
- TypeScript `5.9.3`;
- Vite `8.2.1`;
- Material UI `9.3.1`;
- React Router `7.18.2`;
- TanStack Query `5.101.4`;
- Vitest `4.1.10` e Testing Library.

Confirme o ambiente:

```bash
node --version
npm --version
```

## Configuração

Copie `.env.example` para `.env.local` somente quando precisar sobrescrever os
valores locais.

```text
VITE_API_URL=/api
API_PROXY_TARGET=http://localhost:8080
```

`VITE_API_URL` é exposta à aplicação. `API_PROXY_TARGET` é lida somente pela
configuração do Vite e encaminha `/api` para o backend durante o desenvolvimento.

## Comandos

Execute dentro de `frontend/`:

```bash
npm ci
npm run dev
npm run lint
npm run typecheck
npm run test
npm run build
```

Para utilizar a API durante o desenvolvimento, mantenha o PostgreSQL e o backend
do AssistLar em execução. O frontend fica disponível por padrão em
<http://localhost:5173>.

## Limite funcional atual

A API `v0.1.0` lista apenas planos. A Web será uma jornada guiada: recursos
criados são acessados pelo UUID retornado, e recursos conhecidos podem ser
consultados por UUID. O frontend não simula listagens ou métricas ausentes no
backend.
