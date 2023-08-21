        
	fica do formatted pra text block. Antar em AP também além daqui
	if(filtersOfUser.hasBusinessunits()) {
            parameter.addValue("business_ids", filtersOfUser.getBusinessunits());
            sql += """
                     AND (
                          buss.id IN (:business_ids) OR
                 ....                        (r.removed IS NULL OR r.removed = 0 )
                                               LIMIT 1
                                            )
                         )
                    """.formatted(WORKPLACE_TRANSFER_FOR_PERSON);
        }
--------------------------
Não sei se é do java 9, mas em src/main/java/com/nexti/operationdesk/service/filterofuser/HandleUserFiltersService.java temos:
    private FiltersOfUserDto returnFiltersOfUserDto() {

        filtersUserDto.forEach(filterUserDto -> {
            switch (filterUserDto.type()) {
                case CLIENT -> filters.addItem(filters.getClients(), filterUserDto.id());
                case AREA -> filters.addItem(filters.getAreas(), filterUserDto.id());
                case COMPANY -> filters.addItem(filters.getCompanys(), filterUserDto.id());
                case WORKPLACE -> filters.addItem(filters.getWorkplaces(), filterUserDto.id());
                case CLIENTTYPE -> filters.addItem(filters.getClientTypes(), filterUserDto.id());
                case BUSINESSUNIT -> filters.addItem(filters.getBusinessunits(), filterUserDto.id());
                case SERVICETYPE -> filters.addItem(filters.getServiceTypes(), filterUserDto.id());
            }
        });
		
		return filters;
    }
-------------------------
a funcionalidade de passar um Map groupVacanciesJobByWorkplaceId como argumento no exemplo abaixo:
Map<Long, Long> combinedResult = new HashMap<>(groupVacanciesJobByWorkplaceId);
para criar outro Map, funciona assim à partir de qual versão de java?
--------------------------- Resposta abaixo

Antes do Java 9, a criação de um novo `Map` com base em outro `Map` exigia a adição dos elementos manualmente usando um loop ou outras abordagens. Com o Java 9, foi introduzido um novo construtor no `HashMap` que aceita um `Map` como argumento e copia todos os elementos do `Map` fornecido para o novo `HashMap`.

Aqui está o exemplo de como funciona a partir do Java 9:

```java
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Crie um mapa original
        Map<Long, Long> groupVacanciesJobByWorkplaceId = new HashMap<>();
        groupVacanciesJobByWorkplaceId.put(1L, 100L);
        groupVacanciesJobByWorkplaceId.put(2L, 200L);
        groupVacanciesJobByWorkplaceId.put(3L, 300L);

        // Crie um novo mapa a partir do mapa original usando o construtor do Java 9
        Map<Long, Long> combinedResult = new HashMap<>(groupVacanciesJobByWorkplaceId);

        // Exiba o novo mapa criado
        System.out.println("Novo Mapa: " + combinedResult);
    }
}
```

A saída será:

```
Novo Mapa: {1=100, 2=200, 3=300}
```

Isso mostra que o novo mapa `combinedResult` foi criado com base no `groupVacanciesJobByWorkplaceId` original usando o construtor que aceita um `Map` como argumento.

Portanto, a partir do Java 9, você pode usar essa funcionalidade para criar um novo `Map` copiando todos os elementos de outro `Map`.
A funcionalidade de passar um `Map` como argumento ao criar outro `Map` usando o construtor `HashMap<>(Map)` está disponível desde o Java 9.

Antes do Java 9, a criação de um novo `Map` com base em outro `Map` exigia a adição dos elementos manualmente usando um loop ou outras abordagens. Com o Java 9, foi introduzido um novo construtor no `HashMap` que aceita um `Map` como argumento e copia todos os elementos do `Map` fornecido para o novo `HashMap`.

Aqui está o exemplo de como funciona a partir do Java 9:

```java
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Crie um mapa original
        Map<Long, Long> groupVacanciesJobByWorkplaceId = new HashMap<>();
        groupVacanciesJobByWorkplaceId.put(1L, 100L);
        groupVacanciesJobByWorkplaceId.put(2L, 200L);
        groupVacanciesJobByWorkplaceId.put(3L, 300L);

        // Crie um novo mapa a partir do mapa original usando o construtor do Java 9
        Map<Long, Long> combinedResult = new HashMap<>(groupVacanciesJobByWorkplaceId);

        // Exiba o novo mapa criado
        System.out.println("Novo Mapa: " + combinedResult);
    }
}
```

A saída será:

```
Novo Mapa: {1=100, 2=200, 3=300}
```

Isso mostra que o novo mapa `combinedResult` foi criado com base no `groupVacanciesJobByWorkplaceId` original usando o construtor que aceita um `Map` como argumento.

Portanto, a partir do Java 9, você pode usar essa funcionalidade para criar um novo `Map` copiando todos os elementos de outro `Map`.



---------------------------------------------------------- Mais coisa para falar abaixo

O mesmo se aplica para List e Set?

Sim, a partir do Java 9, você também pode usar a mesma funcionalidade para criar cópias de `List` e `Set` usando o construtor de cópia, desde que as implementações concretas dessas interfaces forneçam um construtor que aceite o mesmo tipo de coleção como argumento.

Aqui estão alguns exemplos de como criar cópias de `List` e `Set`:

Exemplo com `List`:

```java
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Crie uma lista original
        List<String> listaOriginal = new ArrayList<>();
        listaOriginal.add("Item 1");
        listaOriginal.add("Item 2");
        listaOriginal.add("Item 3");

        // Crie uma nova lista a partir da lista original usando o construtor do Java 9
        List<String> novaLista = new ArrayList<>(listaOriginal);

        // Exiba a nova lista criada
        System.out.println("Nova Lista: " + novaLista);
    }
}
```

Exemplo com `Set`:

```java
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // Crie um set original
        Set<Integer> setOriginal = new HashSet<>();
        setOriginal.add(1);
        setOriginal.add(2);
        setOriginal.add(3);

        // Crie um novo set a partir do set original usando o construtor do Java 9
        Set<Integer> novoSet = new HashSet<>(setOriginal);

        // Exiba o novo set criado
        System.out.println("Novo Set: " + novoSet);
    }
}
```

Ambos os exemplos funcionam da mesma maneira: criam uma nova coleção (lista ou set) com base na coleção original usando o construtor de cópia do Java 9. Com isso, você obtém uma cópia dos elementos da coleção original para a nova coleção.

Portanto, a funcionalidade de criar cópias de coleções usando o construtor de cópia está disponível para `Map`, `List` e `Set` a partir do Java 9, desde que a implementação concreta da coleção forneça o construtor adequado.




