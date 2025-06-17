
# 🌐 Frontend - Sistema de Checklist IoT

Interface web para visualização, gerenciamento e acompanhamento dos dados de checklists operacionais provenientes do sistema IoT. Desenvolvido com React, TailwindCSS e shadcn/ui, consumindo uma API REST criada em Spring Boot com persistência em PostgreSQL.

---

## 🚀 Funcionalidades

- ✅ Autenticação de usuários
- ✅ Dashboard com indicadores, contagem e gráficos
- ✅ Visualização de checklists feitos por bancada, turno, operador, modelo e período
- ✅ Filtros dinâmicos por data, turno, bancada e usuário
- ✅ Consulta de logs de UID
- ✅ Gerenciamento de usuários e vinculação de UID
- ✅ Visualização detalhada de cada checklist
- ✅ Exclusão de checklists e logs (quando necessário)
- 📈 Gráficos de desempenho dos checklists e dos modelos

---

## 🧠 Tecnologias Utilizadas

- 🏗️ Vite (Build tool)
- ⚛️ React
- 🎨 TailwindCSS (Design e responsividade)
- 💎 shadcn/ui (Componentes acessíveis e bonitos)
- 📊 Recharts (Gráficos)
- 🌐 React Router Dom (Rotas)
- 🗓️ React Day Picker (Calendário e seleção de datas)
- 🔒 Zod + React Hook Form (Validações e formulários)
- 🔗 Consumo de API REST desenvolvida em Java Spring Boot
- 🔥 Dados coletados do Firebase via backend (middleware entre IoT e PostgreSQL)

---

## 🏗️ Estrutura do Projeto

```
src/
├── components/        → Componentes reutilizáveis (UI, tabelas, inputs, cards)
├── pages/             → Páginas da aplicação (Dashboard, Checklists, Logs, Users)
├── services/          → Configurações de API e serviços HTTP
├── hooks/             → Hooks personalizados
├── lib/               → Helpers, configurações e validações (ex.: filtros, zod, date)
├── routes/            → Definição das rotas da aplicação
├── assets/            → Imagens e logos
├── App.tsx            → Arquivo principal da aplicação
└── main.tsx           → Entry point do Vite
```

---

## 🔗 Endpoints Consumidos

- `/users`
- `/users/count`
- `/checklists`
- `/checklists/count`
- `/checklists/count/today`
- `/checklists/last`
- `/uids/logs`
- `/uids`
- 🔒 Todos os endpoints são protegidos por autenticação JWT

---

## 📝 To-Do (Melhorias Futuras)

- 📱 Implementar versão mobile responsiva aprimorada
- 🔔 Sistema de alertas e notificações em tempo real
- 📄 Exportação de relatórios em PDF/Excel
- 🔍 Filtros avançados nos gráficos
- 🔑 Recuperação de senha e melhorias no fluxo de autenticação
