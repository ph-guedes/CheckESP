# CheckESP - Backend

API responsável pela comunicação entre o frontend, o banco de dados (PostgreSQL) e o Firebase.

## 🚀 Funcionalidades

- 🗂️ Persistência de dados do Firebase no PostgreSQL.
- 🔥 Leitura e escrita de dados no Firebase Realtime Database.
- 👥 Gestão de usuários, checklists, UIDs e logs.
- 📊 Endpoints para dashboards e relatórios.
- 🔐 Autenticação via JWT.

---

## 🛠️ Tecnologias

- ☕ Java 17
- 🧠 Spring Boot
- 🐘 PostgreSQL
- 🔥 Firebase Realtime Database (via SDK Admin)
- 🌐 Spring Security + JWT
- 🛠️ Maven

---

## 🔗 Endpoints Principais

- `/auth/login` → Login
- `/users` → Gestão de usuários
- `/uids` → Gestão de UIDs vinculados
- `/checklists` → Consultas e filtros de checklists
- `/dashboard` → Dados agregados para o frontend

---

## 🗄️ Banco de Dados

- Banco utilizado: **PostgreSQL**
- As tabelas são geradas automaticamente pelo Hibernate na primeira execução.

---

## 📜 Documentação da API

A documentação dos endpoints pode ser acessada via Swagger:  
👉 `http://localhost:8081/api/swagger-ui/index.html`  
*(se Swagger estiver habilitado)*

---

## 🏗️ Futuras melhorias

- 📈 Monitoramento com Prometheus e Grafana.
- 🔍 Validações adicionais.
- 📄 Logs mais detalhados.
