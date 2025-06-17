# CheckESP

Sistema de Checklist Digital com IoT, Backend e Frontend.

## 🚀 Sobre o Projeto

O **CheckESP** é uma solução completa para controle de checklists operacionais, integrando:

- 📲 **Dispositivos IoT com ESP32** para coleta de dados em campo.
- 🖥️ **Frontend Web** para monitoramento, gestão e visualização dos dados.
- 🔙 **Backend** para sincronização, persistência e processamento dos dados.

---

## 🏗️ Estrutura do Projeto

```
CheckESP/
├── backend/   → API em Java Spring Boot + PostgreSQL + Firebase
├── frontend/  → Interface Web (React + Vite + TailwindCSS)
├── esp32/     → Firmware do ESP32 (C++ via PlatformIO ou Arduino IDE)
└── README.md  → Este arquivo
```

---

## 💡 Objetivo

Digitalizar processos de checklist em ambientes industriais, oferecendo:

- 🚀 Coleta rápida de dados via dispositivos IoT.
- 📊 Visualização em tempo real de checklists.
- ✅ Histórico, relatórios e gestão de usuários, operadores e equipamentos.
