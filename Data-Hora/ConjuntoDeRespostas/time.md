Perfeito. Vou reunir um conteúdo completo cobrindo:

* Todos os tipos e usos de data e hora do Java 8 (`java.time.*`)
* Como mapear `DATE`, `DATETIME`, `TIMESTAMP` do MySQL 8 para objetos como `LocalDate`, `LocalDateTime` e `Instant`
* Uso de `NamedParameterJdbcTemplate` para consultar, inserir e atualizar essas datas
* Conversões com `ResultSet` e boas práticas com fuso horário.

Vou começar a pesquisa e volto com o conteúdo detalhado para você em breve.


# Datas e Horários no Java 8 (java.time) e Integração com MySQL 8: Guia Completo

## Classes de Data e Hora do Java 8 (`java.time`)

Java 8 introduziu uma nova API de datas e horas no pacote `java.time` para resolver as deficiências das classes legadas (`java.util.Date`/`Calendar`). Essa API é **imutável**, **thread-safe** e muito mais intuitiva, separando claramente conceitos de datas/horas *locais* e *com fuso horário*. A seguir, detalhamos as principais classes, seus usos e funcionalidades:

### LocalDate (Data sem hora)

A classe `LocalDate` representa uma **data** (ano, mês, dia) **sem informação de hora ou fuso horário**. É útil para datas como aniversários, datas de vencimento, etc., em que não há componente de horário. Exemplos de uso:

* Obter a data atual: `LocalDate hoje = LocalDate.now();`
* Criar uma data específica: `LocalDate data = LocalDate.of(2025, 8, 2);`
* Analisar uma data no formato ISO-8601: `LocalDate data = LocalDate.parse("2025-08-02");`
* Manipular datas: métodos como `plusDays`, `minusMonths` etc., por exemplo `hoje.plusDays(1)` para amanhã.
* Consultar propriedades: `data.getDayOfWeek()` (dia da semana), `data.isLeapYear()` (ano bissexto).
* Comparar datas: `data1.isBefore(data2)` ou `isAfter`.
* Converter para início do dia em DateTime: `data.atStartOfDay()` produz um `LocalDateTime` no início daquela data.

### LocalTime (Hora do dia)

A classe `LocalTime` representa um **horário** (hora, minuto, segundo, nanos) **sem data ou fuso horário**, como um horário de relógio de parede. Exemplos:

* Hora atual: `LocalTime agora = LocalTime.now();`
* Criar horário específico: `LocalTime hora = LocalTime.of(14, 30, 00); // 14:30:00`
* Analisar string de horário: `LocalTime.parse("08:45:20")`.
* Adicionar/Subtrair: `agora.plusHours(2)`, `hora.minusMinutes(15)`.
* Consultar partes: `hora.getHour()`, `getMinute()`, etc. (também existem constantes como `LocalTime.MIDNIGHT`, `NOON`).
* Comparar horários: `t1.isBefore(t2)` ou `isAfter` para ver se um horário vem antes/depois de outro.

### LocalDateTime (Data e hora local)

A classe `LocalDateTime` representa **data e hora combinadas**, mas **sem fuso horário**. É a combinação de um `LocalDate` e um `LocalTime`. Use-a para modelar eventos no tempo local do sistema ou contexto do usuário, quando não for necessário considerar fusos. Exemplos:

* Data-hora atual: `LocalDateTime agora = LocalDateTime.now();`
* Criar específico: `LocalDateTime dt = LocalDateTime.of(2025, Month.AUGUST, 2, 15, 20);`
* Analisar string ISO: `LocalDateTime.parse("2025-08-02T15:20:00")`.
* Operações: `dt.plusDays(1)`, `dt.minusYears(1)`, `dt.plusHours(4)` etc..
* Obter partes: `dt.getMonth()`, `dt.getHour()` etc..
* Comparar: `dt1.isBefore(dt2)`/`isAfter` para verificar ordenação temporal.

**Conversões com LocalDateTime:** é fácil converter entre tipos locais:

* De `LocalDate` e `LocalTime` para `LocalDateTime`: `date.atTime(time)` ou `time.atDate(date)`.
* De `LocalDateTime` para `LocalDate`/`LocalTime`: métodos `toLocalDate()` e `toLocalTime()`.

### Instant (Instante no tempo UTC)

A classe `Instant` representa **um instante (timestamp) no tempo UTC**, equivalente a um ponto na linha do tempo (como um número de segundos/nanosegundos desde 1970-01-01T00:00:00Z). Use `Instant` para marcações de tempo absolutas (por exemplo, registro de log, timestamps globais). Exemplos:

* Instante atual (agora em UTC): `Instant agora = Instant.now();`
* Criar a partir de timestamp ISO ou epoch: `Instant.parse("2025-08-02T18:30:00Z")` ou `Instant.ofEpochMilli(1627900000000L)`.
* Converter para epoch segundos/millis: `instant.getEpochSecond()` ou `instant.toEpochMilli()`.
* Adicionar/Subtrair duração: `instant.plusSeconds(30)`, `instant.minus(5, ChronoUnit.MINUTES)`.

> **Nota:** `Instant` sempre representa o horário em UTC. Para exibir em outro fuso, é preciso convertê-lo para um tipo com fuso (por exemplo, `ZonedDateTime`). No Java, `Instant` é frequentemente usado para armazenar no banco (ou em JSON) porque independe de fuso e evita ambiguidades de horário.

### ZoneId e ZoneOffset (Fusos horários)

A classe `ZoneId` identifica uma região de fuso horário, usando uma base de dados de regras (por ex: `"America/Sao_Paulo"`, `"Europe/Paris"`). Já `ZoneOffset` representa um deslocamento fixo em horas/minutos em relação a UTC (por ex: `-03:00`). O `ZoneOffset` é uma subclasse de `ZoneId` para zonas fixas. Exemplos:

* Criar um ZoneId: `ZoneId sp = ZoneId.of("America/Sao_Paulo");`
* Obter fuso default do sistema: `ZoneId.of(ZoneId.systemDefault().getId())`.
* Listar fusos suportados: `ZoneId.getAvailableZoneIds()` (há dezenas de IDs disponíveis).
* Criar ZoneOffset: `ZoneOffset offset = ZoneOffset.of("-03:00");` (representando -3h em relação ao UTC).

**Diferença ZoneId vs ZoneOffset:** Um `ZoneId` de região (como "Europe/Paris") pode ter diferentes offsets ao longo do ano devido a horário de verão, enquanto um `ZoneOffset` (como +02:00) é apenas um número fixo de horas. Em outras palavras, `ZoneId` contém as regras de transição de fuso, e pode fornecer o `ZoneOffset` correto para um dado instante.

### ZonedDateTime (Data e hora com fuso)

A classe `ZonedDateTime` representa uma data e hora com um fuso horário específico, combinando um `LocalDateTime` com um `ZoneId` (e internamente um `ZoneOffset` válido para aquele instante). Por exemplo: `2019-12-20T10:15:30+05:00[Asia/Karachi]`. Use `ZonedDateTime` para eventos agendados em fusos específicos ou quando você precisa associar um momento a um local/fuso. Exemplos:

* Data-hora atual no fuso do sistema: `ZonedDateTime agora = ZonedDateTime.now();`
* Criar com base em fuso específico: `ZonedDateTime dtParis = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Paris"));`.
* Analisar string com fuso: `ZonedDateTime.parse("2025-08-02T15:20:00-03:00[America/Sao_Paulo]");`
* Converter Instant -> ZonedDateTime: `ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));` (ou outro fuso desejado).
* Alterar o fuso mantendo o instante: `dtParis.withZoneSameInstant(ZoneId.of("Asia/Tokyo"))` (converte o horário para o equivalente em Tokyo).
* Alterar o fuso mantendo campos locais: `dtParis.withZoneSameLocal(ZoneId.of("Asia/Tokyo"))` (cuidado: ajusta o offset mas mantém mesma data/hora local, podendo representar outro instante).
* Operações e getters similares a LocalDateTime (`plusHours`, `getYear`, etc.).

### OffsetDateTime (Data e hora com offset)

A classe `OffsetDateTime` é semelhante a `ZonedDateTime`, mas em vez de um `ZoneId` completo de região, carrega apenas um **offset** de UTC. É uma data-hora com deslocamento fixo, por exemplo `2019-12-03T10:15:30+01:00`. Use quando você precisa representar um momento com offset conhecido, porém sem querer lidar com regras de horário de verão. Exemplos:

* OffsetDateTime atual (no offset do sistema default): `OffsetDateTime agora = OffsetDateTime.now();`
* Criar a partir de LocalDateTime + offset: `OffsetDateTime odt = OffsetDateTime.of(localDateTime, ZoneOffset.of("+04:00"));`.
* Analisar string com offset: `OffsetDateTime.parse("2019-12-03T10:15:30+01:00");`
* Conversão: é fácil converter entre ZonedDateTime e OffsetDateTime (basta escolher o offset do ZonedDateTime ou associar um ZoneOffset). Ambos representam instantes absolutos (diferente de LocalDateTime).

**Resumo das diferenças entre os tipos principais:** LocalDate/LocalTime/LocalDateTime são **locais** (não possuem fuso nem representam instantes absolutos). `ZonedDateTime` e `OffsetDateTime` representam **instantes no tempo com informação de fuso/offset**, permitindo mapear para um ponto exato na linha do tempo. `Instant` representa diretamente um ponto na linha do tempo UTC (essencial para timestamps). Para determinar qual usar, considere o contexto do dado:

* Se for um **momento específico no tempo global** (por ex. horário de log, registro de evento que deve ser unívoco no timeline), use `Instant` ou `ZonedDateTime`/`OffsetDateTime` com UTC.
* Se for uma **data/hora localmente significativa** (ex: compromisso às 9h em São Paulo), você deve incluir o fuso para não perder o significado. Um compromisso às 9h SP não é o mesmo instante que 9h em Nova York – usar `ZonedDateTime` evita confusão. *Exemplo:* armazenar um agendamento de consulta como `Instant` (UTC) pode levar a problemas com horário de verão – após uma mudança de fuso, o mesmo horário local corresponde a outro Instant. Nesse caso, prefira guardar como `ZonedDateTime` (com ZoneId) ou `OffsetDateTime` adequado.
* Se for um valor apenas **local, sem referência global** (ex: data de nascimento, que independe de fuso), `LocalDate`/`LocalDateTime` é apropriado.

### Period e Duration (Diferenças entre datas e tempos)

Para lidar com diferenças ou intervalos, Java 8 oferece `Period` e `Duration`:

* **Period**: diferença baseada em **datas (anos, meses, dias)**. Útil para calcular idade, parcelas mensais, etc. Por exemplo, `Period.between(dataInicio, dataFim)` retorna anos, meses e dias entre duas datas. Você também pode somar/subtrair um Period a uma data (ex: `data.plus(Period.ofWeeks(2))`). O Period entende conceitos de calendário (meses variáveis, anos bissextos, etc.).
* **Duration**: diferença baseada em **tempo contínuo (dias, horas, minutos, segundos, nanos)**. Adequado para durações de horário ou timestamps. Exemplo: `Duration.between(t1, t2)` obtém segundos, minutos, horas entre dois instantes ou tempos. Também permite adicionar a um tempo: `hora.plus(Duration.ofHours(5))`. *Observação:* `Duration` pode medir dias de 24h exatas, diferentemente de Period que mede dias de calendário (que podem ter 23h ou 25h em DST).

Em resumo, use **Period** para diferenças em unidades de calendário (anos, meses, dias) e **Duration** para intervalos em unidades de tempo contínuo (horas, minutos, segundos). Por exemplo, adicionar 1 dia com Period respeita a transição de horário de verão (pula para mesma hora do dia seguinte, mesmo que a duração não seja exatamente 24h), enquanto 24 horas com Duration sempre adiciona exatamente 24h em termos de relógio.

### Conversões entre tipos de data/hora do Java 8

É comum converter entre esses tipos:

* **LocalDate <-> LocalDateTime**: `localDate.atTime(LocalTime)` produz um LocalDateTime no mesmo dia com o horário dado. Inversamente, `localDateTime.toLocalDate()` extrai a data.
* **LocalTime <-> LocalDateTime**: `localTime.atDate(LocalDate)` produz um LocalDateTime naquele dia. `localDateTime.toLocalTime()` extrai a hora.
* **LocalDateTime <-> ZonedDateTime**: adicione um fuso para obter um ZonedDateTime: `localDateTime.atZone(zoneId)` assume que a data/hora é referente àquele fuso e produz um ZonedDateTime. Remover o fuso: `zonedDateTime.toLocalDateTime()`.
* **ZonedDateTime <-> Instant**: `zdt.toInstant()` obtém o Instant correspondente (descartando a informação de fuso após aplicar offset). Para o inverso, **é necessário um ZoneId**: `Instant i = ...; ZonedDateTime zdt = i.atZone(zoneId);`. Sem o fuso não é possível criar uma data/hora local a partir de um Instant.
* **OffsetDateTime <-> Instant**: similar a ZonedDateTime, use `offsetDateTime.toInstant()` ou `Instant.atOffset(zoneOffset)`.
* **OffsetDateTime <-> ZonedDateTime**: perdem ou ganham a informação do fuso de região; por exemplo, `zonedDateTime.toOffsetDateTime()` mantém o mesmo offset atual. Já `offsetDateTime.atZoneSameInstant(zoneId)` converte para um fuso específico mantendo o mesmo instante (pode alterar o offset exibido).
* **Instant <-> LocalDateTime**: requer definir um fuso de referência. Ex: converter `Instant` para horário local do sistema: `LocalDateTime.ofInstant(instant, ZoneId.systemDefault())`. Para ir de LocalDateTime a Instant, forneça o fuso assumido: `localDateTime.atZone(zoneId).toInstant()`. *Cuidado:* converter LocalDateTime a Instant sem especificar fuso é inválido, pois LocalDateTime não representa um instante único até ser associado a um timezone.

Essas conversões permitem interoperar entre representações. Por exemplo, ao receber um `Instant` (UTC) do banco de dados, podemos transformá-lo em horário local do usuário para exibição usando `atZone(userZone)` ou `atOffset(offset)` conforme necessário.

### Formatação e Parsing com `DateTimeFormatter`

A API `java.time.format.DateTimeFormatter` facilita a conversão de datas/horas em texto (formatação) e vice-versa (parsing). As classes de data e hora possuem métodos:

* `parse(String, formatter)` ou métodos estáticos como `LocalDate.parse(str, formatter)` para criar objetos a partir de strings.
* `format(formatter)` para gerar uma string formatada a partir do objeto de data/hora.

Por exemplo, formatando um `LocalDateTime` em diferentes formatos:

```java
LocalDateTime dt = LocalDateTime.of(2025, 8, 2, 15, 45);
// Formato ISO local date (yyyy-MM-dd):
String isoDate = dt.format(DateTimeFormatter.ISO_DATE);             // "2025-08-02"
// Formato ISO datetime (yyyy-MM-ddTHH:mm:ss):
String isoDateTime = dt.format(DateTimeFormatter.ISO_DATE_TIME);    // "2025-08-02T15:45:00"
// Formato customizado:
DateTimeFormatter brFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String br = dt.format(brFormatter);  // "02/08/2025 15:45"
```

Há diversos formatters pré-definidos (ISO\_LOCAL\_DATE, ISO\_INSTANT, etc.) e é possível definir padrões customizados com `ofPattern`, inclusive ajustando Locale (p.ex. datas por extenso em português). O parsing é simétrico, e.g. `LocalDate.parse("02/08/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy"));`.

**Detalhe:** Para strings com informações de fuso (offset ou zone ID), use as classes correspondentes:

* `OffsetDateTime.parse("2025-08-02T15:45:00-03:00")` reconhecerá o offset e cria um OffsetDateTime.
* `ZonedDateTime.parse("2025-08-02T15:45:00-03:00[America/Sao_Paulo]")` interpretará inclusive o zone ID.

### Boas Práticas em Sistemas com Múltiplos Fusos Horários

Trabalhar com múltiplos fusos requer cuidado para evitar erros sutiles. Recomendações importantes:

* **Armazene instantes em UTC sempre que possível:** Em sistemas distribuídos, é comum armazenar horários em UTC no banco de dados e converter para o fuso do usuário apenas na apresentação. Isso garante consistência e facilita comparações. Por exemplo, um sistema de log deve salvar timestamps como `Instant` (ou `OffsetDateTime` em UTC) no BD; ao mostrar para o usuário, converta para o fuso local desejado. Esse padrão evita confusão e *offsets* duplos.
* **Inclua informação de fuso em eventos futuros/localizados:** Se você agenda eventos em diferentes fusos (como reuniões globais), guardar apenas a hora local e assumir um fuso padrão pode levar a problemas. O ideal é **salvar o fuso ou offset junto com a data/hora** (por exemplo, usar `ZonedDateTime` com zone ID) para que o evento possa ser convertido corretamente para UTC no momento certo. Caso contrário, mudanças de horário de verão podem deslocar o evento. *Exemplo:* um compromisso marcado para "2025-10-20 08:00 America/Sao\_Paulo" deve ser armazenado com esse fuso. Se fosse armazenado apenas como Instant (UTC), uma virada de DST poderia fazer o horário local esperado divergir.
* **Evite depender do fuso padrão do sistema:** Sempre que fizer conversões explícitas, passe o `ZoneId` desejado em vez de usar implicitamente o default. Fusos padrão podem diferir entre servidores (produção vs desenvolvimento) ou mudar se a configuração do SO mudar. Seja explícito usando métodos como `ZonedDateTime.now(ZoneId.of("UTC"))` ou definindo `ZoneId` em conversões (e.g. `Instant.now().atZone(ZoneId.of("America/New_York"))`).
* **Cuidado com API legada e conversões silenciosas:** Se você ainda precisar integrar com `java.util.Date` ou `Calendar`, converta-os usando métodos adequados (`Date.toInstant()` e `Date.from(instant)`). Evite misturar `TimeZone` de `Calendar` com os tipos novos. Também, não confunda `java.sql.Timestamp#toString()` com o valor real – o `toString()` de Timestamp converte para uma string no fuso local, o que pode dar a impressão errada. Prefira usar os tipos do pacote `java.time` e formatá-los com `DateTimeFormatter` controlando o fuso.
* **Period vs Duration nas operações corretas:** Lembre-se de usar `Period` para operações de calendário (adição de meses, anos, etc., que levam em conta variação de dias no mês) e `Duration` para horas/minutos fixos. Uma armadilha comum é adicionar "24 horas" a um horário para ir ao próximo dia – se ocorrer em um dia de mudança de fuso (DST), isso pode não resultar na mesma hora do dia seguinte. Em vez disso, use `LocalDate.plusDays(1)` ou `ZonedDateTime.plus(Period.ofDays(1))` para avançar um dia do calendário, ou use `with(TemporalAdjusters.next(DayOfWeek.X))` para pular para o próximo dia específico. Use `Duration` apenas quando quer realmente somar uma quantidade exata de tempo contínuo.
* **Nomes de fusos confiáveis:** Sempre que aceitar entrada de fuso horário do usuário, use identificadores completos (ex: `"America/Sao_Paulo"`) em vez de abreviações como "BST" ou "EST", que são ambíguas. Os IDs regionais garantem interpretação correta e futura (considerando mudanças governamentais).
* **Testes e offsets extremos:** Teste sua aplicação em cenários de fusos diferentes, inclusive offsets incomuns (±12h etc.) para ver se conversões funcionam. Tenha atenção especial a datas como horário de verão início/fim, onde certos horários podem não existir ou se repetir. A API Java.time possui classes como `ZoneRules` (via `ZoneId`) que podem ajudar a verificar esses detalhes se necessário.

## NamedParameterJdbcTemplate com MySQL 8 e Tipos de Data/Hora

O Spring Framework fornece a classe `NamedParameterJdbcTemplate` para facilitar operações JDBC usando **parâmetros nomeados** em vez de `?` posicionais. Veremos como utilizá-la para trabalhar com campos de data e hora no MySQL 8, aproveitando os tipos do Java 8 (`java.time`). O MySQL 8 (com Connector/J 8.x) possui suporte JDBC 4.2 para mapear diretamente os tipos de data/hora SQL para as classes Java 8 em muitas situações, simplificando bastante o código.

### SELECT: Mapeamento de campos DATE/DATETIME/TIMESTAMP para Java 8

Suponha uma tabela MySQL com colunas de data/hora, por exemplo:

```sql
CREATE TABLE evento (
    id INT,
    data_evento DATE,
    horario_evento TIME,
    timestamp_evento TIMESTAMP,
    datahora_evento DATETIME
);
```

Ao realizar um SELECT com `NamedParameterJdbcTemplate`, podemos mapear esses campos para objetos Java 8 facilmente:

```java
String sql = "SELECT data_evento, horario_evento, timestamp_evento, datahora_evento FROM evento WHERE id=:id";
Map<String, ?> params = Collections.singletonMap("id", 123);
Evento e = namedParamJdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
    LocalDate data = rs.getObject("data_evento", LocalDate.class);
    LocalTime horario = rs.getObject("horario_evento", LocalTime.class);
    Instant timestamp = rs.getObject("timestamp_evento", Instant.class);
    LocalDateTime dataHora = rs.getObject("datahora_evento", LocalDateTime.class);
    return new Evento(...); // construir objeto com esses valores
});
```

No exemplo acima, usamos o método `ResultSet.getObject(String, Class<T>)` disponível no JDBC 4.2+ para obter diretamente tipos Java 8. Isso exige que o driver JDBC suporte essa conversão, o que o **MySQL Connector/J 8** faz em grande parte:

* Coluna DATE -> `LocalDate` (ou `rs.getDate().toLocalDate()` se preferir)
* Coluna TIME -> `LocalTime` (`rs.getTime().toLocalTime()`)
* Coluna DATETIME -> `LocalDateTime` (por default, desde Connector/J 8.0.23 o driver retorna `LocalDateTime` para DATETIME em vez de `java.sql.Timestamp`)
* Coluna TIMESTAMP -> pode ser obtida como `Instant` ou `OffsetDateTime`. *Dica:* Por padrão, o driver pode mapear TIMESTAMP para `java.sql.Timestamp` ou `OffsetDateTime` dependendo do método usado. Mas você pode solicitar um `Instant` diretamente via `getObject(..., Instant.class)`. Segundo a especificação JDBC 4.2, os drivers são obrigados a suportar pelo menos `OffsetDateTime` para tipos de timestamp. De fato, o MySQL 8 mapeia TIMESTAMP para `OffsetDateTime` (ou `Timestamp`) por padrão; pedir `Instant` funciona desde que o driver seja recente, pois `Instant` é considerado um tipo "instantâneo" semelhante a `Timestamp`.

No exemplo usamos `Instant` para `TIMESTAMP` porque este tipo do MySQL armazena pontos instantes no tempo (em UTC por design). Assim, obter um Instant evita confusões de fuso. Você também poderia usar `OffsetDateTime` para reter um offset (que provavelmente seria igual ao do fuso da sessão do banco), mas normalmente Instant já basta pois representa o mesmo instante absoluto.

Se preferir usar **BeanPropertyRowMapper** ou outra estratégia automática do Spring JDBC, note que:

* Se a coluna for DATE e o campo no objeto for `LocalDate`, o Spring tentará converter o `java.sql.Date` retornado para `LocalDate`. Isso geralmente funciona pois `java.sql.Date` tem método `toLocalDate()`. Em versões modernas do Spring, conversões padrão para java.time são suportadas. Ainda assim, tome cuidado e teste para garantir que o mapeamento automático está funcionando conforme esperado.
* Para DATETIME/TIMESTAMP, muitos drivers retornavam `java.sql.Timestamp`. Se seu driver não mapear automaticamente para Java 8, o BeanPropertyRowMapper pode tentar converter Timestamp em LocalDateTime usando conversão de propriedade (Spring possui conversores internos). É mais garantido, porém, usar um RowMapper customizado (como acima) ou atualizar o driver para uma versão que suporte diretamente os tipos java.time.

**Dica:** Você pode configurar globalmente o comportamento do JdbcTemplate, mas por padrão ele seguirá o driver. Por exemplo, o MySQL Connector tem uma configuração `treatMysqlDatetimeAsTimestamp` (default *false*) que, se habilitada, faria o inverso – mapear DATETIME para Timestamp em vez de LocalDateTime. Entretanto, o padrão atual já é ideal (mantém DATETIME -> LocalDateTime). Portanto, geralmente não é necessário ajuste manual para MySQL 8; usar as classes Java 8 diretamente é o melhor caminho.

### INSERT e UPDATE: Persistindo datas e horas com Named Parameters

Para inserir ou atualizar colunas de data/hora usando `NamedParameterJdbcTemplate`, você pode simplesmente passar os objetos java.time como valores dos parâmetros. O Spring JDBC cuidará de chamar os métodos apropriados do JDBC (tipicamente `PreparedStatement.setObject`) e o driver converterá para o tipo SQL correto:

```java
String insertSql = "INSERT INTO evento (id, data_evento, timestamp_evento, datahora_evento) "
                 + "VALUES (:id, :data, :instante, :datahora)";
MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("id", 123)
    .addValue("data", LocalDate.of(2025, 8, 2))
    .addValue("instante", Instant.now())              // TIMESTAMP (UTC)
    .addValue("datahora", LocalDateTime.now());       // DATETIME
namedParamJdbcTemplate.update(insertSql, params);
```

No código acima:

* O campo DATE (`data_evento`) receberá um `LocalDate`. O driver JDBC converte internamente via `java.sql.Date.valueOf(localDate)`.
* O campo TIMESTAMP (`timestamp_evento`) recebe um `Instant`. Se o driver suportar, ele enviará como `TIMESTAMP` em UTC. Por segurança, você poderia usar `OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)` para deixar explícito que é UTC, já que a especificação JDBC exige suporte a OffsetDateTime nativamente. Mas muitos drivers aceitam `Instant` diretamente. De qualquer forma, o MySQL armazenará esse valor em UTC, convertendo da timezone da sessão para UTC (ver detalhes sobre fuso mais adiante).
* O campo DATETIME (`datahora_evento`) recebe um `LocalDateTime`. Como DATETIME não tem fuso, o driver envia os campos de data e hora “como estão”. Aqui é crucial entender: o MySQL Connector/J **presume que um `LocalDateTime` está no fuso da sessão** do banco ao inserir em um DATETIME. Ou seja, não haverá conversão de fuso nesse caso (diferente de TIMESTAMP).

Se preferir, você pode especificar explicitamente o tipo SQL para o parâmetro. Por exemplo, usando `SqlParameterSourceUtils.createBatch` ou passando um `SqlParameterValue` com tipo JDBC. Em geral, não é necessário se o driver for compatível com JDBC 4.2 e os objetos forem Java 8 (o driver identificará o tipo apropriado automaticamente). Porém, se você encontrar erro do tipo *"Cannot infer the SQL type for ... LocalDateTime"*, pode ser indicativo de driver antigo ou incompleto. Nesse caso:

* Certifique-se de usar MySQL Connector/J 8.0.**>** (e JDBC URL adequadamente configurado). Drivers antigos (JDBC 4.1 ou anteriores) não reconhecem `LocalDateTime` e exigem `java.sql.Timestamp`.
* Como solução alternativa, você pode converter manualmente antes de passar: por exemplo, `new java.sql.Timestamp(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli())` para TIMESTAMP em UTC, ou `Timestamp.valueOf(localDateTime)` que presume data/hora no fuso local e devolve Timestamp.
* Para um `LocalDate`, usar `java.sql.Date.valueOf(localDate)` faz a conversão para `java.sql.Date`.

Felizmente, com MySQL 8 isso raramente é necessário, pois ele suporta setObject com as classes do `java.time`. Um caso que pode requerer ajuste é o do **fuso da conexão** (discutido a seguir em detalhes de conversão).

### Extração direta do `ResultSet` (quando necessário)

Em alguns cenários avançados, talvez queiramos extrair dados de datas diretamente do `ResultSet`, fora do mecanismo do Spring (por exemplo, dentro de um loop de `JdbcTemplate.query` com `ResultSetExtractor`). Os métodos a usar são similares:

* `rs.getDate(col)` retorna `java.sql.Date` para DATE – converta via `.toLocalDate()`.
* `rs.getTime(col)` retorna `java.sql.Time` para TIME – converta via `.toLocalTime()`.
* `rs.getTimestamp(col)` retorna `java.sql.Timestamp` para DATETIME ou TIMESTAMP – você pode converter:

  * `ts.toLocalDateTime()` (Java 8) obtém um `LocalDateTime` equivalente (no **fuso local da JVM** no caso de Timestamp sem timezone).
  * `ts.toInstant()` obtém o `Instant` (considerando Timestamp como momento UTC?). **Cuidado:** Aqui há uma pegadinha: `Timestamp` é um instante mas **em milissegundos desde 1970 no *fuso local*** ou UTC? Na verdade, `java.sql.Timestamp` extende `java.util.Date`, que internamente armazena um instante UTC em milissegundos. Então `timestamp.toInstant()` efetivamente dá o Instant correto correspondente àquele Timestamp. Já `timestamp.toLocalDateTime()` dá a representação local desse mesmo instante no fuso padrão da JVM.
* `rs.getObject(col, LocalDateTime.class)` ou `LocalDate.class` etc., conforme já mostramos.

Em geral, usar `getObject(…, Class)` é mais limpo e evita ter que lidar com os tipos legacy. Lembre-se de que se o driver MySQL estiver configurado com `treatMysqlDatetimeAsTimestamp=true`, um `getObject` sem especificar a classe poderia te retornar um `Timestamp` mesmo para DATETIME. Mas, pedindo explicitamente a classe Java 8, você obtém o tipo desejado ou uma exceção se não suportado.

### Conversões corretas entre tipos SQL e Java 8

Vamos compilar as correspondências entre tipos do MySQL e tipos Java 8 recomendados, e salientar alguns detalhes de conversão:

* **DATE (SQL) ↔ LocalDate (Java):** Mapeamento direto de data calendário. Sem parte de hora ou fuso. Deve ser trivial, e o driver MySQL já converte `DATE` para `java.time.LocalDate` automaticamente. Caso receba como `java.sql.Date`, converta via `.toLocalDate()`.

* **TIME (SQL) ↔ LocalTime (Java):** Mapeamento direto de hora do dia. MySQL TIME não tem fuso nem data, representando HH\:MM\:SS. O driver pode mapear para `LocalTime` diretamente; caso contrário, `java.sql.Time.toLocalTime()` resolve.

* **DATETIME (SQL) ↔ LocalDateTime (Java):** O tipo DATETIME do MySQL armazena uma data e hora **"como lidas"**, sem fuso nem conversão interna. Isso corresponde exatamente ao conceito de `LocalDateTime`. Assim, use `LocalDateTime` para ler e escrever DATETIME. Por padrão o MySQL Connector/J 8 retorna DATETIME como `LocalDateTime` (já que considerá-lo um instant absoluto poderia ser incorreto). **Importante:** Ao enviar um `LocalDateTime` para uma coluna DATETIME, o driver **não** fará conversão de fuso – ele pegará os campos e armazenará. Portanto, se seu `LocalDateTime` foi criado no fuso do usuário e a base espera timezone diferente, você mesmo deve ajustar. Ex: se a aplicação roda em UTC mas você quer gravar um `LocalDateTime` como hora local de São Paulo em um DATETIME, você deveria criar esse `LocalDateTime` já no horário de SP (ou usar `ZonedDateTime` e então pegar `.toLocalDateTime()` antes de inserir). O conceito de DATETIME é essencialmente "relógio de parede", então trate-o como tal no código.

* **TIMESTAMP (SQL) ↔ Instant/OffsetDateTime/LocalDateTime (Java):** O tipo TIMESTAMP do MySQL *é armazenado em UTC internamente*, convertendo automaticamente da timezone da sessão do cliente para UTC ao salvar, e de UTC para timezone da sessão ao ler. Ou seja, TIMESTAMP representa um instante absoluto. Recomenda-se mapear para `Instant` ou `OffsetDateTime`:

  * *Leitura:* O driver MySQL 8, se você chamar `rs.getObject("col", OffsetDateTime.class)`, retornará um OffsetDateTime com offset igual ao fuso da sessão e valor correspondente ao instante correto. Se pedir `Instant.class`, idealmente deveria retornar o Instant UTC. Caso ele não suporte Instant diretamente, uma alternativa é pegar como `OffsetDateTime` e converter via `.toInstant()`. `rs.getTimestamp("col").toInstant()` também dá o Instant exato.
  * *Escrita:* Se você fornecer um `Instant` para uma coluna TIMESTAMP, o driver precisa saber o fuso da sessão para converter. Com MySQL Connector/J, a configuração padrão `preserveInstants=true` (a partir da 8.0.23) faz com que se você passar um tipo *instantâneo* (Instant, Timestamp, OffsetDateTime), ele **preserve o instante** ao enviar para TIMESTAMP. Já se você passar um `LocalDateTime` (não tem fuso, considerado "non-instant"), o driver *vai assumir que aquele DateTime está no timezone da sessão e converter para UTC*. Em outras palavras, inserir `LocalDateTime` em TIMESTAMP pode causar deslocamento se o timezone da JVM ou sessão for diferente do pretendido. **Boa prática:** envie sempre `Instant` ou `OffsetDateTime` (com offset conhecido) para colunas TIMESTAMP, ou garanta que `ZoneId.systemDefault()` (JVM) e `time_zone` da sessão MySQL sejam ambas UTC para não ter conversão indesejada.
  * *Exemplo de pitfall:* Se o aplicativo Java estiver em UTC+02:00 e o servidor MySQL em UTC+01:00 (diferença de 1h) e você fizer `insert into tabela (ts_col) values (?)` com um `LocalDateTime.now()` (digamos "2025-08-02T12:00"), o driver vai supor "2025-08-02T12:00+02:00" (fuso da JVM) e converter para UTC (10:00) para armazenar. Se a sessão do MySQL estava em UTC+01:00, o valor armazenado interno é 10:00 UTC. Ao ler de volta sem ajuste, você pode ter confusão. Em suma: **não use LocalDateTime para TIMESTAMP a menos que tenha certeza que não haverá diferença de fuso**. Prefira usar Instant ou configure `connectionTimeZone=UTC` no JDBC URL para neutralizar isso.

* **YEAR (SQL) ↔ `Year` ou `short`:** O MySQL YEAR (ano de 2 ou 4 dígitos) não tem um tipo específico em java.time, mas você pode tratá-lo como um número (Short) ou converter para `LocalDate` (ex.: year 2025 -> LocalDate.of(2025,1,1)). O driver geralmente mapeia YEAR para `java.sql.Date` com dia/mês padrão, ou para Short dependendo da configuração (`yearIsDateType`).

* **Campos de data/hora "zero" (0000-00-00):** MySQL permite datas inválidas como '0000-00-00' em colunas DATE/DATETIME como valor default. Por padrão, o driver lança exceção ao encontrar isso (`zeroDateTimeBehavior=EXCEPTION`). Se houver essa possibilidade, configure para CONVERT\_TO\_NULL ou trate a exceção. No Java 8, não há representação de "data zero", então normalmente você transformaria isso em null ou trataria como caso especial.

Resumindo, sempre procure alinhar o tipo Java com a semântica do tipo SQL:

* Use `LocalDate` ↔ DATE para datas puras.
* Use `LocalDateTime` ↔ DATETIME para data/hora sem fuso (relógio local).
* Use `Instant`/`OffsetDateTime` ↔ TIMESTAMP para instantes absolutos em timeline.
* Use `LocalTime` ↔ TIME para horários do dia.
* **Nunca descarte informação de fuso necessária:** se o dado tem significado de fuso, guarde-o (com OffsetDateTime ou campo separado). Se não tem, não introduza fuso arbitrariamente.

## Boas Práticas e Armadilhas Comuns (Java 8 + MySQL 8)

Por fim, listamos algumas boas práticas gerais e **pegadinhas comuns** ao lidar com datas/horas na integração Java–MySQL:

* **Mantenha o banco e a aplicação no mesmo fuso base (preferencialmente UTC):** Uma das melhores práticas é configurar a conexão JDBC para usar `UTC` como timezone da sessão do MySQL. Assim, ao usar tipos TIMESTAMP, não haverá conversões inesperadas – o que você envia como UTC será armazenado e lido em UTC. Por exemplo, `connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true` no URL JDBC força a sessão a UTC e elimina discrepâncias. Isso, combinado a utilizar `Instant`/`OffsetDateTime` no Java, garante que os valores no MySQL TIMESTAMP realmente representem o instante esperado.
* **Cuidado com o alcance de datas no MySQL:** O tipo TIMESTAMP no MySQL só representa datas até 19/01/2038 03:14:07 UTC (devido ao limite de 32 bits para epoch) e não aceita anos antes de 1970. Se sua aplicação precisa armazenar datas muito futuras ou passadas (ex: datas de nascimento antigas, agendamentos após 2038), **não use TIMESTAMP** – prefira DATETIME (que vai de ano 1000 a 9999). Nesses casos, você perde a conversão automática de fuso do TIMESTAMP, mas é melhor do que overflow ou erro. Lembre-se de então tratar DATETIME como hora local.
* **Precisão de sub-segundos:** MySQL por padrão armazena TIME/DATETIME/TIMESTAMP com precisão de segundos, mas versões recentes permitem até microssegundos (6 casas decimais) se especificado na definição da coluna. O `Instant` no Java tem nanos de precisão. Ao salvar, a parte de nanos acima de microssegundos será truncada. Isso raramente é um problema sério (diferenças de nanos não afetam ordenação), mas é bom estar ciente. Se você fizer comparações entre um Instant recém-criado e o valor retornado do banco, poderá ver diferenças se nanos foram descartados. Uma abordagem se precisão total for necessária é armazenar o nanosegundo separadamente ou usar um tipo numérico/BigDecimal (mas isso complica consultas).
* **Sempre use as classes do pacote `java.time`:** Evite usar `java.util.Date` ou `java.sql.Timestamp` diretamente no seu domínio. Além de serem mutáveis e menos intuitivas, seu uso pode levar a confusão de fuso. Converta-os o mais cedo possível para `Instant`/`LocalDateTime` etc. (por exemplo, ao receber um `java.sql.Timestamp` de alguma API legada, chame `toInstant()` e descarte o Timestamp). Isso facilita aplicar as ferramentas modernas de formatação e cálculo do Java 8.
* **Teste com dados reais de produção (especialmente fusos):** Uma cilada comum é desenvolver/testar tudo em um único fuso (ex: tudo em "America/Sao\_Paulo" ou tudo em UTC) e depois implantar globalmente e encontrar bugs. Teste cenários como: cliente insere dado em UTC-5, servidor do BD em UTC, outro cliente lê em UTC+9 – os valores fazem sentido? Se você seguir as práticas de armazenar em UTC e usar os tipos corretos, deverá funcionar bem. Utilize logs (e.g. logue Instants em milissegundos) para depurar possíveis discrepâncias.
* **Uso de `Calendar` no JDBC (evitar):** A API JDBC permite métodos legados como `ResultSet.getTimestamp(col, Calendar)`, que aplicam um Calendar específico para conversão. Isso era usado para ajustar timezones manualmente. Com java.time, isso raramente é necessário – é melhor obter um Instant e manipular via `ZonedDateTime` no fuso necessário. O uso de Calendar pode mascarar problemas de configuração de fuso na conexão. Prefira as abordagens já descritas em vez de passar Calendar para JDBC.
* **Tratamento de horário de verão duplicado/inexistente:** Em transições de DST, certos horários locais podem não existir (pulo) ou ocorrer duas vezes (atraso do relógio). Por exemplo, em São Paulo quando o relógio atrasava uma hora, o horário entre 00:00 e 00:59 repetia. Se sua lógica envolve essas datas (por ex., agendar algo exatamente na repetição), use `ZonedDateTime` – ele tem flags para indicar se o horário é antes ou depois da mudança e métodos para resolver ambiguidades (veja `ZonedDateTime.withLaterOffsetAtOverlap()` etc.). **Pitfall:** se usar `LocalDateTime` naquele intervalo e converter para Instant sem especificar qual offset (no overlap), o Java escolherá o offset padrão (geralmente o antigo ou o novo dependendo da impl.), o que pode levar a 1h de diferença. Portanto, para horários em transição de DST, sempre crie usando `ZonedDateTime` explícito.
* **Consistência entre Java e MySQL nas funções de data:** Lembre que funções do MySQL como `NOW()` retornam o timestamp no fuso da *sessão* do banco. Se você mantém a sessão em UTC, sem problemas. Mas se por algum motivo a sessão está em outro fuso, um `NOW()` no SQL e `Instant.now()` no Java podem não ser diretamente comparáveis. Uma opção segura é sempre definir `time_zone = '+00:00'` na sessão (pode ser automático com `connectionTimeZone`). Assim, `NOW()` e `CURRENT_TIMESTAMP` do MySQL geram valores UTC.
* **Atualize o driver e documente suposições:** Por fim, use versões atualizadas do Connector/J e do Spring. A evolução do driver trouxe melhorias (por exemplo, mapeamento nativo para LocalDateTime introduzido em versões 8.x). Documente no código/configuração se você está confiando em alguma propriedade (como `preserveInstants=true` padrão desde 8.0.23) para que futuros mantenedores entendam o contexto.

Seguindo essas práticas, você consegue tirar proveito máximo da API de datas do Java 8 integrada ao MySQL 8, com código mais claro, menos propenso a erros de fuso horário, e dados temporais armazenados de forma consistente e correta.

**Referências Utilizadas:** Java Docs e tutoriais sobre a API `java.time`, documentação do MySQL Connector/J e debates em comunidades técnicas que esclarecem detalhes de implementação, entre outras. Todas as fontes consultadas estão citadas ao longo do texto para aprofundamento.

