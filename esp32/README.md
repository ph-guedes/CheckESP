
# 📋 Sistema de Checklist IoT com ESP32, Firebase e RFID

Projeto desenvolvido em Arduino para ESP32, utilizando RFID, display LCD I2C, botões físicos e integração com Firebase Realtime Database. O sistema permite que operadores realizem checklists diretamente na bancada, com identificação por cartão RFID e registro automático dos dados na nuvem.

---

## 🚀 Funcionalidades

- ✅ Leitura de cartão RFID para identificação do operador
- ✅ Exibição sequencial das perguntas do checklist no display LCD
- ✅ Resposta via botões físicos (OK/NOK)
- ✅ Seleção dinâmica de opções (modelo e voltagem)
- ✅ Envio dos dados para o Firebase Realtime Database
- ✅ Registro de logs de UID
- ✅ Feedback visual (LEDs) e sonoro (buzzer)
- ✅ Reinício manual e navegação reversa no checklist via botão dedicado

---

## 🛠️ Hardware Utilizado

- 🔸 ESP32
- 🔸 Leitor RFID RC522
- 🔸 Display LCD 16x2 I2C
- 🔸 2 LEDs (verde e vermelho)
- 🔸 3 Botões:
  - ✔️ OK (confirma)
  - ❌ NOK (nega/avança)
  - 🔄 Reset (reinício ou voltar item)
- 🔸 Buzzer (ativo ou passivo)
- 🔸 Protoboard e jumpers

---

## 🔌 Conexões

| Componente | ESP32 Pinagem |
|-------------|----------------|
| RFID SDA    | GPIO 5         |
| RFID RST    | GPIO 2         |
| LED Verde   | GPIO 33        |
| LED Vermelho| GPIO 32        |
| Botão OK    | GPIO 26        |
| Botão NOK   | GPIO 25        |
| Reset       | GPIO 27        |
| Buzzer      | GPIO 14        |
| LCD I2C     | SDA (21) / SCL (22) |

---

## 🔗 Bibliotecas Utilizadas

- [`FirebaseClient`](https://github.com/mobizt/FirebaseClient)
- [`MFRC522`](https://github.com/miguelbalboa/rfid)
- [`LiquidCrystal_I2C`](https://github.com/johnrickman/LiquidCrystal_I2C)
- [`ArduinoJson`](https://arduinojson.org/)
- WiFi e WiFiClientSecure (nativas do ESP32)

---

## ☁️ Configurações Necessárias

### 🔑 Informações do Wi-Fi e Firebase:

```cpp
#define WIFI_SSID "SUA_REDE_WIFI"
#define WIFI_PASSWORD "SENHA_WIFI"

#define Web_API_KEY "API_KEY_DO_FIREBASE"
#define DATABASE_URL "URL_DO_FIREBASE"
#define USER_EMAIL "EMAIL_AUTENTICACAO"
#define USER_PASS "SENHA_AUTENTICACAO"
```

### 🌳 Estrutura no Firebase Realtime Database:

- `/uid_usuarios/{UID}/nome` ➝ Nome do operador
- `/itens` ➝ Lista de itens do checklist
- `/checklists/{UID}/{timestamp}` ➝ Checklists enviados
- `/uid_logs` ➝ Logs de tentativas de autenticação via UID

---

## 🏗️ Funcionamento do Fluxo

1️⃣ O operador aproxima seu cartão RFID  
2️⃣ O sistema verifica se o UID está cadastrado no Firebase  
3️⃣ Carrega o checklist do banco de dados  
4️⃣ O display exibe cada item e o operador responde com os botões ✔️ (OK) ou ❌ (NOK)  
5️⃣ Após responder tudo, confirma o envio dos dados  
6️⃣ Os dados são enviados ao Firebase com data, hora, operador e resultados do checklist  

---

## 💾 Como Usar

1. Instale as bibliotecas necessárias no Arduino IDE  
2. Configure as credenciais de Wi-Fi e Firebase no código  
3. Faça o upload para o ESP32  
4. Monte o hardware conforme o esquema de pinos  
5. Execute!  

---

## ⚠️ Observações

- O código possui debouncing para evitar múltiplos cliques acidentais.  
- O botão 🔄 Reset tem dupla função:  
  - Pressão rápida ➝ Voltar item anterior  
  - Pressão longa (3 segundos) ➝ Reiniciar o dispositivo  

---

## 🔮 Melhorias Futuras

- 🚀 Migrar comunicação para MQTT para reduzir latência  
- 📲 Criar uma versão mobile do checklist  
- 🔔 Adicionar sistema de notificações (ex.: falha no checklist)  
- 🔍 Implementar análise de dados e relatórios automáticos  
