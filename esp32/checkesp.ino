#include <ArduinoJson.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <FirebaseClient.h>
#include <SPI.h>
#include <MFRC522.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <time.h>
#include <vector>


// ==== WI-FI e FIREBASE ====
#define WIFI_SSID "SUA_REDE_WIFI"
#define WIFI_PASSWORD "SENHA_WIFI"

#define Web_API_KEY "API_KEY_DO_FIREBASE"
#define DATABASE_URL "URL_DO_FIREBASE"
#define USER_EMAIL "EMAIL_AUTENTICACAO"
#define USER_PASS "SENHA_AUTENTICACAO"

// ==== OBJETOS FIREBASE ====
UserAuth user_auth(Web_API_KEY, USER_EMAIL, USER_PASS);
FirebaseApp app;
WiFiClientSecure ssl_client;
using AsyncClient = AsyncClientClass;
AsyncClient aClient(ssl_client);
RealtimeDatabase Database;

// ==== RFID ====
#define SS_PIN 5
#define RST_PIN 2
MFRC522 rfid(SS_PIN, RST_PIN);

// ==== DISPLAY ====
LiquidCrystal_I2C lcd(0x27, 16, 2);

// ==== PINOS ====
const byte ledVm = 32;
const byte ledVd = 33;
const byte bOK = 26;
const byte bNOK = 25;
const byte bRBOOT = 27;
const byte buzzer = 14;

// ==== VARIÁVEIS ====
const String bancada = "Bancada_12";
const int debounceDelay = 300;
String nomeOperador = "";
String uidGlobal = "";
String logUIDStatus = "";
unsigned long ultimaLeituraOK = 0;
unsigned long ultimaLeituraNOK = 0;
bool checklistCarregada = false;
bool checklistAtivo = false;
int totalItensChecklist = 0;
byte itemAtual = 0;
std::vector<String> perguntasChecklist;
std::vector<String> checarChecklist;
std::vector<String> respostas;
std::vector<String> chavesChecklist;
String opcoesModelo[3];
String opcoesVoltagem[2];

// ==== SETUP ====
void setup() {
  Serial.begin(115200);
  pinMode(bOK, INPUT_PULLUP);
  pinMode(bNOK, INPUT_PULLUP);
  pinMode(bRBOOT, INPUT_PULLUP);
  pinMode(ledVm, OUTPUT);
  pinMode(ledVd, OUTPUT);
  pinMode(buzzer, OUTPUT);
  delay(2000);

  digitalWrite(ledVm, HIGH);
  digitalWrite(ledVd, LOW);
  lcd.init();
  lcd.backlight();
  lcdTexto("Iniciando...");

  conectarWifi();
  inicializarFirebase();

  SPI.begin();
  rfid.PCD_Init();
  Serial.println("Aproxime o cartão RFID...");
  lcdTexto("Aproxime o cartao...");
}

// ==== LOOP ====
void loop() {
  reiniciar();
  app.loop();

  if (logUIDStatus.length() > 0) {
  registrarLogUID(logUIDStatus);
  logUIDStatus = "";
  }

  if (checklistAtivo) {
    entradaCheckList();
    return;
  }

  if (!app.ready()) {
    return;
  }

  if (!rfid.PICC_IsNewCardPresent() || !rfid.PICC_ReadCardSerial()) return;

  beep(1000, 100);
  uidGlobal = lerUID();
  Serial.println("UID detectado: " + uidGlobal);
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("UID:");
  lcd.setCursor(0, 1);
  lcd.print(uidGlobal);

  String caminho = "/uid_usuarios/" + uidGlobal + "/nome";
  Database.get(aClient, caminho.c_str(), processData, false, "GET_NOME");

  String caminhoChecklist = "/itens";
  Database.get(aClient, caminhoChecklist.c_str(), processData, false, "GET_CHECKLIST");

  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();
}

// ==== FUNÇÕES ====
void conectarWifi() {
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  lcdTexto("Conectando WiFi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(300);
    Serial.print(".");
  }
  Serial.println("\nWi-Fi conectado");
  lcdTexto("Wi-Fi conectado");
  delay(1000);
}

void inicializarFirebase() {
  configTime(-14400, 3600, "a.st1.ntp.br", "b.st1.ntp.br", "pool.ntp.org");

  ssl_client.setInsecure();
  ssl_client.setConnectionTimeout(1000);
  ssl_client.setHandshakeTimeout(5);

  initializeApp(aClient, app, getAuth(user_auth), processData, "authTask");
  app.getApp<RealtimeDatabase>(Database);
  Database.url(DATABASE_URL);
  lcdTexto("Firebase conectado");
  delay(1000);
}

void beep(int frequencia, int duracao) {
  tone(buzzer, frequencia);
  delay(duracao);
  noTone(buzzer);
}

void lcdTexto(String texto) {
  lcd.clear();
  if (texto.length() <= 16) {
    lcd.setCursor(0, 0);
    lcd.print(texto);
  } else if (texto.length() <= 32) {
    lcd.setCursor(0, 0);
    lcd.print(texto.substring(0, 16));
    lcd.setCursor(0, 1);
    lcd.print(texto.substring(16));
  } else {
    for (int i = 0; i <= texto.length() - 16; i++) {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print(texto.substring(i, i + 16));
      delay(300);
    }
  }
}

void reiniciar(){
  static bool bPressionado = false;
  static bool bToque = false;
  static unsigned long tempo = 0;
  static unsigned long pisca = 0;

  if (digitalRead(bRBOOT) == LOW) {
    unsigned long tAtual = millis();
    if (!bPressionado) {
      tempo = tAtual;
      pisca = tAtual;
      bPressionado = true;
      bToque = true;
    } else if (tAtual - pisca >= 250) {
      digitalWrite(ledVm, !digitalRead(ledVm));
      pisca = tAtual;
    }
    if (tAtual - tempo >= 3000) {
      bToque = false;
      Serial.println("Botão pressionado 3s");
      lcdTexto("Reiniciando...");
      digitalWrite(ledVm, LOW);
      delay(300);
      ESP.restart();
    }
  } else {
    if (bPressionado && bToque) {
      if (itemAtual > 0) {
        itemAtual--;
        mostrarItemChecklist();
      }
    }
    bPressionado = false;
    bToque = false;
    digitalWrite(ledVm, HIGH);
  }
}

bool botaoPressionado(int pino, unsigned long &ultimaLeitura) {
  if (digitalRead(pino) == LOW && (millis() - ultimaLeitura) > debounceDelay) {
    ultimaLeitura = millis();
    return true;
  }
  return false;
}

String lerUID() {
  String uid = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    uid += (rfid.uid.uidByte[i] < 0x10 ? "0" : "");
    uid += String(rfid.uid.uidByte[i], HEX);
  }
  uid.toUpperCase();
  return uid;
}

void mostrarItemChecklist() {
  if (itemAtual == 0) {
    lcdTexto("MODELO: " + opcoesModelo[0] + "?");
  } else if (itemAtual == 1) {
    lcdTexto("VOLTAGEM: " + opcoesVoltagem[0] + "?");
  } else {
    lcdTexto(perguntasChecklist[itemAtual]);
  }
}

void avancarChecklist() {
  itemAtual++;
  if (itemAtual < totalItensChecklist) {
    mostrarItemChecklist();
  } else {
    lcd.clear();
    for (int i = 0; i < totalItensChecklist; i++) {
      lcdTexto(checarChecklist[i] + ": " + respostas[i]);
      delay(500);
    }
    lcdTexto("Enviar?");
  }
  delay(500);
}

void enviarDados() {
  lcdTexto("Enviando dados...");
  digitalWrite(ledVd, HIGH);
  delay(1000);

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Wi-Fi desconectado!");
    lcdTexto("Sem Wi-Fi!");
    delay(2000);
    if (itemAtual > 0) {
      itemAtual--;
      mostrarItemChecklist();
    }
    return;
  }

  time_t now;
  struct tm timeinfo;
  int tentativas = 0;
  while (!getLocalTime(&timeinfo) && tentativas < 5) {
    Serial.println("Falha ao obter hora, tentando novamente...");
    delay(1000);
    tentativas++;
  }

  if (tentativas == 5 && !getLocalTime(&timeinfo)) {
    Serial.println("Falha persistente ao obter hora para o timestamp");
    lcdTexto("Erro no tempo!");
    delay(2000);
    if (itemAtual > 0) {
      itemAtual--;
      mostrarItemChecklist();
    }
    return;
  }

  char isoTime[25];
  strftime(isoTime, sizeof(isoTime), "%Y-%m-%dT%H:%M:%SZ", &timeinfo);
  String timestamp = String(isoTime);

  char dataStr[11];
  strftime(dataStr, sizeof(dataStr), "%Y-%m-%d", &timeinfo);
  String dataCompleta = String(dataStr);

  String turno;
  int horaAtual = timeinfo.tm_hour;
  if (horaAtual >= 6 && horaAtual < 13) {
    turno = "Manhã";
  } else if (horaAtual >= 13 && horaAtual < 18) {
    turno = "Tarde";
  } else {
    turno = "Noite";
  }

  if (uidGlobal == "" || nomeOperador == "" || totalItensChecklist == 0) {
    Serial.println("Dados insuficientes para envio.");
    lcdTexto("Dados faltando!");
    delay(2000);
    return;
  }

  String caminhoEnvio = "/checklists/" + uidGlobal + "/" + timestamp;
  StaticJsonDocument<1024> doc;

  doc["bancada"] = bancada;
  doc["data"] = dataCompleta;
  doc["turno"] = turno;
  doc["operador"] = nomeOperador;

  JsonObject itens = doc.createNestedObject("itens_checklist");
  for (int i = 0; i < totalItensChecklist; i++) {
    JsonObject itemObj = itens.createNestedObject(chavesChecklist[i]);
    itemObj["item"] = checarChecklist[i];
    itemObj["resposta"] = respostas[i];
  }

  String payload;
  serializeJsonPretty(doc, Serial);
  Serial.println();
  serializeJson(doc, payload);
  Serial.println("Enviando para: " + caminhoEnvio);

  Database.set<object_t>(aClient, caminhoEnvio.c_str(), object_t(payload.c_str()), processData, "SET_CHECKLIST");
}

void entradaCheckList() {
  if (itemAtual == 0 && checarChecklist[itemAtual] == "MODELO") {
    static byte modeloAtual = 0;
    if (botaoPressionado(bOK, ultimaLeituraOK)) {
      beep(1000, 50);
      respostas[itemAtual] = opcoesModelo[modeloAtual];
      avancarChecklist();
      modeloAtual = 0;
    } else if (botaoPressionado(bNOK, ultimaLeituraNOK)) {
      beep(500, 50);
      modeloAtual = (modeloAtual + 1) % 3;
      lcdTexto("MODELO: " + opcoesModelo[modeloAtual] + "?");
    }
    return;
  }

  if (itemAtual == 1 && checarChecklist[itemAtual] == "VOLTAGEM") {
    static byte voltagemAtual = 0;
    if (botaoPressionado(bOK, ultimaLeituraOK)) {
      beep(1000, 50);
      respostas[itemAtual] = opcoesVoltagem[voltagemAtual];
      avancarChecklist();
      voltagemAtual = 0;
    } else if (botaoPressionado(bNOK, ultimaLeituraNOK)) {
      beep(500, 50);
      voltagemAtual = (voltagemAtual + 1) % 2;
      lcdTexto("VOLTAGEM: " + opcoesVoltagem[voltagemAtual] + "?");
    }
    return;
  }

  if (itemAtual < totalItensChecklist) {
    if (botaoPressionado(bOK, ultimaLeituraOK)) {
      beep(1000, 50);
      respostas[itemAtual] = "OK";
      avancarChecklist();
    } else if (botaoPressionado(bNOK, ultimaLeituraNOK)) {
      beep(500, 50);
      respostas[itemAtual] = "NOK";
      avancarChecklist();
    }
  } else {
    if (botaoPressionado(bOK, ultimaLeituraOK)) {
      beep(1000, 100);
      enviarDados();
    } else if (botaoPressionado(bNOK, ultimaLeituraNOK)) {
      beep(500, 100);
      lcdTexto("Checklist cancelado.");
      delay(1500);
      itemAtual = 0;
      for (int i = 0; i < totalItensChecklist; i++) respostas[i] = "";
      checklistAtivo = false;
      lcdTexto("Aproxime o cartao...");
    }
  }
}

void registrarLogUID(const String& status) {
  time_t now;
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Falha ao obter hora para o log de UID");
    return;
  }
  
  char isoTime[25];
  strftime(isoTime, sizeof(isoTime), "%Y-%m-%dT%H:%M:%SZ", &timeinfo);
  String timestamp = String(isoTime);

  String caminhoLog = "/uid_logs/" + uidGlobal + "_" + timestamp;
  StaticJsonDocument<128> doc;

  doc["uid"] = uidGlobal;
  doc["bancada"] = bancada;
  doc["status"] = status;

  String log;
  serializeJson(doc, log);
  Database.set<object_t>(aClient, caminhoLog.c_str(), object_t(log.c_str()), processData, "SET_LOG_UID");
}

void processData(AsyncResult &aResult) {
  if (aResult.isResult()) {
    if (aResult.uid() == "GET_NOME") {
      String payload = aResult.c_str();
      payload.replace("\"", "");
      if (payload.length() > 0 && payload != "null") {
        logUIDStatus = "Aprovado";
        digitalWrite(ledVd, HIGH);
        nomeOperador = payload;
        String nome;
        int espaco = nomeOperador.indexOf(' ');
        if (espaco != -1) {
          nome = nomeOperador.substring(0, espaco);
        } else {
          nome = nomeOperador;
        }
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Bem-vindo(a):");
        lcd.setCursor(0, 1);
        lcd.print(nome);
        delay(1000);
      } else {
        logUIDStatus = "Desconhecido";
        nomeOperador = "";
        lcdTexto("UID desconhecido");
        for (int i = 0; i < 6; i++) {
          digitalWrite(ledVd, !digitalRead(ledVd));
          delay(250);
        }
        lcdTexto("Aproxime o cartao...");
      }
    } else if (aResult.uid() == "GET_CHECKLIST" && nomeOperador.length() > 0) {
      processChecklistJson(aResult.c_str());
    } else if (aResult.uid() == "SET_CHECKLIST") {
      if (aResult.isError()) {
        Serial.printf("Erro ao enviar Checklist: %s | Código: %d\n", aResult.error().message().c_str(), aResult.error().code());
        lcdTexto("Erro no envio!");
        delay(3000);
        lcdTexto("Aproxime o cartao...");
      } else {
        for (int i = 0; i < 6; i++) {
          digitalWrite(ledVd, !digitalRead(ledVd));
          delay(250);
        }
        Serial.println("Checklist enviado com sucesso!");
        lcdTexto("Dados enviados!");
        delay(2000);
        digitalWrite(ledVd, LOW);
        itemAtual = 0;
        for (int i = 0; i < totalItensChecklist; i++) respostas[i] = "";
        checklistAtivo = false;
        lcdTexto("Aproxime o cartao...");
      }
    }
  } else {
    Serial.printf("Erro geral de conexão: %s | Código: %d\n", aResult.error().message().c_str(), aResult.error().code());
    lcdTexto("Erro de conexao");
    delay(3000);
    lcdTexto("Aproxime o cartao...");
  }
}

void processChecklistJson(const String& jsonStr) {
  StaticJsonDocument<2048> doc;
  DeserializationError error = deserializeJson(doc, jsonStr);

  if (error) {
    Serial.print("Erro ao parsear o JSON do checklist: ");
    Serial.println(error.c_str());
    lcdTexto("Checklist invalido");
    delay(2000);
    return;
  }

  JsonObject itens = doc.as<JsonObject>();
  if (itens.isNull()) { 
    lcdTexto("Checklist ausente");
    delay(2000);
    return;
  }

  int quantidade = itens.size();

  perguntasChecklist.clear();
  checarChecklist.clear();
  respostas.clear();
  chavesChecklist.clear();

  perguntasChecklist.resize(quantidade);
  checarChecklist.resize(quantidade);
  respostas.resize(quantidade);
  chavesChecklist.resize(quantidade);

  for (JsonPair kv : itens) {
    JsonObject dados = kv.value().as<JsonObject>();
    int indice = dados["indice"] | -1;

    if (indice < 0 || indice >= quantidade) {
      Serial.printf("Índice inválido em %s\n", kv.key().c_str());
      continue;
    }

    perguntasChecklist[indice] = dados["pergunta"] | "";
    checarChecklist[indice] = dados["chave_envio"] | "";
    chavesChecklist[indice] = kv.key().c_str();

    String chave = kv.key().c_str();
    if (chave == "item_modelo") {
      JsonArray opcoes = dados["opcoes"].as<JsonArray>();
      int j = 0;
      for (JsonVariant v : opcoes) {
        if (j < 3) opcoesModelo[j++] = v.as<String>();
      }
    } else if (chave == "item_voltagem") {
      JsonArray opcoes = dados["opcoes"].as<JsonArray>();
      int j = 0;
      for (JsonVariant v : opcoes) {
        if (j < 2) opcoesVoltagem[j++] = v.as<String>();
      }
    }

    Serial.printf("Item [%s] carregado no índice %d: %s\n", kv.key().c_str(), indice, perguntasChecklist[indice].c_str());
  }

  totalItensChecklist = quantidade;
  checklistCarregada = true;
  checklistAtivo = true;
  itemAtual = 0;

  digitalWrite(ledVd, LOW);
  lcdTexto("Checklist pronto");
  delay(1500);
  mostrarItemChecklist();
}