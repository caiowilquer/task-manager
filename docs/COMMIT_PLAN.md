# Plano de commits incrementais

O projeto extraído contém o estado final. Os comandos abaixo montam um histórico limpo por grupos de responsabilidade. Antes de cada commit, use `git status` e `git diff --staged`.

## 0. Repositório e planejamento inicial

```bash
git init
git branch -M main
git add .gitignore .gitattributes docs/ROADMAP.md
git commit -m "chore: initialize repository and development roadmap"
```

## 1. Fundação Spring Boot e padrões compartilhados

```bash
git add pom.xml mvnw mvnw.cmd .mvn .env.example
git add src/main/java/com/caiowilquer/taskmanager/TaskManagerApplication.java
git add src/main/resources/application.yml src/main/resources/application-local.yml src/main/resources/application-docker.yml
git add src/test/resources/application-test.yml
git add src/main/resources/db/migration/V1__create_users.sql
git add src/main/java/com/caiowilquer/taskmanager/entity/BaseEntity.java
git add src/main/java/com/caiowilquer/taskmanager/entity/AuditableEntity.java
git add src/main/java/com/caiowilquer/taskmanager/dto/common
git add src/main/java/com/caiowilquer/taskmanager/exception
git add src/main/java/com/caiowilquer/taskmanager/config/JpaConfig.java
git add src/main/java/com/caiowilquer/taskmanager/config/CacheConfig.java
git add src/main/java/com/caiowilquer/taskmanager/config/OpenApiConfig.java
git add src/main/java/com/caiowilquer/taskmanager/config/JwtProperties.java
git add src/main/java/com/caiowilquer/taskmanager/config/CorsProperties.java
git add src/main/java/com/caiowilquer/taskmanager/config/BootstrapProperties.java
git commit -m "chore: initialize Spring Boot PostgreSQL and API foundation"
```

## 2. Usuários, segurança e JWT

```bash
git add src/main/java/com/caiowilquer/taskmanager/entity/User.java
git add src/main/java/com/caiowilquer/taskmanager/entity/enums/UserRole.java
git add src/main/java/com/caiowilquer/taskmanager/dto/auth
git add src/main/java/com/caiowilquer/taskmanager/dto/user
git add src/main/java/com/caiowilquer/taskmanager/repository/UserRepository.java
git add src/main/java/com/caiowilquer/taskmanager/mapper/UserMapper.java
git add src/main/java/com/caiowilquer/taskmanager/security
git add src/main/java/com/caiowilquer/taskmanager/service/AuthService.java
git add src/main/java/com/caiowilquer/taskmanager/service/CurrentUserService.java
git add src/main/java/com/caiowilquer/taskmanager/controller/AuthController.java
git add src/main/java/com/caiowilquer/taskmanager/config/SecurityConfig.java
git add src/main/java/com/caiowilquer/taskmanager/config/DataInitializer.java
git commit -m "feat: implement JWT authentication and user registration"
```

## 3. Projetos, tarefas e regras de negócio

Projetos e tarefas entram juntos porque a remoção de membros consulta tarefas atribuídas. O agrupamento mantém o commit compilável e evita uma versão intermediária artificial.

```bash
git add src/main/resources/db/migration/V2__create_projects.sql
git add src/main/resources/db/migration/V3__create_tasks_and_audit.sql
git add src/main/java/com/caiowilquer/taskmanager/entity/Project.java
git add src/main/java/com/caiowilquer/taskmanager/entity/ProjectMember.java
git add src/main/java/com/caiowilquer/taskmanager/entity/Task.java
git add src/main/java/com/caiowilquer/taskmanager/entity/TaskAudit.java
git add src/main/java/com/caiowilquer/taskmanager/entity/enums/TaskStatus.java
git add src/main/java/com/caiowilquer/taskmanager/entity/enums/TaskPriority.java
git add src/main/java/com/caiowilquer/taskmanager/entity/enums/AuditAction.java
git add src/main/java/com/caiowilquer/taskmanager/dto/project
git add src/main/java/com/caiowilquer/taskmanager/dto/task
git add src/main/java/com/caiowilquer/taskmanager/repository/ProjectRepository.java
git add src/main/java/com/caiowilquer/taskmanager/repository/ProjectMemberRepository.java
git add src/main/java/com/caiowilquer/taskmanager/repository/TaskRepository.java
git add src/main/java/com/caiowilquer/taskmanager/repository/TaskAuditRepository.java
git add src/main/java/com/caiowilquer/taskmanager/repository/custom
git add src/main/java/com/caiowilquer/taskmanager/repository/projection
git add src/main/java/com/caiowilquer/taskmanager/mapper/ProjectMapper.java
git add src/main/java/com/caiowilquer/taskmanager/mapper/TaskMapper.java
git add src/main/java/com/caiowilquer/taskmanager/service/ProjectAccessService.java
git add src/main/java/com/caiowilquer/taskmanager/service/ProjectService.java
git add src/main/java/com/caiowilquer/taskmanager/service/TaskService.java
git add src/main/java/com/caiowilquer/taskmanager/service/TaskAuditService.java
git add src/main/java/com/caiowilquer/taskmanager/service/TaskSummaryService.java
git add src/main/java/com/caiowilquer/taskmanager/controller/ProjectController.java
git add src/main/java/com/caiowilquer/taskmanager/controller/TaskController.java
git commit -m "feat: implement projects tasks business rules search report and audit"
```

## 4. Testes do backend

```bash
./mvnw test
git add src/test/java
git commit -m "test: cover services and critical task integration flow"
```

O teste `CriticalTaskFlowIT` é executado pelo Failsafe durante `verify` e usa PostgreSQL com Testcontainers:

```bash
./mvnw clean verify
```

## 5. Frontend React

```bash
git add frontend/package.json frontend/tsconfig*.json frontend/vite.config.ts frontend/eslint.config.js
git add frontend/index.html frontend/.env.example
git add frontend/src
git reset frontend/src/components/StatusBadge.test.tsx frontend/src/test
git add frontend/Dockerfile frontend/nginx.conf
git commit -m "feat: implement responsive React task board with drag and drop"
```

## 6. Testes do frontend

```bash
git add frontend/src/components/StatusBadge.test.tsx frontend/src/test
git add frontend/playwright.config.ts frontend/e2e
git commit -m "test: add React component and end-to-end critical flow tests"
```

## 7. Containers, CI, scripts e coleção Postman

```bash
git add Dockerfile compose.yml .dockerignore frontend/.dockerignore scripts postman .github
git commit -m "chore: add containers CI and delivery assets"
```

## 8. Documentação final

```bash
git add README.md docs
git commit -m "docs: document setup architecture tradeoffs and API usage"
```

## Conferência final

```bash
git status
git log --oneline --decorate
./mvnw clean verify
cd frontend
npm install
npm run lint
npm run test
npm run build
```
