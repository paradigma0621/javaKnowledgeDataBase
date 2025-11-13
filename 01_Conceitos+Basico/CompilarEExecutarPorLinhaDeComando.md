Prompt:
crie um manual do java 8 de como usando linha de comando compilar e executar uma classe que:
 1) Não faz uso de nenhuma lib externa, mas está locada num pacote 
 2) Está locada num pacote e faz uso de libs externas do javafx que estão no jar localizado na pasta "/libs"

Perfeito! Aqui está um **manual completo para Java 8**, usando **apenas linha de comando**, cobrindo os dois cenários que você pediu:

---

## ✅ Pré-requisitos

* **JDK 8 instalado**
* Terminal configurado com acesso aos comandos `javac` e `java`
* Estrutura de arquivos típica:

```
/projeto
│
├── /src
│   └── meuapp/
│       └── App.java
│
└── /libs
    └── algum-arquivo.jar   (usado no segundo caso)
```

---

## 📌 Caso 1: Classe com `package` mas **sem usar libs externas**

### 1. Exemplo de código

Arquivo: `src/meuapp/App.java`

```java
package meuapp;

public class App {
    public static void main(String[] args) {
        System.out.println("Olá do Java 8!");
    }
}
```

### 2. Compilar

```bash
cd projeto
javac -d out src/meuapp/App.java
```

> `-d out` → gera os `.class` dentro da pasta `out/meuapp`

### 3. Executar

```bash
cd out
java meuapp.App
```

---

## 📌 Caso 2: Classe com `package` **e** uso de libs externas (JARs)

Mesmo que o JavaFX esteja embutido no Java 8, você pediu o exemplo **como se a lib estivesse fora em `/libs`**. Aqui vai:

### 1. Estrutura do projeto

```
/projeto
├── /src/meuapp/App.java
├── /libs/javafx-controls.jar
└── /out
```

### 2. Exemplo de código

```java
package meuapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(new Scene(new Label("JavaFX via JAR externo")));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### 3. Compilar

```bash
javac -cp "libs/*" -d out src/meuapp/App.java
```

> `-cp "libs/*"` inclui todos os `.jar` dentro de `/libs`

### 4. Executar

```bash
cd out
java -cp ".:../libs/*" meuapp.App
```

> No Windows, substitua `:` por `;`:

```bash
java -cp ".;../libs/*" meuapp.App
```

---

## 🧠 Dica extra: criando o JAR final (opcional)

```bash
jar cfe app.jar meuapp.App meuapp/*.class
```

Depois execute com:

```bash
java -cp "app.jar:../libs/*" -jar app.jar
```


