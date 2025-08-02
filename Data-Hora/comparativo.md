Abaixo está uma comparação direta entre `java.sql.Date` e `java.util.Date`, focando nas diferenças, usos, armadilhas e conversões entre eles:

---

## 📌 Tabela comparativa: `java.sql.Date` vs `java.util.Date`

| Aspecto                             | `java.util.Date`                                    | `java.sql.Date`                               |
| ----------------------------------- | --------------------------------------------------- | --------------------------------------------- |
| **Pacote**                          | `java.util`                                         | `java.sql`                                    |
| **Finalidade**                      | Representar data e hora genérica                    | Representar **apenas data (sem hora)** em SQL |
| **Herança**                         | `Object` → `java.util.Date`                         | **Estende** `java.util.Date`                  |
| **Campos armazenados**              | Data e hora                                         | Internamente possui hora, mas **ignorada**    |
| **Para JDBC**                       | Pode ser usado, mas não recomendado direto          | Feito especificamente para `DATE` do SQL      |
| **Formato ao imprimir**             | Exibe data e hora                                   | Exibe **apenas a data**                       |
| **Construtores**                    | Vários, incluindo com milissegundos                 | Aceita apenas timestamp em milissegundos      |
| **Compatibilidade com `LocalDate`** | Precisa converter manualmente                       | Usa `.toLocalDate()` desde Java 8             |
| **Conversão para SQL**              | Precisa ser convertido com `new java.sql.Date(...)` | Já é aceito como argumento em JDBC            |
| **Obsoleto desde Java 8?**          | Sim, substituído por `Instant`, `LocalDate` etc     | Também desaconselhado, exceto em drivers JDBC |

---

## 🔁 Exemplo de conversões

### De `java.util.Date` para `java.sql.Date`

```java
java.util.Date utilDate = new Date();
java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
```

### De `java.sql.Date` para `LocalDate` (Java 8+)

```java
LocalDate localDate = sqlDate.toLocalDate();
```

### De `LocalDate` para `java.sql.Date`

```java
java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
```

---

## ⚠️ Armadilhas

| Situação                                      | Problema                                                             |
| --------------------------------------------- | -------------------------------------------------------------------- |
| Usar `java.sql.Date` achando que não tem hora | Internamente **tem hora (herda de util.Date)**, mas JDBC ignora      |
| Comparar `util.Date` com `sql.Date`           | Pode gerar inconsistência: tipos diferentes e horas podem interferir |
| Armazenar `java.util.Date` diretamente        | Pode perder precisão ou causar problemas de timezone no JDBC         |

---

## ✅ Boas práticas

* Evite usar `java.util.Date` e `java.sql.Date` em projetos novos.
* Use `java.time.LocalDate`, `LocalDateTime` ou `Instant` em Java 8+.
* Converta para `java.sql.Date` **somente ao enviar dados para JDBC**, e use `toLocalDate()` ao ler.

---

## 🧠 Conclusão

> `java.sql.Date` é uma versão especializada de `java.util.Date` para JDBC que **ignora o componente de hora**, mesmo armazenando internamente. Ambos são considerados **legados** desde o Java 8, com a recomendação de usar a API moderna `java.time.*` no lugar.

-------------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------


Claro! Abaixo está o **conteúdo completo da cheatsheet** atualizado com a adição explícita dos tipos JDBC (`Types.DATE`, `Types.TIMESTAMP`, etc.) em chamadas ao `params.addValue(...)` usando `NamedParameterJdbcTemplate`.

---

# 📝 Cheatsheet com `Types.X`: Datas com `NamedParameterJdbcTemplate` (MySQL + Java 8)

---

## 📦 Inserção de datas (`INSERT`)

### 1. Usando `LocalDate` com `DATE` (recomendado)

```java
params.addValue("birthDate", java.sql.Date.valueOf(localDate), Types.DATE); // MySQL DATE
```

### 2. Usando `LocalDateTime` com `DATETIME`

```java
params.addValue("createdAt", Timestamp.valueOf(localDateTime), Types.TIMESTAMP); // MySQL DATETIME
```

### 3. Usando `Instant` com `TIMESTAMP`

```java
params.addValue("eventTime", Timestamp.from(instant), Types.TIMESTAMP); // MySQL TIMESTAMP (UTC)
```

### 4. Usando `java.util.Date` (drivers antigos / legado)

```java
params.addValue("created", new java.util.Date(), Types.TIMESTAMP);
```

---

## 📥 Consulta de datas (`SELECT`)

### 1. Mapeando `DATE` para `LocalDate`

```java
LocalDate dataNascimento = rs.getObject("birth_date", LocalDate.class);
```

### 2. Mapeando `DATETIME` para `LocalDateTime`

```java
LocalDateTime criadoEm = rs.getObject("created_at", LocalDateTime.class);
```

### 3. Mapeando `TIMESTAMP` para `Instant`

```java
Instant eventoUtc = rs.getTimestamp("event_time").toInstant();
```

### 4. Mapeando para `java.util.Date`

```java
Date criado = rs.getTimestamp("created"); // Subclasse de java.util.Date
```

---

## 🔄 Conversões entre APIs

| Origem (`java.time`) | Conversão para JDBC                |
| -------------------- | ---------------------------------- |
| `LocalDate`          | `java.sql.Date.valueOf(localDate)` |
| `LocalDateTime`      | `Timestamp.valueOf(localDateTime)` |
| `Instant`            | `Timestamp.from(instant)`          |

| Origem (legado)      | Conversão para `java.time`                        |
| -------------------- | ------------------------------------------------- |
| `java.sql.Date`      | `sqlDate.toLocalDate()`                           |
| `java.sql.Timestamp` | `ts.toLocalDateTime()` ou `ts.toInstant()`        |
| `java.util.Date`     | `date.toInstant().atZone(ZoneId.systemDefault())` |

---

## ✅ Boas práticas

* ✅ Prefira `java.time.*` sempre que possível
* ⚠️ Use `java.sql.Date`/`Timestamp` apenas na **camada JDBC**
* 🚫 Evite `java.util.Date` no domínio da aplicação
* 🌐 Para UTC, use `Instant` + `Timestamp.from(...)`
* 🕓 Para armazenar fuso horário, use `ZoneId` em coluna separada

---

## 📌 Exemplo completo de insert com tipos explícitos

```java
String sql = "INSERT INTO person (name, birth_date, created_at) VALUES (:name, :birthDate, :createdAt)";
MapSqlParameterSource params = new MapSqlParameterSource();
params.addValue("name", "Lucas", Types.VARCHAR);
params.addValue("birthDate", java.sql.Date.valueOf(localDate), Types.DATE);
params.addValue("createdAt", Timestamp.valueOf(LocalDateTime.now()), Types.TIMESTAMP);

namedParameterJdbcTemplate.update(sql, params);
```

------------------------------------------------------------------------

-------------------------------------------------------------------------

-------------------------------------------------------------------------

Perfeito! Abaixo está uma **cheatsheet completa de conversões e uso de `java.sql.Date`, `java.util.Date` e `java.time` com `NamedParameterJdbcTemplate`**.

---

# 📝 Cheatsheet: Datas com `NamedParameterJdbcTemplate` (MySQL + Java 8)

---

## 📦 Inserção de datas (`INSERT`)

### 1. Usando `LocalDate` com `DATE` (recomendado)

```java
MapSqlParameterSource params = new MapSqlParameterSource();
params.addValue("birthDate", java.sql.Date.valueOf(localDate)); // MySQL DATE
```

### 2. Usando `LocalDateTime` com `DATETIME`

```java
params.addValue("createdAt", Timestamp.valueOf(localDateTime)); // MySQL DATETIME
```

### 3. Usando `Instant` com `TIMESTAMP`

```java
params.addValue("eventTime", Timestamp.from(instant)); // MySQL TIMESTAMP (UTC)
```

### 4. Usando `java.util.Date` (com drivers antigos)

```java
params.addValue("created", new java.util.Date()); // Internamente vira Timestamp
```

---

## 📥 Consulta de datas (`SELECT`)

### 1. Mapeando para `LocalDate` via `RowMapper`

```java
rs.getObject("birth_date", LocalDate.class)
```

### 2. Mapeando `DATETIME` para `LocalDateTime`

```java
rs.getObject("created_at", LocalDateTime.class)
```

### 3. Mapeando `TIMESTAMP` para `Instant`

```java
rs.getTimestamp("event_time").toInstant()
```

### 4. Mapeando para `java.util.Date`

```java
rs.getTimestamp("created") // retorna java.sql.Timestamp (subclasse de java.util.Date)
```

---

## 🔄 Conversões entre APIs

| Origem (`java.time`) | Conversão para JDBC                |
| -------------------- | ---------------------------------- |
| `LocalDate`          | `java.sql.Date.valueOf(localDate)` |
| `LocalDateTime`      | `Timestamp.valueOf(localDateTime)` |
| `Instant`            | `Timestamp.from(instant)`          |

| Origem (legado)      | Conversão para `java.time`                 |
| -------------------- | ------------------------------------------ |
| `java.sql.Date`      | `sqlDate.toLocalDate()`                    |
| `java.sql.Timestamp` | `ts.toLocalDateTime()` ou `ts.toInstant()` |
| `java.util.Date`     | `date.toInstant().atZone(zoneId)`          |

---

## ✅ Boas práticas

* 🟢 Prefira `java.time.*` em sua aplicação.
* ⚠️ Use `java.sql.Date`/`Timestamp` **apenas na camada de persistência**.
* 🚫 Evite `java.util.Date` se puder — é mutável e propenso a erros de fuso.
* 🌍 Armazene `Instant` com `TIMESTAMP` se quiser garantir UTC.
* 🕐 Para fuso horário, salve o `ZoneId` em coluna separada, e reconstrua com `ZonedDateTime.ofInstant()`.

---

## 📌 Exemplo completo de insert com `NamedParameterJdbcTemplate`

```java
String sql = "INSERT INTO person (name, birth_date, created_at) VALUES (:name, :birthDate, :createdAt)";
MapSqlParameterSource params = new MapSqlParameterSource();
params.addValue("name", "Lucas");
params.addValue("birthDate", java.sql.Date.valueOf(localDate));
params.addValue("createdAt", Timestamp.valueOf(LocalDateTime.now()));

namedParameterJdbcTemplate.update(sql, params);
```
--------------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------
Abaixo está uma **síntese comparativa global** entre os tipos de **data/hora do Java 8** e os **tipos correspondentes do MySQL 8**, juntamente com boas práticas ao utilizar o `NamedParameterJdbcTemplate` para integrá-los.

---

## 🧩 COMPARAÇÃO GLOBAL – Tipos de Data e Hora

| Conceito / Objetivo               | Java 8 (`java.time`)              | MySQL 8                          | Conversão recomendada com JDBC                        |
| --------------------------------- | --------------------------------- | -------------------------------- | ----------------------------------------------------- |
| **Somente data**                  | `LocalDate`                       | `DATE`                           | `ResultSet.getObject(col, LocalDate.class)`           |
| **Somente hora**                  | `LocalTime`                       | `TIME`                           | `ResultSet.getObject(col, LocalTime.class)`           |
| **Data + hora (sem fuso)**        | `LocalDateTime`                   | `DATETIME`                       | `ResultSet.getObject(col, LocalDateTime.class)`       |
| **Data + hora UTC (timestamp)**   | `Instant`                         | `TIMESTAMP` (UTC implícito)      | `Timestamp ts = rs.getTimestamp(col); ts.toInstant()` |
| **Data + hora com offset**        | `OffsetDateTime`                  | — *(não tem equivalente direto)* | Armazene como `VARCHAR`/`TIMESTAMP` + offset          |
| **Data + hora com zona nomeada**  | `ZonedDateTime`                   | — *(não tem equivalente direto)* | Use `ZonedDateTime.of(...)`, salve zona separadamente |
| **Intervalo de tempo (relativo)** | `Period` (dias, meses, anos)      | — *(não possui tipo nativo)*     | Armazene como `VARCHAR` ou decomponha em campos       |
| **Duração cronometrada (exata)**  | `Duration` (milissegundos, nanos) | — *(não possui tipo nativo)*     | Armazene como `BIGINT` (milis) ou `VARCHAR`           |

---

## 🛠️ JDBC Template com MySQL 8 – Boas Práticas

| Ação                               | Exemplo com `NamedParameterJdbcTemplate`                        | Observação                                                    |
| ---------------------------------- | --------------------------------------------------------------- | ------------------------------------------------------------- |
| **Select para `LocalDate`**        | `rs.getObject("birth_date", LocalDate.class)`                   | MySQL `DATE` → Java `LocalDate`                               |
| **Insert com `LocalDateTime`**     | `params.addValue("createdAt", LocalDateTime.now())`             | MySQL `DATETIME` ou `TIMESTAMP`                               |
| **Insert com `Instant`**           | `params.addValue("event_time", Timestamp.from(Instant.now()))`  | Java `Instant` precisa ser convertido p/ `java.sql.Timestamp` |
| **Update com fuso horário**        | Salvar campo `offset` separado em coluna textual (ex: `+03:00`) | Use `OffsetDateTime` + `ZoneOffset`                           |
| **Conversão manual com ResultSet** | `rs.getTimestamp("col").toLocalDateTime()`                      | Quando não usar `RowMapper` automático                        |
| **Formatação customizada**         | `DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")`            | Para debug ou exportação textual                              |

---

## ⚠️ ARMADILHAS COMUNS

| Problema                                  | Como evitar                                                               |
| ----------------------------------------- | ------------------------------------------------------------------------- |
| Perda de fuso horário (`ZonedDateTime`)   | Salve o `ZoneId` como string separada (ex: `America/Sao_Paulo`)           |
| Armazenar `Instant` como `DATETIME`       | Prefira `TIMESTAMP` para manter UTC                                       |
| Diferença de comportamento em `TIMESTAMP` | Sempre garanta que o timezone do banco e da JVM estejam bem definidos     |
| Serialização incorreta para JSON          | Use `@JsonFormat` ou `JavaTimeModule` do Jackson corretamente configurado |

---

## 🧭 Quando usar cada tipo no Java

| Deseja armazenar...                        | Use no Java     | E no MySQL              |
| ------------------------------------------ | --------------- | ----------------------- |
| Apenas a data (sem hora)                   | `LocalDate`     | `DATE`                  |
| Data e hora locais (sem fuso)              | `LocalDateTime` | `DATETIME`              |
| Data e hora com fuso (precisa de zona)     | `ZonedDateTime` | `TIMESTAMP` + `zone_id` |
| Um timestamp absoluto (sem contexto local) | `Instant`       | `TIMESTAMP`             |

---

## ✅ Conclusão

* A API `java.time` do Java 8 é **imutável, clara e adequada para modelar qualquer cenário de data/hora**.
* O `NamedParameterJdbcTemplate` permite **bind seguro e limpo de parâmetros com tipos de data**, respeitando os tipos SQL.
* MySQL 8 lida bem com `DATE`, `TIME`, `DATETIME` e `TIMESTAMP`, mas não representa `OffsetDateTime` ou `ZonedDateTime` diretamente — nesses casos, **armazene o fuso separadamente**.

---------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------

Abaixo está a **síntese comparativa atualizada**, agora incluindo também o tipo legado `java.util.Date`, usado antes do Java 8 (e ainda muito encontrado em integrações com bibliotecas ou JDBC).

---

# 📘 Síntese Comparativa – Tipos de Data e Hora no Java 8 + `java.util.Date` + MySQL 8 + JDBC

---

## 📆 TABELA GLOBAL DE TIPOS

| Objetivo / Conceito                   | Tipo Java 8 (`java.time`) | Tipo legado (`java.util`) | MySQL 8     | Conversão JDBC recomendada                                |
| ------------------------------------- | ------------------------- | ------------------------- | ----------- | --------------------------------------------------------- |
| Somente data                          | `LocalDate`               | `java.sql.Date`           | `DATE`      | `rs.getObject("col", LocalDate.class)`                    |
| Somente hora                          | `LocalTime`               | `java.sql.Time`           | `TIME`      | `rs.getObject("col", LocalTime.class)`                    |
| Data + hora (sem fuso)                | `LocalDateTime`           | `java.sql.Timestamp`      | `DATETIME`  | `rs.getObject("col", LocalDateTime.class)`                |
| Data + hora absoluta (UTC)            | `Instant`                 | `java.util.Date`          | `TIMESTAMP` | `rs.getTimestamp("col").toInstant()`                      |
| Data + hora com offset (ex: +03:00)   | `OffsetDateTime`          | —                         | —           | Armazene offset como texto separado                       |
| Data + hora com zona (ex: America/SP) | `ZonedDateTime`           | —                         | —           | Salvar `ZonedDateTime.toInstant()` + `zone_id`            |
| Duração exata                         | `Duration`                | —                         | —           | Armazene milissegundos como `BIGINT` ou `VARCHAR`         |
| Intervalo de tempo (anos, meses...)   | `Period`                  | —                         | —           | Armazene partes separadas (ano, mês...) ou como `VARCHAR` |

---

## 🔄 Conversões entre tipos

| De / Para                     | Conversão                                  |
| ----------------------------- | ------------------------------------------ |
| `java.util.Date` → `Instant`  | `date.toInstant()`                         |
| `Instant` → `java.util.Date`  | `Date.from(instant)`                       |
| `LocalDateTime` → `Timestamp` | `Timestamp.valueOf(localDateTime)`         |
| `Timestamp` → `LocalDateTime` | `rs.getTimestamp("col").toLocalDateTime()` |
| `LocalDate` → `java.sql.Date` | `java.sql.Date.valueOf(localDate)`         |
| `java.sql.Date` → `LocalDate` | `rs.getDate("col").toLocalDate()`          |

---

## 💡 Quando ainda usar `java.util.Date`?

| Situação                                | Justificativa                                                        |
| --------------------------------------- | -------------------------------------------------------------------- |
| Bibliotecas antigas (ex: JasperReports) | Elas esperam `java.util.Date`, mesmo com suporte parcial a `Instant` |
| Serialização com frameworks antigos     | Às vezes mais fácil lidar com `Date` diretamente                     |
| Integração com JDBC puro                | JDBC retorna `java.sql.Date` e `Timestamp` por padrão                |

---

## 🧠 Boas práticas com `NamedParameterJdbcTemplate`

| Ação                                        | Exemplo                                                  |
| ------------------------------------------- | -------------------------------------------------------- |
| **SELECT para `LocalDate`**                 | `rs.getObject("col", LocalDate.class)`                   |
| **INSERT com `LocalDateTime`**              | `params.addValue("createdAt", LocalDateTime.now())`      |
| **INSERT com `Instant`**                    | `params.addValue("event_time", Timestamp.from(instant))` |
| **INSERT com `java.util.Date`**             | `params.addValue("created", new Date())`                 |
| **UPDATE com `LocalDateTime`**              | `params.addValue("lastModified", LocalDateTime.now())`   |
| **Requisição `DATETIME` → `LocalDateTime`** | `rs.getTimestamp("created_at").toLocalDateTime()`        |

---

## ⚠️ Armadilhas comuns

| Problema                                          | Como evitar                                                       |
| ------------------------------------------------- | ----------------------------------------------------------------- |
| Usar `java.util.Date` diretamente com fuso errado | Prefira `Instant` ou `ZonedDateTime` para controle de fuso        |
| `DATETIME` vs `TIMESTAMP` no MySQL                | `TIMESTAMP` é UTC; `DATETIME` é local. Use conforme a necessidade |
| Serialização ruim de `java.util.Date`             | Use `@JsonFormat` ou converta para `LocalDateTime` com Jackson    |
| Armazenar `ZonedDateTime` diretamente             | Salve `Instant` + `ZoneId` separados                              |

---

## ✅ Recomendação geral

| Deseja modelar...              | Use no Java moderno        | E no MySQL?         |
| ------------------------------ | -------------------------- | ------------------- |
| Apenas uma data (sem hora)     | `LocalDate`                | `DATE`              |
| Data e hora local (sem fuso)   | `LocalDateTime`            | `DATETIME`          |
| Data e hora absoluta (UTC)     | `Instant`                  | `TIMESTAMP`         |
| Data e hora com offset ou zona | `ZonedDateTime` + `ZoneId` | `TIMESTAMP` + texto |
| Integração com libs antigas    | `java.util.Date`           | `TIMESTAMP`         |

---

## 🏁 Conclusão

> A transição da API antiga (`Date`, `Calendar`) para a nova (`java.time`) traz **clareza, segurança, imutabilidade e controle de fuso**. Ao integrar com JDBC e MySQL 8, saiba:
>
> * **O tipo MySQL define qual tipo Java usar**
> * **Evite misturar fuso com `DATETIME`**
> * **Prefira `NamedParameterJdbcTemplate` com tipos modernos e conversões explícitas**

--------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------

Vamos agora focar especificamente nos métodos do `ResultSet` como `getDate()`, `getTime()` e `getTimestamp()`, explicando:

* Quais são os métodos disponíveis no `ResultSet` para extrair datas
* O que eles retornam
* Como convertê-los para a API moderna do Java 8 (`java.time`)
* E quando **usar ou evitar** esses métodos

---

## 📥 Métodos do `ResultSet` para campos de data/hora

| Método JDBC (`ResultSet`)        | Tipo retornado (antigo)       | Conversão para Java 8                  |
| -------------------------------- | ----------------------------- | -------------------------------------- |
| `rs.getDate(String column)`      | `java.sql.Date`               | `.toLocalDate()`                       |
| `rs.getTime(String column)`      | `java.sql.Time`               | `.toLocalTime()`                       |
| `rs.getTimestamp(String column)` | `java.sql.Timestamp`          | `.toLocalDateTime()` ou `.toInstant()` |
| `rs.getObject(col, Class<T>)`    | Direto para `LocalDate`, etc. | Requer JDBC 4.2+ (Java 8 compatível)   |

---

## 🧪 Exemplos práticos com conversões

### 1. Para `LocalDate`

```java
LocalDate dataNascimento = rs.getDate("birth_date").toLocalDate();
```

### 2. Para `LocalTime`

```java
LocalTime horario = rs.getTime("horario").toLocalTime();
```

### 3. Para `LocalDateTime`

```java
LocalDateTime atualizadoEm = rs.getTimestamp("updated_at").toLocalDateTime();
```

### 4. Para `Instant` (UTC)

```java
Instant eventoUtc = rs.getTimestamp("event_time").toInstant();
```

---

## ✅ Quando usar `rs.getObject(..., Class<T>)`

A partir do JDBC 4.2 (Java 8), você pode fazer:

```java
LocalDate data = rs.getObject("birth_date", LocalDate.class);
LocalDateTime dt = rs.getObject("updated_at", LocalDateTime.class);
```

**Vantagem:** Evita o uso dos tipos legados `java.sql.Date`, `java.sql.Timestamp`.

---

## ⚠️ Quando *não* usar `getDate()/getTimestamp()`

* **Quando já está usando a API moderna (`java.time`)** → prefira `getObject(..., Class<T>)`
* **Quando precisa do fuso horário real (`ZonedDateTime`)** → `Timestamp` não contém `ZoneId`
* **Quando o banco usa `TIMESTAMP` e você precisa de UTC real** → cuidado com a conversão local da JVM

---

## 🧠 Resumo: Qual usar?

| Objetivo                         | Melhor opção em Java 8 com JDBC              |
| -------------------------------- | -------------------------------------------- |
| Usar API moderna                 | `getObject("col", LocalDate.class)` etc.     |
| Compatibilidade com libs legadas | `getDate()/getTimestamp()` + `.toLocalXxx()` |
| Lidar com UTC                    | `getTimestamp()` → `.toInstant()`            |

---

## ✅ Conclusão

* `rs.getDate()`, `getTime()` e `getTimestamp()` ainda são úteis, especialmente quando se trabalha com bibliotecas ou drivers mais antigos.
* **Mas a forma preferida em Java 8 é `rs.getObject("col", Tipo.class)`**, pois retorna diretamente tipos da nova API (`LocalDate`, `LocalDateTime`, `Instant`).
* Para fusos horários, a responsabilidade continua sendo sua: `Timestamp` não carrega essa informação — para isso, use `Instant` + `ZoneId` manualmente.

--------------------------------------------------------------------------
--------------------------------------------------------------------------
--------------------------------------------------------------------------

o JDBC namedparameter aceita apenas tipos de parâmetros Types.TIMESTAMP e Types.DATE sendo que passados apenas os tipos: java.sql.Date, java.util.Date e Timestamp?

Sim, o `NamedParameterJdbcTemplate` **aceita perfeitamente os tipos `java.sql.Date`, `java.util.Date` e `java.sql.Timestamp`**, e você pode (opcionalmente) especificar o tipo SQL correspondente via `java.sql.Types`.

Abaixo está uma explicação completa:

---

## ✅ Tipos suportados por `NamedParameterJdbcTemplate`

### 🟢 Aceitos diretamente (sem conversão extra):

| Tipo Java            | Tipo SQL recomendado (`Types`) | Comportamento no MySQL                          |
| -------------------- | ------------------------------ | ----------------------------------------------- |
| `java.sql.Date`      | `Types.DATE`                   | Armazena somente a **data**                     |
| `java.sql.Timestamp` | `Types.TIMESTAMP`              | Armazena data e hora, **sem fuso** (`DATETIME`) |
| `java.util.Date`     | `Types.TIMESTAMP`              | Internamente convertido para `Timestamp`        |

> 🔎 Nota: `java.util.Date` é aceito **porque o driver JDBC converte automaticamente** para `Timestamp`.

---

## 📌 Exemplo com todos os três tipos

```java
params.addValue("onlyDate", java.sql.Date.valueOf(LocalDate.now()), Types.DATE); // DATE
params.addValue("withTime", Timestamp.valueOf(LocalDateTime.now()), Types.TIMESTAMP); // DATETIME
params.addValue("legacyDate", new java.util.Date(), Types.TIMESTAMP); // TIMESTAMP
```

---

## ⚠️ Observações importantes

* Mesmo que você **não use `Types.X` explicitamente**, o Spring tenta **inferir automaticamente o tipo SQL** correto com base no tipo Java do valor.
* O uso de `Types.DATE` ou `Types.TIMESTAMP` é **opcional**, mas recomendável para clareza e controle, especialmente com valores nulos ou drivers mais restritos.
* O Spring **não converte automaticamente** de `java.time.LocalDate` ou `LocalDateTime`. Para isso, você deve fazer:

  ```java
  java.sql.Date.valueOf(localDate)
  Timestamp.valueOf(localDateTime)
  ```

---

## 🚫 Tipos `java.time` direto?

| Tipo Java 8     | Funciona direto em `params.addValue(...)`?                |
| --------------- | --------------------------------------------------------- |
| `LocalDate`     | ❌ **Não** — deve ser convertido com `Date.valueOf()`      |
| `LocalDateTime` | ❌ **Não** — deve ser convertido com `Timestamp.valueOf()` |
| `Instant`       | ❌ **Não** — use `Timestamp.from(instant)`                 |

> ⚠️ Embora o JDBC 4.2 permita uso direto de `LocalDate` e `LocalDateTime` com `PreparedStatement#setObject(...)`, o Spring JDBC ainda **espera os tipos SQL legados** (`java.sql.Date`, `Timestamp`) para garantir compatibilidade.

---

## ✅ Conclusão

Sim, o `NamedParameterJdbcTemplate` aceita apenas **tipos compatíveis com JDBC**:

* `java.sql.Date` → `Types.DATE`
* `java.sql.Timestamp` e `java.util.Date` → `Types.TIMESTAMP`

E para trabalhar com `java.time`, sempre converta explicitamente para esses tipos.

-------------------------------------------------------------------------
-------------------------------------------------------------------------
-------------------------------------------------------------------------

aceita outros tipos (que não java.sql.Date, java.util.Date e Timestamp)?

Sim, o `NamedParameterJdbcTemplate` **aceita vários outros tipos Java**, **além de** `java.sql.Date`, `java.util.Date` e `java.sql.Timestamp`.

Aqui está uma **lista detalhada de tipos Java compatíveis** com JDBC e que são aceitos pelo `NamedParameterJdbcTemplate`:

---

## ✅ Tipos Java aceitos pelo `NamedParameterJdbcTemplate`

### 1. **Tipos primitivos e suas wrappers**

| Tipo Java            | SQL Type inferido |
| -------------------- | ----------------- |
| `int`, `Integer`     | `Types.INTEGER`   |
| `long`, `Long`       | `Types.BIGINT`    |
| `double`, `Double`   | `Types.DOUBLE`    |
| `float`, `Float`     | `Types.REAL`      |
| `boolean`, `Boolean` | `Types.BOOLEAN`   |
| `short`, `Short`     | `Types.SMALLINT`  |
| `byte`, `Byte`       | `Types.TINYINT`   |

---

### 2. **Strings e dados de texto**

| Tipo Java   | SQL Type inferido         |
| ----------- | ------------------------- |
| `String`    | `Types.VARCHAR`           |
| `char[]`    | `Types.VARCHAR` ou `CLOB` |
| `Character` | `Types.CHAR`              |

---

### 3. **Enums**

| Tipo Java | SQL Type                                                       |
| --------- | -------------------------------------------------------------- |
| `Enum`    | `Types.VARCHAR` (via `.name()` ou `.ordinal()` se customizado) |

---

### 4. **Datas e hora**

| Tipo Java                 | SQL Type esperado                     |
| ------------------------- | ------------------------------------- |
| `java.sql.Date`           | `Types.DATE`                          |
| `java.util.Date`          | `Types.TIMESTAMP`                     |
| `java.sql.Timestamp`      | `Types.TIMESTAMP`                     |
| `java.sql.Time`           | `Types.TIME`                          |
| `java.time.LocalDate`     | ❌ (não suportado diretamente)         |
| `java.time.LocalDateTime` | ❌ (não suportado diretamente)         |
| `java.time.Instant`       | ❌ (precisa `Timestamp.from(instant)`) |

> 🔔 Para os tipos do pacote `java.time`, **você deve converter manualmente** para os tipos JDBC compatíveis (`Date`, `Timestamp`, etc.)

---

### 5. **Outros**

| Tipo Java                   | Observação                                 |
| --------------------------- | ------------------------------------------ |
| `BigDecimal` / `BigInteger` | Aceitos como `NUMERIC` ou `DECIMAL`        |
| `UUID`                      | Tratado como `VARCHAR`                     |
| `InputStream` / `Reader`    | Para campos `BLOB` / `CLOB`                |
| `byte[]`                    | Para campos binários (`BLOB`, `VARBINARY`) |

---

## ❌ Tipos não aceitos diretamente

| Tipo Java                         | Por que não?                         |
| --------------------------------- | ------------------------------------ |
| `LocalDate`, `LocalDateTime`      | Não são convertidos automaticamente  |
| `ZonedDateTime`, `OffsetDateTime` | JDBC não tem representação direta    |
| `List<T>` (para JSON)             | Depende de extensão/conversor manual |
| `Map<String, Object>` (para JSON) | Idem acima                           |
| Objetos personalizados (DTOs)     | Você deve extrair e mapear campos    |

---

## ✅ Como o Spring trata esses tipos?

O Spring, por trás do `NamedParameterJdbcTemplate`, delega para:

* O driver JDBC (ex: `com.mysql.cj.jdbc.Driver`)
* E internamente usa `org.springframework.jdbc.core.StatementCreatorUtils`

Esse utilitário decide como mapear cada tipo Java → tipo SQL.

---

## 🎯 Exemplo avançado

```java
MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("id", UUID.randomUUID(), Types.VARCHAR)
    .addValue("name", "Lucas", Types.VARCHAR)
    .addValue("price", new BigDecimal("199.99"), Types.NUMERIC)
    .addValue("active", true, Types.BOOLEAN)
    .addValue("created_at", Timestamp.valueOf(LocalDateTime.now()), Types.TIMESTAMP);
```

---

## ✅ Conclusão

O `NamedParameterJdbcTemplate` aceita **vários tipos Java compatíveis com JDBC**, não se limitando a datas. Basta:

* Converter corretamente os tipos modernos (`java.time`)
* Usar `Types.X` quando quiser controle explícito
