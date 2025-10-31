Ótimo! Vou organizar e sintetizar todo o conteúdo discutido neste chat sobre:

* Todos os usos e funcionalidades das classes de data/hora do Java 8 (`java.time.*`)
* Conversões com `java.util.Date` e `java.sql.Date`
* Mapeamento e inserção com JDBC e `NamedParameterJdbcTemplate`
* Comportamento no MySQL 8 para os tipos `DATE`, `DATETIME` e `TIMESTAMP`
* Uso explícito de `java.sql.Types` como `Types.TIMESTAMP` e `Types.DATE`
* Boas práticas, armadilhas e tabelas comparativas claras

A resposta será estruturada como um guia técnico completo, com exemplos práticos, tabelas e um modelo de `README.md` pronto para repositórios Java.

Voltarei em breve com o conteúdo completo para você.


# Guia Completo: Datas e Horas no Java 8 (API java.time) e Integração com MySQL

Java 8 introduziu a API de data/hora do pacote `java.time` (JSR-310), que substitui as antigas classes problemáticas de datas (`java.util.Date`, `Calendar`, `SimpleDateFormat`, etc.). Essa nova API é **imutável** e **thread-safe**, oferecendo classes bem definidas para diferentes usos (datas anuais, horas do dia, instantes no tempo, períodos, durações, etc.), eliminando muitos dos erros e confusões das abordagens anteriores. Este guia detalha o uso de todas as principais classes de data e data+hora do Java 8, comparando-as com as classes legadas, mapeando-as para os tipos SQL do MySQL e demonstrando como usá-las com JDBC (especialmente com `NamedParameterJdbcTemplate` do Spring). Também abordamos conversões, armadilhas comuns e boas práticas com exemplos de código.

## Visão Geral da API de Data e Hora do Java 8

A API `java.time` foi inspirada na biblioteca Joda-Time e projetada para resolver deficiências das classes legadas. As classes antigas eram mutáveis, não thread-safe e dependentes de fusos horários do sistema de forma confusa. Com Java 8, não há motivo para não utilizar a API moderna para novos projetos – mesmo em cenários onde bibliotecas ou APIs legadas ainda exigem os tipos antigos, os métodos de conversão estão embutidos para facilitar a interoperabilidade. Em resumo, use as classes do pacote `java.time` sempre que possível; elas **suplantam as antigas classes de data/hora problemáticas** e tornam o código mais claro e menos propenso a erros.

Alguns pontos-chave sobre a nova API:

* As classes são específicas para cada propósito (data somente, hora somente, data e hora combinadas, instante global, etc.), evitando ambiguidades.
* Todas as principais classes são imutáveis e thread-safe, podendo ser livremente compartilhadas entre threads.
* A API oferece métodos de fábrica (`now()`, `of()`, `parse()`), métodos de conversão (`from()`, `to...()`), além de métodos para adicionar/subtrair tempo (`plus()`, `minus()`) e outras utilidades (como ajustadores temporais, formatação, etc).
* Suporte a fuso horário explícito e offset UTC está embutido em classes específicas (`ZoneId`, `ZoneOffset`, `ZonedDateTime`, `OffsetDateTime`), em vez de espalhado implicitamente como nas antigas `java.util.Date`/`Calendar`.
* Integração nativa com JDBC 4.2+: é possível enviar e recuperar objetos `java.time` diretamente via `PreparedStatement.setObject(...)` e `ResultSet.getObject(...)` quando se usa drivers JDBC compatíveis (tanto o driver MySQL 8+ quanto os de outros bancos modernos suportam isso). Veremos detalhes adiante.

## Principais Classes do Pacote `java.time`

A seguir, explicamos as principais classes da API de data e hora do Java 8, incluindo seus usos e funcionalidades:

### LocalDate (Data Sem Hora)

A classe `LocalDate` representa uma **data** no calendário ISO (ano, mês e dia) sem qualquer informação de hora do dia ou fuso horário. É adequada para representar datas como datas de nascimento, feriados, datas de agendamento sem hora específica, etc., ou seja, uma data "civil". Por exemplo, `LocalDate.of(2025, 12, 31)` representa 31 de dezembro de 2025.

Características e usos do `LocalDate`:

* **Sem tempo do dia:** contém apenas ano, mês e dia. Se você precisar também da hora/minuto/segundo, combine com `LocalTime` ou use um `LocalDateTime`.
* **Sem fuso horário:** um `LocalDate` não possui offset ou zona – é simplesmente uma data no calendário local. Portanto, não sofre ajustes de horário de verão ou fusos.
* **Operações:** oferece métodos para obter campos (como `getDayOfWeek()` para dia da semana, `getDayOfMonth()`, etc.) e para manipulação: por exemplo, `date.plusMonths(1)` retorna um novo `LocalDate` adicionando um mês. Também suporta comparações (`isBefore`, `isAfter`, etc.) e pode ser usado com utilitários como `Period` e `ChronoUnit` para cálculos de diferença.
* **Exemplo de uso:**

  ```java
  LocalDate hoje = LocalDate.now();                        // data atual
  LocalDate vencimento = hoje.plusDays(15);                // 15 dias no futuro
  DayOfWeek diaSemana = vencimento.getDayOfWeek();         // dia da semana do vencimento
  LocalDate dataEspecifica = LocalDate.of(2000, Month.NOVEMBER, 20);
  ```

  Nesse exemplo, `hoje` pode ser "2025-08-02" (formato ISO por padrão), e `vencimento` será "2025-08-17" se hoje for 2/Ago/2025, e `diaSemana` indicaria o dia da semana dessa data (por exemplo, MONDAY).

### LocalTime (Hora Sem Data)

A classe `LocalTime` representa um **horário** no dia, sem referência a data ou fuso horário. Use-a para horários do dia, como horário de abertura de uma loja, horário de alarmes, etc., quando a data em si não importa.

Características do `LocalTime`:

* **Sem data nem fuso:** contém apenas hora, minuto, segundo e nanos (se necessário). Exemplo: `LocalTime.of(14, 30)` representa 14:30 (2:30 da tarde). Não há associação a uma data específica ou timezone; é como um relógio de parede local.
* **Precisão de nanos:** suporta precisão de nanossegundos (até 9 dígitos decimais para segundos).
* **Operações:** métodos para adicionar/subtrair horas, minutos, etc. (por exemplo, `time.plusHours(3)`), bem como truncar ou ajustar (e.g. `withHour(0)` retorna uma cópia com hora ajustada).
* **Exemplo de uso:**

  ```java
  LocalTime agora = LocalTime.now();          // hora atual (do sistema)
  LocalTime aula = LocalTime.of(19, 0);       // 19:00 horário da aula
  LocalTime pausa = aula.plusMinutes(15);     // 19:15, horário após 15 minutos
  boolean depois = agora.isAfter(aula);
  ```

  Aqui `agora` pega a hora atual (digamos 17:45:30), `aula` representa 19:00 fixo, `pausa` seria 19:15, e `depois` indicaria se `agora` já passou de 19:00.

*Observação:* Assim como `LocalDate`, o `LocalTime` não sabe nada de fusos ou datas; se você precisar representar um momento específico no tempo (por exemplo, "hoje às 19:00 no fuso X"), então `LocalTime` não é suficiente – você precisaria combinar com uma data e um fuso (ver `ZonedDateTime` ou `OffsetTime/OffsetDateTime`).

### LocalDateTime (Data e Hora, sem Fuso Horário)

A classe `LocalDateTime` combina uma data e uma hora (sem informação de fuso horário). É um **timestamp local**, representando uma data e hora como enxergados num determinado relógio local, mas sem relação com UTC ou zonas. Em outras palavras, um `LocalDateTime` poderia ser "31/12/2025 23:59:59", **mas não diz *onde* ou *qual fuso*** é essa data/hora.

Principais aspectos do `LocalDateTime`:

* **Combina `LocalDate` e `LocalTime`:** efetivamente é um par de data e hora. Por exemplo, `LocalDateTime.of(2025, 12, 31, 23, 59, 59)` representa "31/12/2025 23:59:59". É útil para eventos locais, como "reunião no escritório às 15:00 no dia X" (pressupõe-se o horário local do evento).
* **Sem fuso/offset:** assim como os prefixados com "Local", não armazena fuso horário. Isso significa que não há conversão automática de horário de verão ou diferença de UTC – é literalmente o mesmo relógio de parede combinado com um calendário. Para incluir fuso, use `ZonedDateTime` ou `OffsetDateTime` conforme necessário.
* **Operações:** fornece vários métodos de fábrica (`now()`, `of(...)`, `ofInstant(...)`), métodos de ajuste e cálculo (adicionar/subtrair dias, segundos, etc. similar ao `LocalDate` e `LocalTime`) e conversões para outros tipos. Por exemplo, `LocalDateTime.ofInstant(instant, zone)` converte um `Instant` para um `LocalDateTime` em determinado fuso (veremos conversões mais adiante).
* **Exemplo de uso:**

  ```java
  LocalDateTime agora = LocalDateTime.now();                      // data/hora local atual
  LocalDateTime especifico = LocalDateTime.of(2025, 12, 31, 23, 0);
  LocalDateTime daquiUmaHora = agora.plusHours(1);
  LocalDateTime deInstant = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
  ```

  Acima, `agora` pode ser `2025-08-02T17:39:34` (no formato ISO), `especifico` é 31/12/2025 23:00, `daquiUmaHora` adiciona uma hora, e `deInstant` mostra como converter um instante UTC para um `LocalDateTime` no fuso padrão do sistema.

**Importante:** Use `LocalDateTime` apenas para representar data/hora **sem referência global**. Por exemplo, em sistemas de agendamento local ou para dados que serão interpretados sempre no mesmo fuso (como um horário de funcionamento "09:00" que sempre se refere à hora local de uma loja). Se você precisar de um instante específico no timeline global (por exemplo, um timestamp de evento de log), considere usar `Instant` ou `ZonedDateTime`/`OffsetDateTime` para evitar ambiguidades de fuso.

### ZonedDateTime (Data e Hora com Zona Geográfica)

A classe `ZonedDateTime` representa uma data e hora **com um fuso horário específico (região)**. Essencialmente, é um `LocalDateTime` associado a um `ZoneId` (região do mundo, como `"America/Sao_Paulo"` ou `"Europe/Paris"`), incluindo regras de fuso (offsets variáveis, horário de verão, etc.). Um `ZonedDateTime` corresponde a um momento específico no tempo global, pois sabe **que horas eram e onde**.

Características do `ZonedDateTime`:

* **Data/hora + Zona:** contém ano, mês, dia, hora, minuto, segundo, nanos **e** um identificador de zona (com fusos horários completos, incluindo regras de horário de verão). Exemplo: `2025-08-02T17:39:34-03:00[America/Sao_Paulo]`. Aqui o offset `-03:00` e a zona indicam o momento exato no tempo.
* **Conversões de fuso:** como a zona traz informações de offset, a classe lida com conversões de horário de verão. Por exemplo, adicionando 24 horas pode resultar em 23 ou 25 horas de diferença real se atravessar uma mudança de horário de verão naquela zona. O `ZonedDateTime` ajusta corretamente essas situações conforme as `ZoneRules` da região.
* **Operações e obtenção de Instant:** você pode obter um `Instant` (UTC) a partir de um `ZonedDateTime` usando `.toInstant()`, pois ele representa um ponto específico na linha do tempo. Inversamente, é possível criar um `ZonedDateTime` de um `Instant` especificando um fuso (`Instant.now().atZone(ZoneId.of("Europe/Paris"))`).
* **Usos:** ideal para representar eventos em fuso específico ou informações de data/hora que precisam ser mostradas conforme o horário local de um lugar. Ex.: agendamento de reunião em um escritório de outro país – você armazena como `ZonedDateTime` no fuso local daquele escritório para evitar confusões e conversões posteriores.
* **Exemplo de uso:**

  ```java
  ZonedDateTime zdtAgora = ZonedDateTime.now(ZoneId.of("UTC"));
  ZonedDateTime partida = ZonedDateTime.of(2025, 12, 31, 18, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
  ZonedDateTime chegada = partida.withZoneSameInstant(ZoneId.of("Europe/London"));
  ```

  Aqui, `zdtAgora` é o momento atual em UTC, `partida` representa 31/12/2025 18:00 no fuso de São Paulo (BR), e `chegada` converte esse mesmo instante para o fuso de Londres, ajustando o horário (mantendo o mesmo instante global).

### OffsetDateTime e OffsetTime (Data/Hora com Offset UTC)

As classes `OffsetDateTime` e `OffsetTime` são similares ao `ZonedDateTime`/`ZonedTime`, mas em vez de armazenar uma zona completa com regras, armazenam apenas um **offset fixo em relação ao UTC** (por exemplo, `-03:00`, `+05:30`).

* **`OffsetDateTime`:** Representa data e hora com um deslocamento (offset) em horas e minutos de UTC, mas **sem um identificador de região**. Por exemplo, `2025-08-02T17:39:34-03:00` (sem especificar "America/Sao\_Paulo"). É útil para dados de timestamp onde apenas o offset importa (por exemplo, sistemas ou formatos de intercâmbio de dados como padrões ISO 8601, ou quando você sempre registra eventos em UTC ou outro offset fixo). Bancos de dados que guardam somente o offset (como colunas TIMESTAMP WITH TIME ZONE em alguns SGBDs) podem se mapear bem para `OffsetDateTime`.
* **`OffsetTime`:** Similarmente, representa somente um horário do dia com um offset de UTC (hora+offset, sem data). Usado mais raramente, talvez para representar um horário global recorrente independentemente do dia.

**Diferença OffsetDateTime vs ZonedDateTime:** o `ZonedDateTime` sabe a localização (zona) e pode ter offsets diferentes ao longo do ano (ex.: horário de verão), enquanto o `OffsetDateTime` trata apenas de um offset constante. Portanto, o `ZonedDateTime` consegue aplicar regras de transição (falhas e repetições de horário), enquanto `OffsetDateTime` não lida com isso (é como se assumisse que o offset daquele momento é permanente, o que pode não ser verdade numa zona geográfica). Use `OffsetDateTime` quando quiser simplesmente armazenar um timestamp com offset conhecido (e.g., sempre UTC±0), ou quando um protocolo/format exigir (por exemplo, em JSON/XML muitas vezes utiliza-se strings com offset). Para cálculos complexos baseados em localidade, use `ZonedDateTime`.

### Instant (Instante no Tempo UTC)

A classe `Instant` representa um **instante no tempo na linha temporal global**, equivalente a um timestamp no relógio UTC, com precisão de nanossegundos. É, conceitualmente, o número de nanosegundos desde a época (epoch) Unix *1970-01-01T00:00:00Z*. Um `Instant` específico corresponde a **exatamente um ponto no tempo em todo o mundo** – não importa o fuso, ele pode ser convertido para qualquer fuso equivalendo ao mesmo momento.

Características do `Instant`:

* **Timeline absoluto:** Internamente é geralmente um grande número (segundos desde 1970 + nanos), representando um ponto único no tempo. Pode ser antes ou depois de 1970 (valores negativos para datas anteriores).
* **Sem calendário humano:** Não tem noção de ano, mês, dia, hora local – é sempre relativo a UTC. Se você imprimir um Instant, ele será formatado no padrão ISO UTC (sufixo "Z"), ex: `2025-08-02T20:39:34.123Z`. Para extrair campos de data/hora "legíveis", é necessário associá-lo a um fuso (ex: converter para `LocalDateTime` com um `ZoneId`).
* **Uso típico:** timestamps para logs, marcações de tempo precisas, cálculos de duração entre instantes, etc. Por exemplo, ao gravar o momento exato de um evento no sistema, use `Instant.now()`.
* **Operações:** Permite adição/subtração de durações (`instant.plusSeconds(60)`), comparações (`isBefore`, `isAfter`), e conversão para classes de data/hora locais via métodos como `atZone(zoneId)` ou usando `LocalDateTime.ofInstant(instant, zone)`. Também pode ser criado de volta a partir dessas classes (por exemplo, `zonedDateTime.toInstant()` ou `Timestamp.toInstant()`).
* **Exemplo de uso:**

  ```java
  Instant agoraUtc = Instant.now();                           // instante atual UTC
  Instant futuro = agoraUtc.plus(5, ChronoUnit.MINUTES);      // 5 minutos depois
  long epochMilli = agoraUtc.toEpochMilli();                  // milissegundos desde 1970
  LocalDateTime dataLocal = LocalDateTime.ofInstant(agoraUtc, ZoneId.systemDefault());
  ```

  Nesse exemplo, `agoraUtc` é o timestamp atual, `futuro` é 5 minutos após, `epochMilli` obtém o timestamp em milissegundos (pode ser útil para interoperabilidade), e `dataLocal` converte o Instant para um `LocalDateTime` no fuso do sistema (por exemplo, converte aquele instante UTC para horário local do servidor).

**Nota:** `Instant` é ideal para armazenar momentos absolutos (por exemplo, para persistência, pode mapear para colunas de *timestamp* do banco). Se você sempre usa UTC no servidor e banco, `Instant` simplifica a vida, pois elimina considerações de fuso até precisar apresentar ao usuário (momento em que você escolhe o fuso de exibição). No Java, você pode converter facilmente `Instant` <-> `OffsetDateTime`/`ZonedDateTime` conforme a necessidade de uma apresentação ou cálculo em determinado fuso.

### Period e Duration (Períodos e Durações)

Estas classes representam **intervalos de tempo**, mas de formas diferentes:

* **`Period`:** representa um período baseado em unidades de data: anos, meses e dias. Por exemplo, "2 anos, 3 meses e 5 dias". É adequado para calcular diferenças e adições *no calendário*, respeitando variações de duração dos meses, anos bissextos, etc. Um `Period` pode ser obtido, por exemplo, com `Period.between(dataInicial, dataFinal)` para saber a diferença em anos/meses/dias entre duas datas. Ao adicionar um `Period` a uma data (ou `ZonedDateTime`), o resultado leva em conta o calendário – por exemplo, adicionar 1 mês pode resultar em 28, 29, 30 ou 31 dias de incremento real dependendo do mês e ano. **Importante:** 1 dia de `Period` não é sempre 24h exatas – se aplicado em um `ZonedDateTime` numa transição de horário de verão, pode resultar em 23h ou 25h de diferença real (porque pula ou repete uma hora).
* **`Duration`:** representa uma duração baseada em tempo exato: horas, minutos, segundos, nanos. É medido em segundos e nanosegundos, destinado a durações de máquina (ex.: "90 segundos", "5 horas e 30 minutos"). Uma `Duration` de um dia é sempre exatamente 24 horas (86.400 segundos). Use `Duration` para cálculos de tempo exatos ou diferenças entre instantes (`Duration.between(instant1, instant2)`). Ao adicionar uma `Duration` de 1 dia a um `ZonedDateTime`, ele **sempre adiciona 24h exatas** – o que pode levar a uma data diferente do esperado se houve mudança de horário de verão no meio (pode cair 1h antes ou depois do mesmo horário do dia seguinte no calendário local).

Em resumo, **use `Period` para diferenças em termos de calendário humano** (meses, dias, anos, como "próximo aniversário em X meses Y dias") e **use `Duration` para medidas exatas de tempo** (segundos, nanos, como "tempo decorrido de execução"). Ambas as classes podem ser negativas (se o fim for antes do início, por exemplo) e fornecem métodos como `plus`, `minus`, etc., além de conversão para unidades (ex: `period.getDays()`, `duration.toHours()`).

Exemplos rápidos:

```java
Period umMesDezDias = Period.of(0, 1, 10); // 1 mês e 10 dias
Period entreDatas = Period.between(LocalDate.of(2020,1,1), LocalDate.now());
// entreDatas poderia ser "X anos Y meses Z dias"

Duration cincoMin = Duration.ofMinutes(5);
Duration diff = Duration.between(Instant.now(), Instant.now().plusSeconds(45));
// diff é 45 segundos
```

Ao usar Period/Duration com classes temporais: `date.plus(period)` ajusta data no calendário, `instant.plus(duration)` ajusta instante na linha do tempo. Note que `Period` só funciona com tipos que têm componente de data (LocalDate, LocalDateTime, ZonedDateTime), enquanto `Duration` funciona com tipos com componente de tempo (Instant, LocalDateTime, etc. – *não* com LocalDate puro, já que Duration é horas/minutos/segundos).

## Comparação com Classes Legadas (`java.util.Date`, `Calendar`, `java.sql.Date/Time/Timestamp`)

Antes do Java 8, trabalhávamos com `java.util.Date` (e `Calendar`) para datas e com as subclasses `java.sql.Date`, `java.sql.Time` e `java.sql.Timestamp` ao interagir com bancos via JDBC. A nova API java.time substitui essas classes, mas é importante entender como elas se comparam:

* **`java.util.Date` (util.Date):** Representa um instante no tempo (milissegundos desde 1/1/1970 UTC). Embora tenha "Date" no nome, inclui data e hora (não separa os campos) e internamente é um timestamp em UTC. Porém, ao exibi-la (toString) ou ao usá-la com `Calendar`, costuma-se aplicar o fuso padrão do sistema, o que confunde muitos desenvolvedores. Além disso, `java.util.Date` é mutável (herda de `Date` original do Java 1.0) e não é thread-safe. **Não está depreciada**, mas é considerada rudimentar e foi efetivamente substituída pela API moderna (mantida apenas por legados).

* **`java.util.Calendar` (e `GregorianCalendar`):** Era a forma de manipular datas antes do Java 8, já que `Date` tinha API limitada. Introduzido no Java 1.1, permite ajustar campos de ano/mês/dia/hora... Mas também é mutável, complexo (tem muitos estados internos), e propenso a erros. Com Java 8, deve-se evitá-lo em novos códigos.

* **`java.sql.Date` (SQL Date):** Subclasse de `util.Date` destinada a representar apenas uma data (como coluna SQL DATE). Ela armazena ano, mês e dia, mas as partes de hora, minuto, segundo e milissegundos são **zeradas ou ignoradas**. Foi feita para facilitar o JDBC a distinguir uma coluna DATE (que no SQL não tem hora) – portanto, `java.sql.Date` dá a garantia de que ao convertê-la para `LocalDate` você obtém apenas a data. Por especificação, deveria ser independente de fuso (representa apenas uma data lógica), mas historicamente havia bugs de drivers tratando-a como GMT ou com offset do sistema. Hoje, com JDBC 4.2, pode-se mapear colunas DATE diretamente para `LocalDate`, tornando `java.sql.Date` menos necessária.

* **`java.sql.Time` (SQL Time):** Análogo ao anterior, mas para colunas SQL TIME (que têm somente hora, minuto, segundo). Ela estende `util.Date` também, mas deveria ter a parte de data "zerada" (geralmente definida como 1970-01-01). Representa um horário do dia (H\:M\:S.ms). Com JDBC moderno, geralmente se usaria `LocalTime` em vez disso.

* **`java.sql.Timestamp` (SQL Timestamp):** Subclasse de `util.Date` para colunas TIMESTAMP (data e hora com frações de segundo). Ao contrário de `util.Date` (que só tem milissegundos), o `Timestamp` suporta nanossegundos adicionais, alinhado com a precisão SQL (TIMESTAMP do JDBC pode ter até 9 dígitos de sub-segundo). Ele incorpora data e hora completas. Era (e ainda é) muito usado para ler/gravar timestamps no banco via JDBC (muitos drivers convertem automaticamente entre TIMESTAMP e `java.sql.Timestamp`). No Java 8, preferimos usar `Instant` ou `LocalDateTime`/`OffsetDateTime` conforme o caso, mas `Timestamp` continua útil para interoperar com APIs que esperam esse tipo.

**Relação e conversão entre as classes legadas e modernas:**

Todas as três classes `java.sql` acima extendem `java.util.Date`. Isso significa que elas também carregam um valor de milissegundos desde 1970 internamente. Entretanto, elas especializam o significado desse valor: por exemplo, um `java.sql.Date` corresponde a meia-noite (em GMT) do dia em questão – as horas são zero. Um `java.sql.Time` terá a data base como 1970-01-01. Já o `Timestamp` usa o campo extra de nanos para maior precisão.

Com Java 8, métodos utilitários foram adicionados para facilitar conversão entre as classes legadas e as novas:

* `java.sql.Date.toLocalDate()` e `java.sql.Date.valueOf(LocalDate)` – converte para e a partir de `LocalDate` facilmente (Java 8+ já inclui esses métodos).
* `java.sql.Timestamp.toLocalDateTime()` e `java.sql.Timestamp.valueOf(LocalDateTime)` – converte para e a partir de `LocalDateTime`.
* `java.util.Date.toInstant()` e `Date.from(Instant)` – converte `util.Date` em `Instant` e vice-versa (também Java 8+).

Exemplo de conversão usando essas facilidades (de uma pergunta popular do StackOverflow):

```java
Date utilDate = new Date();                                      // java.util.Date atual
LocalDateTime ldt = LocalDateTime.ofInstant(utilDate.toInstant(), ZoneId.systemDefault());
Date utilDate2 = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
```



No código acima, convertemos um `java.util.Date` legada para `LocalDateTime` (assumindo o fuso padrão do sistema para interpretar aquele instante) e depois de volta para `Date`. Em muitos casos, porém, pode-se evitar o uso de `java.util.Date` completamente e trabalhar apenas com as novas classes.

**Comparação resumida:** A tabela a seguir resume as correspondências entre tipos Java legados vs Java 8 vs SQL:

| Situação/Tipo                            | Descrição/Uso                                  | Tipo legado (até Java 7)                                            | Tipo moderno (Java 8+)                                                |
| ---------------------------------------- | ---------------------------------------------- | ------------------------------------------------------------------- | --------------------------------------------------------------------- |
| Data apenas (sem hora)                   | Ex: 2025-08-02                                 | `java.sql.Date` (subclass de Date)                                  | `java.time.LocalDate`                                                 |
| Hora do dia (sem data)                   | Ex: 13:45:00                                   | `java.sql.Time`                                                     | `java.time.LocalTime`                                                 |
| Data e hora (sem fuso)                   | Ex: 2025-08-02 17:39:34                        | `java.sql.Timestamp` ou `java.util.Date` (util)                     | `java.time.LocalDateTime` (ou `Instant`/`OffsetDateTime` se absoluto) |
| Data e hora absoluta (UTC ou com offset) | Ex: timestamp global, log events               | `java.util.Date` (util, representava UTC internamente)              | `java.time.Instant` (UTC) ou `java.time.OffsetDateTime` (c/ offset)   |
| Data/hora com fuso específico            | Ex: 2025-08-02 17:39 -03:00 America/Sao\_Paulo | *(Sem equivalente exato; usava-se util.Date+Calendar com timezone)* | `java.time.ZonedDateTime`                                             |

*Observações:*

* `Calendar` não aparece na tabela, pois ele servia para manipular qualquer tipo de dado temporal com fuso, mas na prática hoje substituímos seu uso pelas classes acima combinadas com `ZoneId` quando preciso.
* A classe `OffsetDateTime` não tinha equivalente direto pré-Java 8; quando necessário, aplicativos guardavam o offset manualmente ou perdiam essa info.
* `Instant` é similar em propósito a `java.util.Date` (um timestamp), mas muito mais claro e com nanosegundos (Date tem apenas milissegundos).
* Embora `java.sql.Timestamp` ainda seja usado, podemos frequentemente evitá-lo usando `PreparedStatement.setObject(..., LocalDateTime/Instant)` com drivers JDBC 4.2+.

## Mapeamento entre Tipos Java e Tipos SQL (MySQL)

Ao trabalhar com banco de dados MySQL, geralmente usamos os tipos de coluna DATE, TIME, DATETIME e TIMESTAMP para armazenar informações de data/hora. Abaixo está uma comparação de como esses tipos se alinham com os tipos Java (legados e modernos) e considerações sobre seu comportamento:

| Tipo de Coluna MySQL | Intervalo e Semântica no MySQL                                                                                                                                                                                                                                                      | Tipo Java recomendado (Java 8)                                   | Detalhes de Mapeamento JDBC e Observações                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| -------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **DATE**             | Data de calendário (somente ano, mês, dia). <br>Formato padrão `'YYYY-MM-DD'`. <br>Válido de `1000-01-01` até `9999-12-31`.                                                                                                                                                         | `java.time.LocalDate`                                            | O MySQL DATE não tem parte de hora nem fuso horário. Mapeia bem para `LocalDate`. <br>**JDBC:** `ResultSet.getDate()` retorna `java.sql.Date`, que pode ser convertido via `.toLocalDate()`. Com JDBC 4.2+, você pode usar `getObject(..., LocalDate.class)` diretamente.                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **TIME**             | Horário do dia (hh\:mm\:ss). <br>Válido de `'−838:59:59'` até `'838:59:59'` (permite horas fora de 0-23 para intervalos).                                                                                                                                                           | `java.time.LocalTime`                                            | Armazena apenas hora/min/seg (possível fração). Use `LocalTime` para ler/gravar. <br>**JDBC:** `ResultSet.getTime()` dá `java.sql.Time` -> use `.toLocalTime()`. Ou `getObject(..., LocalTime.class)` se suportado.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| **DATETIME**         | Data e hora (sem fuso) exatas. <br>Formato `'YYYY-MM-DD hh:mm:ss[.ffffff]'`. <br>Válido de `1000-01-01 00:00:00` a `9999-12-31 23:59:59`. Sem conversão automática de fuso (armazenado como inserido).                                                                              | `java.time.LocalDateTime` (ou `LocalDate` + `LocalTime`)         | Representa um timestamp *local*, exatamente como armazenado. <br>**JDBC:** Mapeia para `java.sql.Timestamp` ao usar drivers legados (`getTimestamp`), ou pode usar `getObject(..., LocalDateTime.class)` com JDBC 4.2. Se utilizar `Instant`, precisará escolher um fuso para convertê-lo, pois DATETIME não guarda offset (geralmente tratado como hora local do servidor ou da aplicação). <br>**Observação:** Como não há conversão de fuso, é seguro para horários locais futuros (agenda), mas não deve ser usado para instantes absolutos sem acordo de fuso externo.                                                                                                                                                     |
| **TIMESTAMP**        | Data e hora com conversão de fuso para UTC. <br>Armazena em UTC internamente; ao inserir, converte do fuso atual da conexão para UTC, e ao ler converte de UTC para o fuso da conexão. <br>Válido de `1970-01-01 00:00:01` UTC a `2038-01-19 03:14:07` UTC (limite do Unix 32-bit). | `java.time.Instant` (ou `OffsetDateTime`/`ZonedDateTime` em UTC) | Representa um instante absoluto. <br>**JDBC:** `ResultSet.getTimestamp()` retorna `java.sql.Timestamp` (que pode ser `.toInstant()` ou `.toLocalDateTime()` no fuso *da JVM*). Com JDBC 4.2, `getObject(..., Instant.class)` pode ser suportado (depende do driver). <br>**Observação:** O TIMESTAMP do MySQL **ajusta valores** conforme o timezone da conexão. Assim, é ótimo para sempre armazenar em UTC (configurando conexão/server para UTC), mas pode causar confusão se múltiplas conexões usam fusos diferentes ou se alguém insere dados sem saber dessa conversão. A faixa limitada (1970-2038) é um fator a considerar – datas históricas ou muito futuras podem não caber em TIMESTAMP (nesse caso use DATETIME). |

Algumas diretrizes a partir da tabela:

* Para colunas **DATE**, use `LocalDate`. Para **TIME**, use `LocalTime`. Essas mapeações são diretas e não envolvem fuso horário.
* Para **DATETIME** (que não faz conversão de fuso), normalmente use `LocalDateTime` em Java – ambos representam uma data e hora "como escrito". Isso é útil para datas/horas em contexto local. *Exemplo:* horário de um compromisso no horário local do usuário armazenado como DATETIME: você guarda e recupera o mesmo `LocalDateTime` sem surpresas. **Cuidado:** não use DATETIME se precisar que o valor seja automaticamente convertido para diferentes fusos – nesse caso, TIMESTAMP é mais adequado.
* Para **TIMESTAMP**, se o objetivo é sempre armazenar instantes absolutos em UTC (o que é recomendável em sistemas distribuídos), a melhor escolha de tipo Java é `Instant` (ou `OffsetDateTime` com offset +0). O driver MySQL (8.x) suporta definir e obter `Instant` diretamente via `setObject`/`getObject`. Caso use `LocalDateTime` com TIMESTAMP, saiba que o driver **vai interpretar** aquele `LocalDateTime` como estando no fuso da conexão e convertê-lo; se não for isso que você quer, pode ter problemas. Em geral, para evitar confusão: configure a conexão do MySQL para UTC (`useLegacyDatetimeCode=false&serverTimezone=UTC` no JDBC URL, por exemplo) e então trabalhe com `Instant` ou `OffsetDateTime` em UTC – assim o TIMESTAMP sempre armazenará e retornará UTC direto.

**Exemplo prático de confusão:** Suponha que o servidor do MySQL está em UTC e a aplicação se conecta com timezone=UTC, e você insere via JDBC um `LocalDateTime.of(2025,8,2,15,0)` em uma coluna TIMESTAMP. O driver vê 15:00 *sem fuso*, assume que é 15:00 UTC (pois conexão é UTC), então armazena 15:00 UTC. Se outro cliente em outro fuso consultar, ele verá o valor ajustado ao seu fuso. Agora, se a conexão estivesse com timezone America/Sao\_Paulo (UTC-3) e você inserir o mesmo `LocalDateTime(2025-08-02T15:00)`, o driver presume que 15:00 refere-se a São Paulo, converte para 18:00 UTC para armazenar. Se depois você ler com conexão UTC, obteria 18:00. Portanto, **um mesmo código poderia inserir e ler valores diferentes dependendo do timezone da conexão**. Por isso, ou fixamos tudo em UTC ou usamos DATETIME se quisermos "15:00 como 15:00 literal sempre".

Resumindo: *Use DATETIME + LocalDateTime para dados locais; use TIMESTAMP + Instant/UTC para timestamps globais.* E nunca misture timezone sem saber o que está fazendo.

## Uso do `NamedParameterJdbcTemplate` com Datas e Horas (SELECT, INSERT, UPDATE)

O Spring oferece o `NamedParameterJdbcTemplate` para facilitar operações JDBC usando nomes de parâmetros ao invés de placeholders `?`. Vamos ver como trabalhar com parâmetros de data/hora ao usar esse template para consultas (SELECT) e atualizações/inserções (UPDATE/INSERT).

### SELECT (Consultas) com Datas

Ao realizar consultas que envolvem colunas de data/hora, podemos usar o `NamedParameterJdbcTemplate.query(...)` passando um `RowMapper` ou lambda para mapear o `ResultSet` em objetos. Suponha que temos uma tabela `eventos` com colunas: `id (BIGINT)`, `nome (VARCHAR)`, `data_evento (DATE)`, `momento_evento (DATETIME)` e queremos buscar eventos após certa data.

Exemplo de consulta usando um `NamedParameterJdbcTemplate`:

```java
// Suponha que namedParameterJdbcTemplate já esteja configurado/injetado
NamedParameterJdbcTemplate jdbc = this.namedParameterJdbcTemplate;

String sql = "SELECT id, nome, data_evento, momento_evento FROM eventos "
           + "WHERE data_evento >= :dataIni";

MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("dataIni", LocalDate.of(2025, 8, 1));  // usando LocalDate como parâmetro

List<Evento> resultados = jdbc.query(sql, params, (rs, rowNum) -> {
    Evento ev = new Evento();
    ev.setId(rs.getLong("id"));
    ev.setNome(rs.getString("nome"));
    // Coluna DATE -> LocalDate
    ev.setDataEvento(rs.getDate("data_evento").toLocalDate());
    // Coluna DATETIME -> LocalDateTime
    ev.setMomentoEvento(rs.getTimestamp("momento_evento").toLocalDateTime());
    return ev;
});
```

No exemplo acima:

* Usamos `addValue("dataIni", LocalDate.of(...))` diretamente. O `NamedParameterJdbcTemplate` sabe lidar com alguns tipos automaticamente. Para um `LocalDate`, drivers JDBC 4.2 irão mapeá-lo para um DATE SQL adequadamente; se estivermos usando um driver antigo sem suporte, talvez precisemos especificar o tipo (veremos adiante).
* No mapeamento do ResultSet, usamos métodos do `ResultSet`:

  * `rs.getDate("data_evento")` retorna um `java.sql.Date`, então chamamos `.toLocalDate()` para obter um `LocalDate` Java 8.
  * `rs.getTimestamp("momento_evento")` retorna `java.sql.Timestamp`, então `.toLocalDateTime()` nos dá o `LocalDateTime`.

Alternativamente, se o driver JDBC suportar, poderíamos usar `rs.getObject("data_evento", LocalDate.class)` e `rs.getObject("momento_evento", LocalDateTime.class)`. Desde JDBC 4.2 (Java 8), drivers compatíveis (como o do MySQL 8+) permitem essa abordagem. Isso elimina a necessidade de lidar com as classes `java.sql.*` manualmente. Por exemplo:

```java
// ... dentro do RowMapper:
ev.setDataEvento(rs.getObject("data_evento", LocalDate.class));
ev.setMomentoEvento(rs.getObject("momento_evento", LocalDateTime.class));
```

Isso já retornaria diretamente as classes do java.time em vez de java.sql. Lembre-se de consultar a documentação do driver; o do MySQL suporta sim essa conversão direta (outros SGBDs também, p.ex. PostgreSQL).

### INSERT e UPDATE com Datas

Para inserir ou atualizar registros com campos de data/hora, usamos o `NamedParameterJdbcTemplate.update(...)` passando os parâmetros nomeados. A maior diferença em relação ao SELECT é que aqui estamos fornecendo valores de data/hora para o JDBC setar na consulta preparada.

Exemplo de inserção de um novo evento:

```java
String insertSql = "INSERT INTO eventos (nome, data_evento, momento_evento) "
                 + "VALUES (:nome, :data, :momento)";

Evento novo = new Evento(null, "Reunião", LocalDate.of(2025, 9, 1), LocalDateTime.of(2025, 9, 1, 9, 30));

MapSqlParameterSource insertParams = new MapSqlParameterSource()
    .addValue("nome", novo.getNome())
    .addValue("data", novo.getDataEvento())            // LocalDate -> DATE
    .addValue("momento", novo.getMomentoEvento());     // LocalDateTime -> DATETIME

int count = jdbc.update(insertSql, insertParams);
```

Nesse código, passamos diretamente `LocalDate` e `LocalDateTime` para o template. O Spring, por baixo dos panos, irá chamar `PreparedStatement.setObject(parameterIndex, value)` para esses objetos. Com um driver JDBC atualizado, isso funciona e o driver inferirá o tipo SQL correto (DATE para LocalDate, TIMESTAMP para LocalDateTime) e enviará ao banco. Caso esteja usando um driver antigo ou encontre algum problema de inferência de tipo, podemos especificar manualmente o tipo JDBC usando uma sobrecarga de `addValue`:

```java
.addValue("data", novo.getDataEvento(), Types.DATE)
.addValue("momento", novo.getMomentoEvento(), Types.TIMESTAMP)
```

O uso de `Types.DATE` e `Types.TIMESTAMP` informa explicitamente ao driver como bindar aquele objeto. Isso evita erros do tipo "*Can't infer the SQL type to use for an instance of java.time.LocalDateTime. Use setObject() with an explicit Types value.*", que podem ocorrer com alguns drivers se não especificado.

Para *UPDATE*, o processo é idêntico, apenas mudando a SQL. Exemplo:

```java
String updateSql = "UPDATE eventos SET momento_evento = :momento WHERE id = :id";
Map<String, Object> updateParams = Map.of(
    "momento", LocalDateTime.now(),
    "id", 123
);
jdbc.update(updateSql, updateParams);
```

Aqui usamos um `Map.of` por concisão (Spring aceitará qualquer `Map<String,Object>` como fonte de parâmetros). Estamos definindo `momento` com um `LocalDateTime.now()`. Novamente, se o driver suportar, ele fará o bind corretamente. Caso contrário, poderia ser necessário: `MapSqlParameterSource().addValue("momento", LocalDateTime.now(), Types.TIMESTAMP)`.

**Batch Update com BeanPropertySqlParameterSource:**

Uma situação comum é inserir vários objetos de uma vez (batch). O Spring oferece utilitários como `SqlParameterSourceUtils.createBatch` que podem pegar uma lista de objetos e automaticamente criar um array de `SqlParameterSource` mapeando propriedades para nomes de parâmetros iguais aos campos. Isso funciona bem com as novas datas também, **desde que** o driver JDBC consiga lidar. Por exemplo:

```java
List<Evento> listaEventos = ... ;
SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(listaEventos.toArray());
namedParameterJdbcTemplate.batchUpdate(
    "INSERT INTO eventos (nome, data_evento, momento_evento) VALUES (:nome, :dataEvento, :momentoEvento)",
    batchParams
);
```

Aqui `:nome, :dataEvento, :momentoEvento` serão preenchidos refletivamente a partir dos getters de `Evento`. Se `getDataEvento()` retorna `LocalDate` e `getMomentoEvento()` retorna `LocalDateTime`, o Spring tentará fazer setObject nesses valores. Em drivers antigos (ex: MySQL Connector/J 5 ou PostgreSQL antes de adicionar suporte), isso poderia falhar informando não saber lidar com `java.time` diretamente. A solução seria: atualizar o driver ou converter manualmente antes (por exemplo, ajustar a classe Evento para ter `Timestamp getMomentoEventoSql() { return Timestamp.valueOf(momentoEvento); }` apenas para facilitar). Felizmente, hoje a maioria dos drivers lida bem com isso se você estiver em Java 8+.

**Conclusão:** Em SELECT, obtém-se dados de data/hora preferencialmente com os métodos do `ResultSet` adequados ou via getObject(Class) para já ter `java.time`. Em INSERT/UPDATE, pode-se passar as instâncias de `java.time` diretamente para o NamedParameterJdbcTemplate. Na maioria dos casos, o Spring/JDBC resolverá o tipo correto automaticamente (especialmente com drivers JDBC 4.2). Caso encontre problemas, use as constantes de `java.sql.Types` para especificar o tipo ou realize conversões explícitas para tipos java.sql legados (Date/Timestamp) antes de passar para o template.

## Conversões de Tipos: JDBC e `java.time`

Já mencionamos algumas formas de converter entre os tipos antigos e novos, mas vamos consolidar as conversões mais comuns que você pode precisar ao trabalhar com JDBC e a API de data/hora:

* **De `ResultSet` (java.sql) para `java.time`:**

  * `java.sql.Date` -> `LocalDate`: use `sqlDate.toLocalDate()` (disponível desde Java 8).
  * `java.sql.Time` -> `LocalTime`: use `sqlTime.toLocalTime()`.
  * `java.sql.Timestamp` -> `LocalDateTime`: use `sqlTimestamp.toLocalDateTime()`. (Também pode converter para Instant via `toInstant()` e depois para qualquer outro tipo com fuso desejado).
  * `ResultSet.getObject("col", LocalDate.class/LocalTime.class/LocalDateTime.class)`: retorna diretamente o tipo java.time (se suportado).
  * `ResultSet.getTimestamp("col").toInstant()`: obtém um Instant (útil se a coluna é TIMESTAMP e você quer trabalhar em UTC diretamente). Mas cuidado: `Timestamp` não guarda fuso, então assume que o Timestamp lido já representa um instante no fuso da JVM ou no UTC dependendo do driver. No MySQL, por exemplo, `getTimestamp` já converte o valor para o timezone da JVM antes de criar o Timestamp. Por isso, se for possível, prefira `getObject(..., Instant.class)` (no driver MySQL 8, isso retorna o Instant exato em UTC).

* **De `java.time` para preparar SQL (`PreparedStatement`/`ParameterSource`):**

  * `LocalDate` -> `java.sql.Date`: use `java.sql.Date.valueOf(localDate)` se precisar do objeto legacy. Mas normalmente, `ps.setObject(index, localDate, Types.DATE)` já funciona.
  * `LocalDateTime` -> `java.sql.Timestamp`: use `Timestamp.valueOf(localDateTime)`. Ou `ps.setObject(index, localDateTime, Types.TIMESTAMP)`.
  * `Instant` -> `java.sql.Timestamp`: `Timestamp.from(instant)` converte Instant para Timestamp (cuidado com possível perda de nanos além de 6 casas, pois Timestamp suporta até 9 decimais e Instant até 9; geralmente ok). Ou simplesmente `ps.setObject(index, instant, Types.TIMESTAMP)`. Lembrando que se for coluna TIMESTAMP do MySQL, passar um Instant evita ambiguidades de fuso – o driver sabe que é um instante UTC.
  * `OffsetDateTime` -> armazenar: não há tipo nativo no MySQL que guarda offset. Opções: guardar em TIMESTAMP (perde o offset original, normaliza para UTC) ou guardar em VARCHAR separado, ou usar duas colunas (um TIMESTAMP UTC + uma coluna offset/zone). Muitas aplicações simplesmente convertem `OffsetDateTime` para Instant UTC antes de salvar, já que o offset original muitas vezes não importa se o objetivo é instante global.

* **Entre classes legadas e java.time (dentro da aplicação):**

  * `java.util.Date` -> `Instant`: método `date.toInstant()` (disponível em Java 8). De Instant você pode ir para `LocalDateTime` ou outro.
  * `Instant` -> `java.util.Date`: `Date.from(instant)`.
  * `java.util.Date` -> `LocalDateTime`: como mostrado, `LocalDateTime.ofInstant(date.toInstant(), zoneId)`. Se você precisar interpretar o Date em um fuso específico, passe o ZoneId desejado (senão use default).
  * `java.sql.Timestamp` -> `Instant`: `timestamp.toInstant()`. (Desde Java 8, `Timestamp` herda `Date`, então também tem `toInstant()`).
  * `java.sql.Timestamp` -> `LocalDateTime`: `timestamp.toLocalDateTime()`.
  * `LocalDateTime` -> `java.sql.Timestamp`: `Timestamp.valueOf(localDateTime)`.
  * `LocalDate` -> `java.sql.Date`: `java.sql.Date.valueOf(localDate)`.
  * `LocalTime` -> `java.sql.Time`: `java.sql.Time.valueOf(localTime)`. (Analogamente possui `.toLocalTime()`).

Exemplo final ilustrando algumas conversões numa função hipotética:

```java
public void demonstrarConversoes(ResultSet rs) throws SQLException {
    // Supomos que rs tem colunas: data_sql (DATE), ts_sql (TIMESTAMP)
    java.sql.Date sqlDate = rs.getDate("data_sql");
    java.sql.Timestamp sqlTs = rs.getTimestamp("ts_sql");
    
    // 1. JDBC legados -> java.time
    LocalDate localDate = sqlDate.toLocalDate();
    LocalDateTime localDateTime = sqlTs.toLocalDateTime();
    Instant instant = sqlTs.toInstant();
    
    // 2. java.time -> legados
    java.sql.Date novoSqlDate = java.sql.Date.valueOf(localDate.plusDays(1));
    java.sql.Timestamp novoSqlTs = java.sql.Timestamp.from(instant.plusSeconds(60));
    
    // 3. Util Date <-> Instant/LocalDateTime
    Date utilDate = new Date();
    Instant inst = utilDate.toInstant();
    Date utilDate2 = Date.from(inst);
    LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneId.of("America/Sao_Paulo"));
    
    // Print para demonstrar (assumindo toString padrão):
    System.out.println("SQL Date: " + sqlDate + " -> LocalDate: " + localDate);
    System.out.println("SQL Timestamp: " + sqlTs + " -> LocalDateTime: " + localDateTime);
    System.out.println("SQL Timestamp as Instant -> " + instant);
    System.out.println("Back to SQL types: " + novoSqlDate + ", " + novoSqlTs);
    System.out.println("UtilDate -> Instant -> utilDate: " + utilDate + " -> " + inst + " -> " + utilDate2);
    System.out.println("Instant in Sao_Paulo -> " + ldt);
}
```

Esse código hipotético mostraria as equivalências. Por exemplo, se `sqlDate` era `2025-08-02` exibido, `localDate` será `2025-08-02`. Se `sqlTs` era `2025-08-02 17:39:34` (no banco, tipo TIMESTAMP), `localDateTime` provavelmente será igual `2025-08-02T17:39:34` e `instant` seria algo como `2025-08-02T20:39:34Z` se o Timestamp foi interpretado no fuso -03:00 (Brasil). Note que, dependendo do driver e do fuso da JVM, `sqlTs.toInstant()` já considera offset; como dito, no MySQL o Timestamp é entregue já como horário local da JVM, então `toInstant()` aplicará o offset da JVM sobre ele resultando no instante UTC.

**Resumindo conversões e JDBC 4.2:** A especificação JDBC 4.2 formalizou suporte para APIs Java 8 Date/Time. Assim, métodos genéricos `setObject` e `getObject` podem ser usados com as classes do pacote `java.time` diretamente. Ao usar Spring JDBC, ele invocará esses métodos. Portanto, certifique-se de estar usando um driver JDBC compatível (por exemplo, MySQL Connector/J 8.x, PostgreSQL 42.x, Oracle JDBC recente, etc.). Se você tentar usar e obtiver uma exceção dizendo que não sabe lidar com o tipo, é um indicativo para atualizar o driver ou especificar Types manualmente (como demonstrado).

## Armadilhas Comuns e Boas Práticas

Trabalhar com datas e horas é notoriamente desafiador. A seguir, listamos algumas armadilhas frequentes e como evitá-las ao usar Java 8 e MySQL:

* **Confundir instantes absolutos com datas locais:** Decida se uma informação representa um instante exato no timeline (por exemplo, "quando uma transação ocorreu no mundo real") ou um horário relativo/local (por exemplo, "reunião às 9:00 no horário local de São Paulo"). Use os tipos adequados: `Instant`/`OffsetDateTime`/`ZonedDateTime` para eventos absolutos, e `LocalDateTime` para eventos locais que não devem mudar com fuso. Muitos bugs acontecem por usar `LocalDateTime` para timestamps globais ou vice-versa. Lembre-se: `LocalDateTime` **não** contém fuso – comparar dois `LocalDateTime` de sistemas diferentes pode ser comparar maçãs com laranjas. Já `Instant` é diretamente comparável e ordenável globalmente.

* **Timezone do MySQL e da conexão descoordenados:** Conforme visto, o MySQL TIMESTAMP converte dados conforme o timezone da sessão. É boa prática fixar o timezone da conexão para UTC (ou o mesmo em toda a aplicação) para evitar surpresas. No JDBC URL do MySQL, especifique `serverTimezone=UTC` (e configure o server para UTC preferencialmente). Assim, um TIMESTAMP sempre armazenará/em UTC e retornará em UTC. Se precisar trabalhar em outro fuso, converta no aplicativo, mas mantenha o armazenamento consistente. Se não fizer isso, pode ocorrer de um mesmo valor ser interpretado diferentemente em contextos distintos (por exemplo, backups ou queries ad-hoc por administradores em outra timezone).

* **Uso inadvertido de `java.util.Date` em APIs modernas:** Às vezes bibliotecas ou métodos antigos ainda usam `java.util.Date`. Como visto, você pode convertê-los imediatamente para `java.time` (ex: `instant = date.toInstant()`) e daí em diante usar somente as classes novas. É tentador continuar usando Date out of habit, mas evite – até mesmo em código novo alguns devs criam `Date.from(Instant.now())` só para representar "agora", o que é desnecessário e retrógrado.

* **Mutabilidade e thread safety:** As classes java.time são imutáveis. Isso elimina problemas de concorrência comuns com `Calendar` e `DateFormat`. Porém, se você ver código antigo reutilizando instâncias de `Calendar` ou `SimpleDateFormat` em múltiplas threads, saiba que isso é bug. Solução: trocar por java.time (`LocalDateTime` e `DateTimeFormatter`, este último também thread-safe) ou sincronizar apropriadamente (não recomendado). Em suma, prefira sempre as novas classes e formatação via `java.time.format.DateTimeFormatter` (que tem instâncias pré-definidas para ISO, RFC\_1123, etc., ou permita customizar padrões).

* **Formatação e parsing inadequados:** Falando em `DateTimeFormatter`, evite misturar APIs – por exemplo, não use `SimpleDateFormat` em `LocalDateTime` (até dá para fazer convertendo para util Date, mas desnecessário). Use `DateTimeFormatter` para qualquer formatação ou parsing de strings de data/hora. Ele sabe lidar com `LocalDateTime`, `ZonedDateTime`, etc., e possui suporte a padrões e a estilos predefinidos. Além disso, é imutável e thread-safe, ao contrário de `SimpleDateFormat`.

* **Perda de informação ao usar tipos errados:** Exemplos: usar `java.sql.Date` (que zera o tempo) para algo que tinha componente de hora – você perderá a hora. Usar `java.sql.Timestamp` para algo que precisava reter fuso de origem – você perderá o offset. Ou, no lado do MySQL, usar TIMESTAMP para datas históricas antes de 1970 ou futuras pós-2038 – elas podem não ser representáveis corretamente. **Boa prática:** escolher o tipo de coluna adequado e o tipo Java correspondente, conforme tabela anterior, para não truncar ou estourar valores.

* **Comparações e cálculos incorretos:** Ao comparar duas datas ou timestamps, certifique-se de estar comparando coisas compatíveis. Ex: não compare `LocalDateTime` de fusos diferentes sem normalizar; se precisar, converta ambos para `Instant` antes (pois Instant representa em comum denominador). Para diferenças, use as classes apropriadas (`ChronoUnit.between`, `Duration` ou `Period` conforme o caso). Não tente calcular diferenças de datas manualmente contando milissegundos, pois fusos e horário de verão podem complicar.

* **Uso de APIs legadas no JDBC por hábito:** Por exemplo, muitas pessoas usam `preparedStatement.setTimestamp(...)` e `resultSet.getTimestamp(...)` por costume. No Java 8+, você pode usar `setObject(..., LocalDateTime)` e `getObject(..., LocalDateTime.class)`, o que torna o código mais claro e elimina a necessidade de conversão manual. A **boa prática** é abraçar as melhorias do JDBC 4.2: trabalhar diretamente com as classes java.time sempre que possível, deixando o driver fazer as conversões necessárias.

* **Ignorar nanosegundos:** O Java 8 trouxe nanosegundos, mas nem todos os bancos suportam 9 dígitos de precisão. O MySQL por padrão (até a v5.6) não armazenava nanos em TIME/DATETIME (só até segundos). A partir do MySQL 5.7+, colunas definidas como DATETIME(fractional\_seconds) podem guardar microssegundos (6 dígitos). O Java Timestamp guarda nanos, mas o MySQL TIMESTAMP guarda até microssegundos também. Fique atento se sua aplicação depende de nanos precisos – possivelmente essa precisão extra será truncada no banco. Boas práticas seriam: ou evitar confiar em nanos (usar millis como base) ou certificar-se de usar tipos de coluna com precisão suficiente e talvez normalizar/truncar as instâncias de Instant/LocalDateTime no envio.

* **`equals()` de java.time:** Saiba que classes como `LocalDateTime` não consideram timezone, então `LocalDateTime.now(ZoneOffset.UTC).equals(LocalDateTime.now(ZoneId.of("America/New_York"))` pode ser falso mesmo que representem o mesmo instante, porque um é 10:00 UTC e outro 05:00-04:00. Para comparar instantes, converta para Instant ou use `ZonedDateTime` normalizados no mesmo fuso. Só compare diretamente objetos do mesmo tipo e contexto.

Em aplicações reais, recomendamos:

* **Armazenar em UTC sempre que viável:** Isso simplifica bastante o raciocínio. Ou armazenar a informação de fuso separadamente se precisar reconstruir o contexto local original.
* **Usar classes java.time de mais alto nível possível:** Por exemplo, se você lida apenas com datas sem tempo, use LocalDate end-to-end. Se lida com instantes absolutos, use Instant. Evite usar `LocalDateTime` para tudo "só porque sim"; escolha conscientemente.
* **Documentar as escolhas de fuso no banco:** novos desenvolvedores devem saber se a coluna DATETIME está em hora local do usuário, ou se TIMESTAMP está sempre em UTC. Uma pequena nota no schema ou no código de acesso ajuda a evitar confusão futura.

## Exemplo de Código (Repository JDBC com Datas)

Por fim, vejamos um exemplo unindo tudo isso em um formato de código reusável, como seria em um repositório Spring JDBC (padrão de projeto). Suponha que temos uma classe de domínio `Evento`:

```java
public class Evento {
    private Long id;
    private String nome;
    private LocalDate dataEvento;         // mapeado para DATE
    private LocalDateTime momentoEvento;  // mapeado para DATETIME (ou TIMESTAMP, dependendo do design)
    // getters e setters...
}
```

E queremos implementar um repository para salvar e ler `Evento` do MySQL. Vamos usar DATETIME para `momento_evento` neste exemplo (pressupondo que armazenamos o horário local do evento). O repository ficaria:

```java
@Repository
public class EventoRepository {
    private final NamedParameterJdbcTemplate jdbc;

    // injeta via construtor
    public EventoRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // INSERT
    public void salvarEvento(Evento ev) {
        String sql = "INSERT INTO eventos (nome, data_evento, momento_evento) " +
                     "VALUES (:nome, :dataEvento, :momentoEvento)";
        // Podemos usar BeanPropertySqlParameterSource para mapear automaticamente por nome
        SqlParameterSource params = new BeanPropertySqlParameterSource(ev);
        jdbc.update(sql, params);
    }

    // SELECT por ID
    public Evento buscarPorId(Long id) {
        String sql = "SELECT id, nome, data_evento, momento_evento FROM eventos WHERE id = :id";
        return jdbc.queryForObject(sql, 
            new MapSqlParameterSource("id", id),
            (rs, rowNum) -> {
                Evento ev = new Evento();
                ev.setId(rs.getLong("id"));
                ev.setNome(rs.getString("nome"));
                // MySQL driver: data_evento -> Date -> LocalDate
                ev.setDataEvento(rs.getDate("data_evento").toLocalDate());
                // momento_evento -> Timestamp -> LocalDateTime
                ev.setMomentoEvento(rs.getTimestamp("momento_evento").toLocalDateTime());
                return ev;
            }
        );
    }

    // UPDATE momento_evento de um evento
    public void reagendarEvento(Long id, LocalDateTime novoMomento) {
        String sql = "UPDATE eventos SET momento_evento = :momento WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("momento", novoMomento)
            .addValue("id", id);
        jdbc.update(sql, params);
    }

    // LISTAR eventos em um dado intervalo de datas
    public List<Evento> listarEventosEntre(LocalDate dataInicio, LocalDate dataFim) {
        String sql = "SELECT * FROM eventos WHERE data_evento BETWEEN :inicio AND :fim";
        Map<String,Object> params = Map.of("inicio", dataInicio, "fim", dataFim);
        return jdbc.query(sql, params, new BeanPropertyRowMapper<>(Evento.class));
        // Observação: BeanPropertyRowMapper vai tentar mapear colunas para propriedades
        // "dataEvento" e "momentoEvento". Ele suportará LocalDate/LocalDateTime?
        // Sim, a partir do Spring 5, o BeanPropertyRowMapper utiliza ConversionService
        // que conhece conversões padrão, incluindo de java.sql.Date -> LocalDate etc.
        // Caso não funcionasse automaticamente, seria necessário um RowMapper manual como acima.
    }
}
```

Alguns pontos sobre o código acima:

* Usamos `BeanPropertySqlParameterSource` no insert para mapear propriedades do objeto `Evento` para parâmetros nomeados (assume que os nomes dos campos na query coincidem com os nomes das propriedades do objeto). Isso torna o código enxuto. O Spring cuidará de converter o `LocalDate` e `LocalDateTime` para os tipos SQL corretos. Se houver algum problema de driver não suportado, poderíamos usar `.addValue("dataEvento", ev.getDataEvento(), Types.DATE)` etc., mas em drivers modernos não é necessário.
* No método `buscarPorId`, fizemos manualmente o mapping. Poderíamos também usar `BeanPropertyRowMapper.newInstance(Evento.class)`, já que os nomes das colunas são iguais às propriedades. O Spring 5+ provavelmente converterá `data_evento` em LocalDate automaticamente usando o `ConversionService` interno. Mas, para fins educativos, mostramos como seria manualmente com `rs.getDate().toLocalDate()`.
* Em `reagendarEvento`, passamos um `LocalDateTime` para o update e isso será mapeado via setObject -> TIMESTAMP.
* Em `listarEventosEntre`, usamos `Map` e o `BeanPropertyRowMapper`. Notar: o `BeanPropertyRowMapper` vai tentar instanciar `Evento` e chamar setters. Ele suportará `LocalDate`? Sim, ele usa o `DefaultConversionService` do Spring que conhece conversões de `java.sql.Date` para `LocalDate` e `java.sql.Timestamp` para `LocalDateTime`. Internamente, ele pegará o valor JDBC (Date/Timestamp) e converterá para o tipo da propriedade se possível. Então esse one-liner deve funcionar. Caso contrário, faríamos como no `buscarPorId`.

Esse repositório cobre operações básicas. Em um cenário real, teríamos possivelmente try-catch para exceções, log, etc., mas focamos no manejo das datas.

## Referências

1. Oracle Java Tutorials – *Date Classes* (Java 8 API de datas)
2. Oracle Java Tutorials – *Date and Time Classes* (Java 8 API de data/hora)
3. Oracle Java Tutorials – *Time Zone and Offset* (explicação de ZonedDateTime vs OffsetDateTime)
4. Oracle Java Tutorials – *Instant Class* (definição de Instant e epoch)
5. Oracle Java Tutorials – *Period and Duration* (diferença entre Period e Duration)
6. *Stack Overflow* – Diferenças entre `java.sql.Date`, `java.sql.Time` e `java.sql.Timestamp`
7. *Stack Overflow* – Recomendações para usar java.time vs Date (Ole V.V. e Basil Bourque)
8. *MySQL 8.0 Reference Manual* – Comportamento de TIMESTAMP vs DATETIME
9. Especificação JDBC 4.2 – Suporte a tipos java.time (`getObject`/`setObject`)
10. *Stack Overflow* – Erro de tipo não inferido para LocalDateTime no JDBC (exemplo de exceção)
11. *Stack Overflow* – Conversão entre `java.util.Date` e `java.time.LocalDateTime`
12. Documentação do Spring – Conversão automática de ResultSet para java.time (BeanPropertyRowMapper)

