Spring MVC é um **framework Java para criar aplicações web** (principalmente APIs e sites) seguindo o **padrão MVC (Model–View–Controller)**. Ele faz parte do **ecossistema Spring** e é um dos pilares do Spring para backend web.

---

## 🧠 O que significa MVC

MVC é uma **separação de responsabilidades**:

* **Model** → dados e regras de negócio
* **View** → o que o usuário vê (HTML, JSON, XML…)
* **Controller** → recebe a requisição, coordena tudo e devolve a resposta

Visualmente:

![Image](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/images/mvc.png)

![Image](https://cs-prod-assets-bucket.s3.ap-south-1.amazonaws.com/image_1_5f8f5d9a89.png)

![Image](https://images.openai.com/static-rsc-3/6G1FyRsmph7QvSyHwlmq01MjT1RnqUV8wNeboZhZ8PPhLfRKV9kSJhdCG6V94IP9bGnKmbefaAh4aq9lYn55BYWZ78QxewkuKXkgIS6EDLc?purpose=fullsize\&v=1)

---

## 🔧 O que o Spring MVC faz, na prática

Ele resolve **todo o encanamento web** para você:

* Recebe requisições HTTP (`GET`, `POST`, `PUT`, `DELETE`)
* Converte URL + parâmetros → métodos Java
* Converte JSON ↔ objetos Java automaticamente
* Gerencia status HTTP (`200`, `404`, `500`, etc.)
* Integra com validação, segurança, filtros, interceptors

Você só escreve **métodos Java**, o Spring cuida do resto.

---

## 📦 Componentes principais do Spring MVC

### 1️⃣ Controller

Classe que recebe requisições:

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

👉 Aqui:

* URL `/users/10`
* Método Java é chamado
* Retorno vira JSON automaticamente

---

### 2️⃣ DispatcherServlet (o cérebro)

Você **não escreve isso**, mas ele é o coração do Spring MVC.

Fluxo real:

![Image](https://miro.medium.com/v2/resize%3Afit%3A1200/0%2A1iuj1QRg0zBPrAc0.png)

![Image](https://docs.spring.io/spring-framework/docs/4.1.7.RELEASE/spring-framework-reference/html/images/mvc.png)

1. Request chega
2. `DispatcherServlet` intercepta
3. Escolhe o controller correto
4. Executa o método
5. Monta a resposta HTTP

---

### 3️⃣ Model

São seus objetos Java:

```java
public class User {
    private Long id;
    private String name;
}
```

No mundo moderno (APIs REST), o **Model geralmente vira JSON**.

---

### 4️⃣ View

Depende do tipo de aplicação:

* API REST → JSON (mais comum hoje)
* Aplicação web clássica → HTML (Thymeleaf, JSP)

Exemplo REST:

```http
GET /users/1

{
  "id": 1,
  "name": "Lucas"
}
```

---

## 🚀 Spring MVC vs Spring Boot

Isso confunde muita gente:

* **Spring MVC** → o framework web
* **Spring Boot** → facilita configurar e rodar o Spring MVC

Ou seja:

> Spring Boot **usa** Spring MVC por baixo

Sem Boot, você teria que configurar:

* Servlets
* Contextos
* Conversores
* JSON
* Mapeamentos

Com Boot:

```java
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

E pronto 😄

---

## 🧩 Quando você está usando Spring MVC (mesmo sem perceber)

Se você já escreveu:

* `@RestController`
* `@GetMapping`
* `@PostMapping`
* `@RequestBody`
* `@PathVariable`

👉 **Você já está usando Spring MVC**

---

## 📌 Em uma frase (nível entrevista)

> **Spring MVC é um framework do Spring que implementa o padrão MVC para aplicações web, facilitando o mapeamento de requisições HTTP para métodos Java e a construção de APIs e interfaces web.**
