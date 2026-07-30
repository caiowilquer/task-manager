# Matriz de atendimento dos requisitos

| Requisito | Implementação |
|---|---|
| Login email/senha JWT | `AuthController`, `AuthService`, `JwtService` |
| Perfis ADMIN e MEMBER | `UserRole`, Spring Security e regras de service |
| Acesso somente aos projetos do usuário | `ProjectAccessService` e query de projetos acessíveis |
| CRUD de projetos | `ProjectController` e `ProjectService` |
| Dono e N membros | `projects.owner_id` e `project_members` |
| CRUD de tarefas | `TaskController` e `TaskService` |
| Status, prioridade, datas, deadline e responsável | Entidade `Task` e DTOs |
| Responsável membro do projeto | `requireProjectMember` |
| DONE não volta para TODO | `validateTransition` |
| CRITICAL fechada pelo ADMIN do projeto | `updateStatus` valida role e owner |
| Limite de cinco IN_PROGRESS | lock pessimista + contagem no `TaskService` |
| Filtros e range de datas | `TaskFilterParams` e Criteria API |
| Ordenação por prioridade/criação/deadline | `TaskRepositoryImpl` |
| Busca título/descrição com performance | endpoint `/search`, paginação e GIN/pg_trgm |
| Relatório por status/prioridade | endpoint `/summary` com agregação SQL |
| Swagger/OpenAPI | Springdoc em `/swagger-ui.html` |
| Paginação com metadata | `PageResponse` |
| Histórico de alterações | `task_audits` e endpoint `/history` |
| Cache | Caffeine no relatório e invalidação em escritas |
| Testes unitários services | JUnit 5, Mockito e AssertJ |
| Teste integração SpringBootTest | `CriticalTaskFlowIT` com Testcontainers |
| ProblemDetail | `GlobalExceptionHandler` e handlers de segurança |
| Bean Validation | DTOs e parâmetros dos controllers |
| React responsivo | aplicação em `frontend/` |
| Estado justificado | TanStack Query + Zustand |
| Drag and drop | dnd-kit no board |
| Toast de atribuição | `useAssignmentNotifications` |
| Teste unitário componente | `StatusBadge.test.tsx` |
| E2E fluxo crítico | Playwright em `frontend/e2e` |
| README e trade-offs | README e documentos em `docs/` |
| Docker | Dockerfiles e `compose.yml` |
| Integração contínua | GitHub Actions valida backend, frontend e E2E |
