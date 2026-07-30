# Guia para explicar o projeto na entrevista

## Resumo de abertura

> Construí um monólito em camadas com Spring Boot 3 e React. Priorizei autorização, regras concorrentes e contratos consistentes. O backend é a fonte de verdade; o frontend apenas representa o estado e solicita transições. Usei PostgreSQL em todos os ambientes relevantes, Flyway para schema e Testcontainers no fluxo crítico.

## Decisões que merecem destaque

### Por que arquitetura em camadas?

É suficiente para o tamanho do domínio, conhecida por equipes Java e separa HTTP, casos de uso e persistência sem criar abstrações artificiais. Caso o domínio crescesse, regras como status e autorização poderiam virar policies ou módulos independentes.

### Por que DTO e mapper manual?

Evita expor entidades JPA e deixa o contrato explícito. O domínio é pequeno; MapStruct seria aceitável, mas adicionaria geração de código sem necessidade real.

### Como evitou a sexta tarefa em andamento em concorrência?

A aplicação bloqueia a linha do responsável com `PESSIMISTIC_WRITE` e só depois conta tarefas `IN_PROGRESS`. O lock é por usuário, então não serializa o sistema inteiro.

### Por que 422 para regra de negócio?

O JSON é válido e a requisição foi entendida, mas o estado solicitado viola uma regra do domínio. Validação estrutural usa 400; conflito de unicidade usa 409; falta de autorização usa 403.

### Como a busca considera performance?

É paginada, sempre restringida por projeto e possui índices GIN com `pg_trgm` sobre `LOWER(title)` e `LOWER(description)`. Para escala muito maior, avaliaria Full Text Search ou motor dedicado.

### Como funciona o cache?

Somente o relatório agregado é cacheado, porque possui baixa cardinalidade. Escritas de tarefa invalidam a chave do projeto. A checagem de acesso ocorre fora do método cacheado, evitando vazamento em cache hit.

### Por que TanStack Query e Zustand?

TanStack Query gerencia dados remotos e invalidações; Zustand guarda apenas sessão e identidade. Isso evita duplicar tarefas e projetos em um estado global manual.

### Trade-offs assumidos

- JWT sem refresh token para manter o escopo controlado.
- Usuário precisa estar cadastrado antes de entrar em um projeto.
- Auditoria é removida junto com a tarefa; soft delete preservaria histórico, mas aumentaria escopo.
- Notificação de atribuição é baseada em atualização do board, não em canal em tempo real.
