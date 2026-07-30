# Contratos da API

A documentação executável está no Swagger. Todos os endpoints privados exigem:

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

## Login

```http
POST /api/v1/auth/login

{"email":"admin@taskmanager.local","password":"Admin@123"}
```

## Criar projeto

```http
POST /api/v1/projects

{"name":"Plataforma de pagamentos","description":"Backlog da equipe"}
```

## Adicionar membro

```http
POST /api/v1/projects/{projectId}/members

{"email":"member@taskmanager.local"}
```

## Criar tarefa

```http
POST /api/v1/projects/{projectId}/tasks

{
  "title":"Implementar autenticação",
  "description":"Adicionar login JWT",
  "priority":"CRITICAL",
  "deadline":"2026-08-10",
  "assigneeId":"<uuid>"
}
```

## Listar com filtros

```http
GET /api/v1/projects/{projectId}/tasks?status=IN_PROGRESS&priority=HIGH&assigneeId=<uuid>&createdFrom=2026-07-01T00:00:00Z&createdTo=2026-07-31T23:59:59Z&sortBy=DEADLINE&direction=ASC&page=0&size=20
```

## Erro padronizado

```json
{
  "type": "https://task-manager.local/problems/422",
  "title": "Business rule violation",
  "status": 422,
  "detail": "Limite de tarefas atingido: o responsável já possui 5 tarefas IN_PROGRESS.",
  "instance": "/api/v1/projects/.../tasks/.../status",
  "timestamp": "2026-07-29T23:00:00Z"
}
```
