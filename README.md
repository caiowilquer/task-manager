# Task Manager

Aplicação full stack para gerenciamento de projetos e tarefas em equipes de desenvolvimento, desenvolvida como parte do **Desafio Técnico Java 2026**.

O sistema permite que usuários criem, acompanhem e organizem tarefas dentro de projetos, aplicando regras de autenticação, autorização, limite de trabalho em andamento, histórico de alterações e controle de acesso por projeto.

## Funcionalidades

### Autenticação e segurança

- Cadastro e autenticação com e-mail e senha
- Autenticação stateless com JWT
- Senhas armazenadas com BCrypt
- Perfis de acesso `ADMIN` e `MEMBER`
- Autorização baseada no perfil e no pertencimento ao projeto
- Proteção dos endpoints com Spring Security

### Projetos

- Criação, consulta, edição e exclusão de projetos
- Definição automática do criador como dono do projeto
- Gerenciamento de membros
- Restrição das operações administrativas ao dono do projeto
- Listagem apenas dos projetos acessíveis ao usuário autenticado

### Tarefas

- Criação, consulta, edição e exclusão de tarefas
- Atribuição de responsável
- Alteração de status por formulário ou drag and drop
- Prioridades `LOW`, `MEDIUM`, `HIGH` e `CRITICAL`
- Status `TODO`, `IN_PROGRESS` e `DONE`
- Filtros, busca textual, ordenação e paginação
- Histórico completo de alterações
- Relatório agregado por status e prioridade

### Regras de negócio

- Uma tarefa `DONE` não pode voltar diretamente para `TODO`
- Uma tarefa finalizada pode retornar apenas para `IN_PROGRESS`
- Tarefas `CRITICAL` somente podem ser concluídas pelo dono `ADMIN` do projeto
- O responsável atribuído deve ser membro do projeto
- Cada usuário pode possuir no máximo cinco tarefas em `IN_PROGRESS`
- O limite WIP é protegido contra transições concorrentes

---

## Tecnologias

### Backend

- Java 21
- Spring Boot 3.5
- Spring MVC
- Spring Data JPA
- Spring Security
- JWT
- Bean Validation
- PostgreSQL
- Flyway
- Caffeine Cache
- Problem Details / RFC 7807
- OpenAPI e Swagger UI
- JUnit 5
- Mockito
- Testcontainers
- JaCoCo

### Frontend

- React 18
- TypeScript
- Vite
- React Router
- TanStack Query
- Zustand
- Axios
- dnd-kit
- Vitest
- React Testing Library
- Playwright

### Infraestrutura

- Docker
- Docker Compose
- Nginx
- GitHub Actions

---

## Arquitetura

O backend utiliza uma arquitetura em camadas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Responsabilidades principais:

```text
controller  → entrada e saída HTTP
service     → regras de negócio e coordenação das operações
repository  → acesso e persistência dos dados
entity      → modelo de persistência
dto         → contratos de entrada e saída
mapper      → conversão entre entidades e DTOs
security    → autenticação e autorização
exception   → tratamento padronizado de erros
config      → configurações da aplicação
```

O frontend separa estado local e estado remoto:

```text
Zustand
→ sessão, token JWT e usuário autenticado

TanStack Query
→ projetos, tarefas, membros, relatórios e histórico
```

Essa separação evita duplicação de estado e simplifica cache, invalidação e atualização dos dados da API.

---

## Pré-requisitos

Para executar o projeto localmente, instale:

- Java 21
- Docker e Docker Compose
- Node.js 20 ou superior
- npm
- Git

Verifique as instalações:

```bash
java -version
docker --version
docker compose version
node -v
npm -v
git --version
```

---

## Executar com Docker

### 1. Configure as variáveis de ambiente

Copie o arquivo de exemplo:

```bash
cp .env.example .env
```

Para ambientes diferentes do desenvolvimento local, gere um segredo JWT:

```bash
openssl rand -base64 48
```

Atualize a variável correspondente no arquivo `.env`:

```env
JWT_SECRET=SEU_SEGREDO_BASE64
```

### 2. Inicie a aplicação

```bash
docker compose up --build
```

Para executar em segundo plano:

```bash
docker compose up -d --build
```

### 3. Verifique os serviços

```bash
docker compose ps
```

### Acessos

| Serviço | Endereço |
|---|---|
| Frontend | `http://localhost:3000` |
| API | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Health check | `http://localhost:8080/actuator/health` |

---

## Credenciais locais

O profile local cria usuários de demonstração por meio do bootstrap:

| Perfil | E-mail | Senha |
|---|---|---|
| `ADMIN` | `admin@taskmanager.local` | `Admin@123` |
| `MEMBER` | `member@taskmanager.local` | `Member@123` |

O bootstrap é controlado pela variável:

```env
BOOTSTRAP_ENABLED=true
```

Em produção, essa configuração deve permanecer desativada:

```env
BOOTSTRAP_ENABLED=false
```

---

## Executar em desenvolvimento

Nesse modo, apenas o PostgreSQL será executado em container. O backend e o frontend serão iniciados localmente.

### Banco de dados

```bash
docker compose up -d postgres
```

Verifique se o banco está saudável:

```bash
docker compose ps
```

### Backend

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

Também é possível executar diretamente pelo IntelliJ utilizando a classe:

```text
com.caiowilquer.taskmanager.TaskManagerApplication
```

Utilize o profile:

```text
local
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

O frontend será disponibilizado no endereço exibido pelo Vite, normalmente:

```text
http://localhost:5173
```

Durante o desenvolvimento, o Vite encaminha requisições iniciadas por `/api` para:

```text
http://localhost:8080
```

---

## Banco de dados

A aplicação utiliza PostgreSQL e migrations versionadas com Flyway.

As migrations estão localizadas em:

```text
src/main/resources/db/migration
```

Exemplo:

```text
V1__create_users.sql
V2__create_projects.sql
V3__create_tasks_and_audit.sql
```

O Hibernate utiliza:

```yaml
ddl-auto: validate
```

Dessa forma, a estrutura do banco não é modificada automaticamente pelas entidades. Toda alteração estrutural deve ser registrada em uma migration.

---

## Autenticação

### Cadastro

```http
POST /api/v1/auth/register
```

Exemplo:

```json
{
  "name": "Maria Silva",
  "email": "maria@email.com",
  "password": "Senha@123"
}
```

### Login

```http
POST /api/v1/auth/login
```

Exemplo:

```json
{
  "email": "admin@taskmanager.local",
  "password": "Admin@123"
}
```

Após o login, envie o token nos endpoints protegidos:

```http
Authorization: Bearer SEU_TOKEN_JWT
```

---

## Regras de autorização

- Todo usuário autenticado enxerga apenas projetos dos quais participa
- Apenas usuários `ADMIN` podem criar projetos
- O criador do projeto se torna automaticamente o dono
- O dono também é inserido como membro do projeto
- Apenas o dono pode editar ou excluir o projeto
- Apenas o dono pode adicionar ou remover membros
- Usuários `ADMIN` e `MEMBER` podem gerenciar tarefas quando pertencem ao projeto
- O responsável de uma tarefa deve ser membro do projeto
- Tarefas `CRITICAL` somente podem ser concluídas pelo dono `ADMIN`

---

## Regras de tarefas

### Status

```text
TODO
IN_PROGRESS
DONE
```

### Prioridades

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### Transições

Transição proibida:

```text
DONE → TODO
```

Transição permitida:

```text
DONE → IN_PROGRESS
```

### Limite WIP

Cada responsável pode ter no máximo cinco tarefas com status:

```text
IN_PROGRESS
```

Para reduzir o risco de duas requisições simultâneas ultrapassarem o limite, a validação utiliza bloqueio pessimista na linha do usuário durante a transação.

---

## Filtros e ordenação

A listagem de tarefas aceita os seguintes parâmetros:

| Parâmetro | Descrição |
|---|---|
| `status` | Filtra pelo status |
| `priority` | Filtra pela prioridade |
| `assigneeId` | Filtra pelo responsável |
| `createdFrom` | Data inicial de criação |
| `createdTo` | Data final de criação |
| `deadlineFrom` | Prazo inicial |
| `deadlineTo` | Prazo final |
| `query` | Busca no título ou descrição |
| `page` | Número da página |
| `size` | Quantidade de registros |
| `sortBy` | Campo utilizado na ordenação |
| `direction` | Direção `ASC` ou `DESC` |

Exemplo:

```http
GET /api/v1/projects/{projectId}/tasks?status=IN_PROGRESS&priority=HIGH&page=0&size=20&sortBy=DEADLINE&direction=ASC
```

Campos de ordenação disponíveis:

```text
PRIORITY
CREATED_AT
DEADLINE
```

---

## Busca textual

```http
GET /api/v1/projects/{projectId}/tasks/search?q=autenticacao
```

A busca considera:

- título
- descrição

Foram adicionados índices PostgreSQL para melhorar o desempenho das consultas textuais.

---

## Relatório resumido

```http
GET /api/v1/projects/{projectId}/tasks/summary
```

Exemplo de resposta:

```json
{
  "byStatus": {
    "TODO": 12,
    "IN_PROGRESS": 3,
    "DONE": 45
  },
  "byPriority": {
    "LOW": 8,
    "MEDIUM": 21,
    "HIGH": 25,
    "CRITICAL": 6
  }
}
```

Os contadores são calculados diretamente no banco com consultas agregadas utilizando `COUNT` e `GROUP BY`.

O resultado é armazenado em cache por projeto e invalidado sempre que uma tarefa é criada, alterada, removida ou tem seu status modificado.

---

## Histórico de alterações

```http
GET /api/v1/projects/{projectId}/tasks/{taskId}/history
```

O histórico registra:

- criação da tarefa
- alterações de título
- alterações de descrição
- alterações de prioridade
- alterações de prazo
- mudança de responsável
- mudança de status
- usuário responsável pela alteração
- data e hora da alteração
- valor anterior e novo valor

---

## Principais endpoints

### Autenticação

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login
GET    /api/v1/auth/me
```

### Projetos

```text
POST   /api/v1/projects
GET    /api/v1/projects
GET    /api/v1/projects/{projectId}
PUT    /api/v1/projects/{projectId}
DELETE /api/v1/projects/{projectId}
```

### Membros

```text
GET    /api/v1/projects/{projectId}/members
POST   /api/v1/projects/{projectId}/members
DELETE /api/v1/projects/{projectId}/members/{userId}
```

### Tarefas

```text
POST   /api/v1/projects/{projectId}/tasks
GET    /api/v1/projects/{projectId}/tasks
GET    /api/v1/projects/{projectId}/tasks/search
GET    /api/v1/projects/{projectId}/tasks/summary
GET    /api/v1/projects/{projectId}/tasks/{taskId}
PUT    /api/v1/projects/{projectId}/tasks/{taskId}
PATCH  /api/v1/projects/{projectId}/tasks/{taskId}/status
DELETE /api/v1/projects/{projectId}/tasks/{taskId}
GET    /api/v1/projects/{projectId}/tasks/{taskId}/history
```

A documentação completa está disponível no Swagger UI.

---

## Tratamento de erros

A API utiliza o padrão Problem Details, baseado na RFC 7807.

Exemplo:

```json
{
  "type": "https://task-manager/errors/business-rule",
  "title": "Regra de negócio violada",
  "status": 422,
  "detail": "Uma tarefa Finalizada não pode voltar diretamente para A FAZER. Use EM ANDAMENTO.",
  "instance": "/api/v1/projects/123/tasks/456/status"
}
```

Principais status HTTP:

| Status | Uso |
|---|---|
| `200` | Operação realizada com sucesso |
| `201` | Recurso criado |
| `204` | Operação concluída sem corpo de resposta |
| `400` | Dados de entrada inválidos |
| `401` | Usuário não autenticado |
| `403` | Usuário sem permissão |
| `404` | Recurso não encontrado |
| `409` | Conflito de dados |
| `422` | Regra de negócio violada |

---

## Testes

### Testes unitários do backend

```bash
./mvnw test
```

Os testes unitários utilizam:

- JUnit 5
- Mockito
- AssertJ

As principais regras testadas incluem:

- bloqueio de `DONE → TODO`
- limite de cinco tarefas em andamento
- conclusão de tarefa crítica
- autorização de projetos
- cadastro e autenticação

### Verificação completa do backend

```bash
./mvnw clean verify
```

Esse comando executa:

- compilação
- testes unitários
- testes de integração
- JaCoCo
- validação do build

Os testes de integração utilizam PostgreSQL real por meio do Testcontainers.

O relatório de cobertura é criado em:

```text
target/site/jacoco/index.html
```

No macOS:

```bash
open target/site/jacoco/index.html
```

### Testes do frontend

```bash
cd frontend
npm install
npm run test
npm run build
```

### Testes E2E

Com PostgreSQL, backend e frontend em execução:

```bash
cd frontend
npx playwright install chromium
npm run e2e
```

O fluxo E2E valida uma regra crítica da aplicação utilizando navegador, frontend, API e banco de dados.

---

## Gerenciamento de estado

O frontend utiliza Zustand apenas para o estado global da sessão:

```text
token JWT
usuário autenticado
perfil de acesso
logout
```

Os dados obtidos da API são gerenciados pelo TanStack Query:

```text
projetos
tarefas
membros
relatórios
histórico
```

Essa estratégia permite utilizar cache, refetch e invalidação sem duplicar os dados remotos em uma store global.

---

## Cache

O relatório resumido utiliza Caffeine Cache.

A chave do cache é o identificador do projeto.

O cache é invalidado nas operações que podem alterar os contadores:

- criação de tarefa
- atualização de tarefa
- alteração de status
- alteração de responsável
- exclusão de tarefa

Essa abordagem reduz consultas repetidas sem manter informações desatualizadas após operações de escrita.

---

## Integração contínua

O workflow está localizado em:

```text
.github/workflows/ci.yml
```

A pipeline executa:

### Backend

```text
compilação
testes unitários
testes de integração
JaCoCo
```

### Frontend

```text
instalação das dependências
lint
testes
build
```

### E2E

```text
inicialização da aplicação
execução do fluxo Playwright
```

O workflow é executado em pushes e pull requests direcionados à branch `main`.

---

## Decisões técnicas

### Arquitetura em camadas

Foi escolhida por fornecer separação clara de responsabilidades e ser adequada ao tamanho do projeto.

Uma arquitetura hexagonal completa foi considerada, mas adicionaria abstrações e interfaces sem benefício proporcional ao escopo do desafio.

### PostgreSQL em vez de H2

O PostgreSQL reduz diferenças entre desenvolvimento, testes e produção.

Como trade-off, o projeto depende de um banco externo, mitigado pelo Docker Compose e pelo Testcontainers.

### Flyway em vez de `ddl-auto=update`

As migrations deixam as alterações estruturais versionadas e reproduzíveis.

### Zustand em vez de Redux

O estado global da aplicação é pequeno e não justifica o volume adicional de configuração e boilerplate do Redux.

### TanStack Query para estado remoto

Projetos, tarefas e relatórios pertencem ao servidor e não precisam ser duplicados em uma store global.

### Mapeamento manual

O mapeamento entre entidades e DTOs foi implementado manualmente para manter o fluxo explícito e evitar dependências adicionais em um domínio de tamanho reduzido.

---

## Melhorias futuras

Com mais tempo, poderiam ser adicionados:

- recuperação de senha
- refresh token
- convite de membros por e-mail
- alteração de perfil por endpoint administrativo
- anexos em tarefas
- comentários
- notificações em tempo real
- WebSocket
- logs estruturados
- métricas com Prometheus
- observabilidade distribuída
- testes de carga
- controle de permissões mais granular
- deploy automatizado

---

## Estrutura resumida

```text
task-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── frontend/
├── docs/
├── .github/workflows/
├── compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Autor

Desenvolvido por **Caio Wilquer** como parte do Desafio Técnico Java 2026.
