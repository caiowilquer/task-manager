# Guia completo de implementação

Este documento descreve o que foi construído, como cada parte funciona e por que as decisões foram tomadas. Ele serve como roteiro de estudo antes da entrevista técnica.

## 1. Visão geral do fluxo

Uma requisição de negócio percorre as camadas abaixo:

```text
React -> HTTP/JSON -> Controller -> Service -> Repository -> PostgreSQL
                              |          |
                         autorização   consultas
                         e regras      e persistência
```

A resposta percorre o caminho inverso. Entidades JPA nunca são expostas diretamente: mappers convertem o estado persistido em DTOs de resposta.

## 2. Estrutura do backend

```text
controller  recebe HTTP, valida entrada e define status codes
service     executa casos de uso, autorização e regras de negócio
repository  encapsula persistência e consultas
entity      representa o modelo relacional e comportamento simples
dto         define contratos estáveis de entrada e saída
mapper      converte entidades em DTOs
security    autentica JWT e integra com Spring Security
config      configura JPA, cache, CORS, Swagger e bootstrap
exception   padroniza erros com ProblemDetail
```

A arquitetura em camadas foi escolhida porque é suficiente para o domínio, fácil de explicar e conhecida por equipes Java. Uma arquitetura hexagonal completa criaria interfaces e adapters sem benefício proporcional para este escopo.

## 3. Banco de dados e migrations

O PostgreSQL é iniciado por Docker Compose. O Flyway é a única fonte de criação e evolução do schema:

```text
V1__create_users.sql
V2__create_projects.sql
V3__create_tasks_and_audit.sql
```

O Hibernate usa `ddl-auto: validate`. Dessa forma, ele verifica se as entidades correspondem ao schema, mas não cria nem altera tabelas automaticamente.

As entidades usam UUID para evitar IDs sequenciais previsíveis e facilitar geração distribuída. Projetos, tarefas e usuários possuem `@Version` para detectar atualizações concorrentes e evitar sobrescrita silenciosa.

## 4. Autenticação e autorização

### Cadastro

`POST /api/v1/auth/register` cria sempre um usuário `MEMBER`. O cliente não pode escolher `ADMIN`, evitando escalada de privilégio.

O email é normalizado para minúsculas e a senha é armazenada com BCrypt. Existe também um índice único em `LOWER(email)` no banco para proteger contra duplicidade mesmo em concorrência.

### Login

`POST /api/v1/auth/login` valida email e senha com o `AuthenticationManager`. Em caso de sucesso, `JwtService` cria um token HMAC contendo:

```text
subject  email do usuário
uid      UUID do usuário
role     ADMIN ou MEMBER
iat      momento de emissão
exp      momento de expiração
```

### Requisições autenticadas

`JwtAuthenticationFilter` lê o header `Authorization: Bearer ...`, valida assinatura e expiração, carrega o usuário e preenche o `SecurityContext`.

A API é stateless com `SessionCreationPolicy.STATELESS`; nenhuma sessão HTTP é criada.

### Autorização

A autorização ocorre em dois níveis:

1. `SecurityConfig` protege todos os endpoints e restringe criação de projeto a `ADMIN`.
2. Services validam pertencimento ao projeto, propriedade e regras específicas.

A checagem no service protege o caso de uso mesmo que ele seja chamado por outra entrada no futuro.

## 5. Projetos e membros

Somente `ADMIN` cria projetos. O criador se torna dono e também é inserido em `project_members`.

Manter o dono na tabela de membros permite usar uma única regra de pertencimento para leitura e manipulação de tarefas. O campo `owner_id` continua explícito para operações administrativas.

Somente o dono pode:

```text
alterar o projeto
excluir o projeto
adicionar membros
remover membros
```

Um membro não pode ser removido enquanto possuir tarefas atribuídas naquele projeto. A decisão evita tarefas órfãs e exige reatribuição consciente antes da remoção.

## 6. Tarefas

Toda tarefa pertence a um projeto e contém:

```text
título
descrição
status
prioridade
deadline
responsável
criador
data de criação
data de atualização
versão otimista
```

O responsável é validado por `requireProjectMember`: não basta o usuário existir, ele deve pertencer ao projeto.

### Transição DONE para TODO

`TaskService.validateTransition` impede `DONE -> TODO`. A volta permitida é `DONE -> IN_PROGRESS`, conforme o requisito.

A regra fica no service porque depende do caso de uso e deve produzir um erro de negócio claro.

### Fechamento de tarefa CRITICAL

Ao solicitar `DONE` para uma tarefa `CRITICAL`, o service exige simultaneamente:

```text
usuário com role ADMIN
usuário dono do projeto
```

O projeto não possui uma segunda tabela de papéis internos. Portanto, o dono `ADMIN` representa de forma explícita o “ADMIN do projeto”.

### Limite WIP

O limite é global por responsável: no máximo cinco tarefas `IN_PROGRESS`, independentemente do projeto. Essa interpretação é conservadora porque o requisito não restringe o limite a um projeto.

Somente fazer uma contagem seria vulnerável a corrida. Duas requisições simultâneas poderiam observar quatro tarefas e ambas aprovar uma nova transição.

A implementação:

1. adquire `PESSIMISTIC_WRITE` na linha do usuário;
2. conta as tarefas `IN_PROGRESS`;
3. rejeita a operação quando já existem cinco;
4. altera o status dentro da mesma transação.

O lock é por usuário, não global, reduzindo a área de contenção.

## 7. Filtros, paginação e ordenação

`TaskFilterParams` reúne filtros opcionais:

```text
status
priority
assigneeId
createdFrom / createdTo
deadlineFrom / deadlineTo
query
page / size
sortBy / direction
```

`TaskRepositoryImpl` usa Criteria API para adicionar apenas os predicados recebidos. O tamanho máximo da página é 100.

A prioridade recebe ordenação semântica:

```text
LOW < MEDIUM < HIGH < CRITICAL
```

Sem essa expressão, o enum armazenado como texto seria ordenado alfabeticamente, o que não representa a importância real.

## 8. Busca textual

A busca consulta título e descrição de forma case-insensitive e sempre aplica três proteções:

```text
restrição ao projeto autorizado
paginação
limite de 200 caracteres no termo
```

A migration habilita `pg_trgm` e cria índices GIN sobre `LOWER(title)` e `LOWER(description)`. Isso mantém a flexibilidade de busca por fragmentos com uma estratégia coerente para PostgreSQL.

Em escala muito maior, PostgreSQL Full Text Search ou um mecanismo dedicado seriam alternativas.

## 9. Relatório e cache

O relatório não carrega tarefas para contar em memória. O repository executa agregações `GROUP BY` para status e prioridade.

O resultado é cacheado por `projectId` com Caffeine durante cinco minutos. Somente esse endpoint foi cacheado porque possui baixa cardinalidade e custo previsível.

Criação, edição, exclusão e mudança de status usam `@CacheEvict` para invalidar a chave do projeto.

A autorização ocorre antes da chamada ao método cacheado. Assim, um cache hit nunca permite que alguém fora do projeto receba o relatório.

## 10. Histórico de alterações

A tabela `task_audits` registra:

```text
ação
campo alterado
valor anterior
valor novo
usuário responsável
momento da mudança
```

A auditoria é explícita no service. Isso deixa visível quais alterações possuem valor de negócio e evita registrar alterações internas irrelevantes.

O trade-off documentado é que a auditoria é removida junto com a tarefa por `ON DELETE CASCADE`. Em um sistema regulado, seria preferível soft delete ou retenção independente.

## 11. Tratamento de erros

`GlobalExceptionHandler` usa `ProblemDetail` e diferencia:

```text
400  JSON ou parâmetros inválidos
401  autenticação ausente ou inválida
403  usuário autenticado sem permissão
404  recurso inexistente
409  conflito de unicidade ou concorrência otimista
422  regra de negócio violada
500  erro inesperado sem exposição de detalhes internos
```

Erros inesperados são registrados no log com stack trace, enquanto a API retorna uma mensagem segura.

Handlers específicos do Spring Security usam o mesmo formato para manter consistência antes de a requisição alcançar o controller.

## 12. Estratégia de testes do backend

### Unitários

JUnit 5, Mockito e AssertJ isolam os services e verificam regras como:

```text
cadastro duplicado
cadastro sempre como MEMBER
MEMBER não cria projeto
membro duplicado
remoção de membro com tarefas
DONE não volta para TODO
MEMBER não fecha CRITICAL
sexta tarefa IN_PROGRESS rejeitada
```

### Integração

`CriticalTaskFlowIT` usa `@SpringBootTest`, servidor em porta aleatória e PostgreSQL real via Testcontainers.

O fluxo cobre:

1. cadastro de MEMBER;
2. login de ADMIN;
3. criação de projeto;
4. inclusão do membro;
5. criação de tarefa CRITICAL;
6. tentativa de fechamento pelo MEMBER com resposta 403;
7. transição para `IN_PROGRESS`;
8. fechamento pelo ADMIN;
9. tentativa proibida de `DONE -> TODO`;
10. validação do relatório agregado.

## 13. Estrutura do frontend

O frontend usa React 18, TypeScript e Vite.

```text
pages       telas e composição de casos de uso
components  elementos reutilizáveis
services    chamadas HTTP
store       sessão autenticada
hooks       comportamentos reutilizáveis
lib         configuração do Axios
types       contratos TypeScript da API
```

### Estado

TanStack Query gerencia estado remoto: projetos, tarefas, relatório, histórico, loading, erro e invalidação.

Zustand guarda somente token e usuário autenticado. Não duplicar tarefas em estado global reduz inconsistência e código manual.

### Board e drag and drop

O board possui três colunas e usa dnd-kit. Ao soltar uma tarefa, o frontend solicita a transição ao backend.

O frontend não replica regras críticas. O backend permanece como fonte da verdade. Em caso de rejeição, a consulta é invalidada e um toast exibe o `ProblemDetail` retornado.

### Notificação de atribuição

`useAssignmentNotifications` compara tarefas atuais com o conjunto anterior. Quando uma nova tarefa atribuída ao usuário aparece após atualização do board, um toast é exibido.

É uma solução simples, coerente com o requisito desejável. Para tempo real, seria utilizado WebSocket ou SSE.

### Responsividade

O CSS adapta navegação, formulários, cartões e colunas para telas menores. Em dispositivos estreitos, o board mantém rolagem horizontal para preservar a leitura das colunas.

## 14. Testes do frontend

Vitest e Testing Library cobrem o componente `StatusBadge`.

Playwright cobre o fluxo crítico pela interface:

```text
ADMIN cria projeto e tarefa CRITICAL
MEMBER tenta arrastar para DONE
API rejeita
UI exibe toast e mantém a tarefa em TODO
```

## 15. Execução com Docker

O `compose.yml` inicia:

```text
postgres  banco e volume persistente
backend   aplicação Spring Boot
frontend  build React servido por Nginx
```

O Nginx encaminha `/api` para o backend, evitando necessidade de configurar uma URL absoluta no frontend empacotado.

Os healthchecks fazem o frontend aguardar o backend e o backend aguardar o PostgreSQL.

## 16. Integração contínua

O workflow do GitHub Actions possui três jobs:

```text
backend   executa clean verify com Java 21 e Testcontainers
frontend  executa lint, testes unitários e build
e2e       sobe o stack Docker e executa o fluxo crítico no Playwright
```

A separação permite identificar rapidamente se a falha está no backend, na qualidade do frontend ou na integração completa.

## 17. Como apresentar a solução

Uma demonstração objetiva pode seguir esta ordem:

1. abrir o Swagger e mostrar autenticação;
2. entrar no frontend como ADMIN;
3. criar projeto e adicionar MEMBER;
4. criar tarefa CRITICAL atribuída ao membro;
5. entrar como MEMBER e tentar concluir;
6. mostrar o erro 403 padronizado;
7. criar cinco tarefas em andamento e mostrar a rejeição da sexta;
8. aplicar filtros e busca;
9. mostrar o relatório e o histórico;
10. abrir os testes e explicar o fluxo de integração.

## 18. Melhorias futuras

Com mais tempo, seriam priorizados:

```text
refresh token com rotação e revogação
convites por email
soft delete e retenção de auditoria
notificações WebSocket/SSE
testes específicos de concorrência WIP
observabilidade com métricas e tracing
acessibilidade e internacionalização mais completas
análise estática adicional com SonarQube ou equivalente
```
