# 🗳️ Sistema de Votação Cooperativa

<!--- > 🌐 **Demo ao vivo:** [Frontend](https://desafio-votacao-fullstack-two.vercel.app) | [API Swagger](https://desafio-votacao-fullstack-production.up.railway.app/swagger-ui.html) --->

API REST + Frontend React para gerenciar sessões de votação em assembleias cooperativas.

---

## 🛠️ Stack

| Camada    | Tecnologia                                |
|-----------|-------------------------------------------|
| Backend   | Java 17, Spring Boot 3.2, Spring Data JPA |
| Banco     | H2 (dev) / PostgreSQL (prod)              |
| Frontend  | React 18, TypeScript, Vite                |
| Docs API  | SpringDoc OpenAPI (Swagger UI)            |
| Container | Docker + Docker Compose                   |
| Testes BE | JUnit 5, Mockito, Gatling 3.10            |
| Testes FE | Vitest, Testing Library, MSW              |

---

## ✅ Pré-requisitos

| Ferramenta | Versão mínima |
|------------|---------------|
| Java       | 17            |
| Maven      | 3.8           |
| Node       | 18            |
| Docker     | 20 (opcional) |

---

## 🚀 Como executar

### 💻 Sem Docker (desenvolvimento local)

```bash
# 1. Backend
cd backend
mvn spring-boot:run
# (Ou apenas "Run" na main class pelo Intellij)
# Acesse:  http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# H2:      http://localhost:8080/h2-console

# 2. Frontend (outro terminal)
cd frontend
npm install
#(Caso não funcione, use:)
npm install --ignore-scripts # <- Caso esteja no Windows use essa flag direto.
#(Caso não funcione, use:)
npx vite --debug
#(Caso não funcione, use:)
npx vite --config vite.config.js
# Acesse: http://localhost:3000
```

### 🐳 Com Docker — perfil dev (H2, mais simples)

```bash
docker-compose up --build
# Frontend: http://localhost
# Backend:  http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
```

### 🐳 Com Docker — perfil prod (PostgreSQL)

```bash
docker-compose --profile prod up --build
```

---

## 📸 Screenshots

### Lista de Pautas - Teste de Performance.
<img width="1919" height="942" alt="image" src="https://github.com/user-attachments/assets/096dfa95-3b9f-4e6b-ac5a-aab33cd12be5" />
<img width="1914" height="944" alt="image" src="https://github.com/user-attachments/assets/557f187b-695e-4cdb-8cc1-c6800bf7ab18" />


### Tela em Branco para iniciar novas Pautas.
<img width="1914" height="942" alt="image" src="https://github.com/user-attachments/assets/12fcc1a3-f1e2-474e-89e6-2679ee530607" />

### Criação de uma nova Pauta.
<img width="1919" height="944" alt="image" src="https://github.com/user-attachments/assets/c316f4eb-d613-44b2-85a2-fecbdda822f2" />

### Pauta Criada, Filtros: Todas, Abertas, Criadas, Fechadas.
<img width="1910" height="943" alt="image" src="https://github.com/user-attachments/assets/d11d5cf2-ac2e-4388-a052-69fe177002c1" />

### Abertura de uma nova seção de votos.
<img width="1919" height="944" alt="image" src="https://github.com/user-attachments/assets/5af37709-db38-4d2b-b3e2-b2a1b1724b54" />

### Definição do tempo de votação.
<img width="1916" height="943" alt="image" src="https://github.com/user-attachments/assets/48863d66-61a7-4783-b380-0ef838901419" />

### Seção Aberta pelo tempo estipulado.
<img width="1917" height="953" alt="image" src="https://github.com/user-attachments/assets/a92ec730-a852-4263-ab95-d9c78ae81b29" />

### Registrando meu voto - Sim ou Não.
<img width="1919" height="945" alt="image" src="https://github.com/user-attachments/assets/567b0978-2c53-430a-902d-3c2c815e9b0a" />

### Votção em tempo real - 9 votos, 7 Sims, 2 nãos.
<img width="1911" height="945" alt="image" src="https://github.com/user-attachments/assets/02eafc08-c7d9-42ec-8747-c4139862d4b1" />

### Pauta Finalizada - Resultado: Aprovada.
<img width="1910" height="948" alt="image" src="https://github.com/user-attachments/assets/270d2674-8194-4fce-b69e-cc943938e894" />
<img width="1918" height="944" alt="image" src="https://github.com/user-attachments/assets/30752d17-7a2c-4677-82cf-225c37788d8c" />





---


## 💾 Persistência dos dados

| Modo             | Onde ficam os dados           | Sobrevive ao restart? |
|------------------|-------------------------------|-----------------------|
| Sem Docker       | `backend/data/votingdb.mv.db` | ✅ Sim                |
| Docker dev (H2)  | Volume Docker `h2-data`       | ✅ Sim                |
| Docker prod (PG) | Volume Docker `pg-data`       | ✅ Sim                |

> `docker-compose down` — mantém os dados
> `docker-compose down -v` — apaga os volumes

---

## 🧪 Testes

### 🔬 Backend — unitários e integração

```bash
cd backend
mvn test
```

Cobertura:
- `AgendaServiceTest` — 5 testes unitários de regras de pauta
- `VoteServiceTest` — 4 testes unitários de regras de votação
- `AgendaControllerIT` — 15 testes de integração (ponta a ponta com H2)

### ⚛️ Frontend — componentes e API

```bash
cd frontend

# Instalar dependências (primeira vez)
npm install --ignore-scripts

# Rodar os testes
npx vitest run --config vitest.config.ts
```

Cobertura:
- `StatusBadge.test` — 3 testes de renderização
- `CountdownTimer.test` — 4 testes incluindo timer fake
- `api.test` — 7 testes do cliente HTTP com MSW
- `HomePage.test` — 6 testes de comportamento e filtros

### ⚡ Performance — Gatling

```bash
cd backend

# 1. Sobe o backend com profile perf (H2 in-memory, pool otimizado, CPF facade desabilitada)
# No IntelliJ: Edit Configurations → Active profiles → perf
# Ou no terminal:
mvn spring-boot:run -Dspring-boot.run.profiles=perf

# 2. Em outro terminal, rode os testes de carga
mvn gatling:test                   # smoke — 10 usuários, 30s (padrão)
mvn gatling:test -DSCENARIO=load   # load  — 500 usuários, ~5min
mvn gatling:test -DSCENARIO=stress # stress — 2.000 usuários, ~5min

# 3. Abra o relatório HTML
# target/gatling/votingsimulation-<timestamp>/index.html
```

---

## 📡 Endpoints da API (v1)

| Método | Endpoint                                | Descrição                      |
|--------|-----------------------------------------|--------------------------------|
| POST   | `/api/v1/agendas`                       | Criar pauta                    |
| GET    | `/api/v1/agendas`                       | Listar todas as pautas         |
| GET    | `/api/v1/agendas/{id}`                  | Buscar pauta por ID            |
| POST   | `/api/v1/agendas/{id}/sessions`         | Abrir sessão de votação        |
| POST   | `/api/v1/agendas/{id}/votes`            | Registrar voto                 |
| GET    | `/api/v1/agendas/{id}/results`          | Resultado da votação           |
| GET    | `/api/v1/users/{cpf}/voting-eligibility`| Validar CPF (Bônus 1)         |

### Exemplos de uso

```bash
# Criar pauta
curl -X POST http://localhost:8080/api/v1/agendas \
  -H "Content-Type: application/json" \
  -d '{"title": "Aprovação do orçamento 2025", "description": "Votação do orçamento anual"}'

# Abrir sessão (5 minutos)
curl -X POST http://localhost:8080/api/v1/agendas/1/sessions \
  -H "Content-Type: application/json" \
  -d '{"durationMinutes": 5}'

# Abrir sessão (1 minuto — default)
curl -X POST http://localhost:8080/api/v1/agendas/1/sessions \
  -H "Content-Type: application/json" \
  -d '{}'

# Votar
curl -X POST http://localhost:8080/api/v1/agendas/1/votes \
  -H "Content-Type: application/json" \
  -d '{"cpf": "52998224725", "choice": "YES"}'

# Resultado
curl http://localhost:8080/api/v1/agendas/1/results
```

---

## 🔢 Versionamento da API (Bônus 3)

Estratégia adotada: **URI Path Versioning** (`/api/v1/...`)

**Por quê?**
- Visível e explícito — qualquer desenvolvedor entende sem documentação
- Fácil de rotear no nginx/gateway por prefixo
- Compatível com cache HTTP (URLs diferentes = recursos diferentes)
- Sem conflito com headers Accept

Para adicionar v2 sem quebrar v1: criar `AgendaControllerV2` em `/api/v2/agendas`, mantendo v1 funcionando em paralelo durante a migração.

---

## 🏗️ Decisões de design

- **H2 modo arquivo** por padrão: zero dependências externas para rodar localmente, dados persistidos entre restarts
- **Spring profiles** (dev/prod/perf): mesmo código, configurações diferentes via variável de ambiente
- **Unique constraint** na tabela `votes` (agenda_id + associate_cpf): safety net contra voto duplo por race condition, independente da validação na aplicação
- **DataIntegrityViolationException** tratada no GlobalExceptionHandler: garante 409 amigável mesmo sob alta concorrência
- **@Scheduled** para fechar sessões: simplicidade — não requer fila de mensagens para o escopo do exercício
- **CPF Facade aleatória**: implementa a spec do Bônus 1 com validação estrutural real (algoritmo dos dígitos verificadores) + aleatoriedade no serviço externo fake

---

## 🎯 Tarefas Bônus implementadas

- **Bônus 1** ✅ — `CpfValidationFacade`: valida estrutura do CPF + retorna `ABLE_TO_VOTE`/`UNABLE_TO_VOTE` aleatoriamente, 404 para CPF inválido
- **Bônus 2** ✅ — Gatling com 3 cenários (smoke/load/stress), índices de banco, cache de resultados, HikariCP pool
- **Bônus 3** ✅ — URI path versioning (`/api/v1/`), justificativa documentada acima

---

## ⚡ Otimizações de performance

**Banco de dados:**
- Índice composto `(agenda_id, associate_cpf)` — query de checagem de voto duplicado
- Índice `(agenda_id, choice)` — COUNT queries de contagem
- Índice em `end_time` — scheduler de sessões expiradas
- Batch insert no Hibernate (`batch_size: 50`)

**Aplicação:**
- `existsByAgendaIdAndAssociateCpf()` gera `SELECT COUNT(1)` sem carregar entidade
- Cache de resultados (`@Cacheable`) com eviction automático a cada novo voto
- HikariCP: `maximum-pool-size: 50`
- Tomcat: 200 threads de worker
