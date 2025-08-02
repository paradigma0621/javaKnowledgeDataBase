# ❓ Pergunta

Tenho o seguinte endpoint na minha controller:

```java
@PostMapping(value = "/dto")
public ResponseEntity<ResponseDto<HolidayDto>> createDto(@RequestBody HolidayRequestDto dto) {
	try {
		return serviceValidatorLayer.createDto(dto);
	} catch (BusinessException e) {
		logger.error(e.getMessage(), e);
		return new ResponseEntity<>(
				new ResponseDto<>(409L, "Não foi possível criar o registro."),
				HttpStatus.CONFLICT
		);
	} catch (Exception e) {
		logger.error(e);
		return new ResponseEntity<>(
				new ResponseDto<>(400L, "Não foi possível criar o registro."),
				HttpStatus.BAD_REQUEST
		);
	}
}
```

Na camada de serviço (`serviceValidatorLayer`), o método `createDto` está anotado com `@Validated`, e o DTO (`HolidayRequestDto`) contém validações como `@NotBlank` nos campos obrigatórios.

No entanto, quando envio uma requisição sem preencher um campo obrigatório (por exemplo, um campo com `@NotBlank`), a exceção **é capturada no bloco `try/catch` da controller**, e **não é redirecionada para o `GlobalExceptionHandler`**, que possui:

```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ResponseDto<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
	List<String> messageErrors = ex.getConstraintViolations()
			.stream()
			.map(message -> getPropertyPath(message) + use(convertMessageToConstant(message.getMessage())))
			.collect(Collectors.toList());

	ResponseDto<Object> responseDto = new ResponseDto<>(
			409L,
			use("IMPOSSIBLE_CREATE_REGISTER_COMMENTARY"),
			null,
			messageErrors
	);

	return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
}
```

### ❓ Dúvida

Por que a exceção de validação (como o `@NotBlank`) está sendo capturada dentro do `try/catch` da controller, mesmo com a configuração do `GlobalExceptionHandler` para tratar `ConstraintViolationException`?


# Resposta	
O comportamento que você está observando — onde o `@NotBlank` falha e a exceção é capturada diretamente no `try-catch` da `@PostMapping`, em vez de ser redirecionada para o `@ExceptionHandler` — está relacionado a como o Bean Validation funciona no Spring, e **em que momento** as validações ocorrem.

### Causa provável:

Você está validando o DTO no **service**, e **não na controller**. Isso faz com que a exceção do tipo `javax.validation.ConstraintViolationException` (normalmente lançada por validações em métodos de serviço com `@Validated`) seja capturada **dentro do seu próprio bloco `try/catch` da controller**, porque ela **ocorre depois** que o controller já pegou a requisição e entrou no método.

### Comportamento detalhado:

| Situação                                                                             | Tipo de validação                          | Tipo de exceção lançada           | Interceptada por                                           |
| ------------------------------------------------------------------------------------ | ------------------------------------------ | --------------------------------- | ---------------------------------------------------------- |
| Validação no `@RequestBody` (no controller) com `@Valid`                             | `@Valid` no parâmetro do método controller | `MethodArgumentNotValidException` | `@ExceptionHandler(MethodArgumentNotValidException.class)` |
| Validação em método do `@Service` com `@Validated` e `@NotBlank` no parâmetro do DTO | `@Validated` e `@NotBlank` em parâmetros   | `ConstraintViolationException`    | `@ExceptionHandler(ConstraintViolationException.class)`    |
| Se você captura com `try/catch` direto no controller                                 | qualquer exceção                           | seu `catch` local na controller   |                                                            |

### Portanto:

#### Cenário atual:

```java
@PostMapping(value = "/dto")
public ResponseEntity<ResponseDto<HolidayDto>> createDto(@RequestBody HolidayRequestDto dto) {
	try {
		// Aqui você chama um service anotado com @Validated
		return serviceValidatorLayer.createDto(dto); 
	} catch (BusinessException e) {
		// capturado aqui
	} catch (Exception e) {
		// capturado aqui
	}
}
```

Como a validação ocorre **dentro do service**, a exceção de `ConstraintViolationException` **não será capturada pelo seu `GlobalExceptionHandler`**, mas sim pelo `catch (Exception e)` da controller — **a não ser que você não a capture localmente**.

---

### ✅ Soluções possíveis:

#### ✔️ Melhor abordagem (recomendada):

Valide o DTO já na **controller**, com `@Valid` no parâmetro do método:

```java
@PostMapping(value = "/dto")
public ResponseEntity<ResponseDto<HolidayDto>> createDto(@Valid @RequestBody HolidayRequestDto dto) {
	// Remova o try/catch para deixar o GlobalExceptionHandler capturar
	return serviceValidatorLayer.createDto(dto);
}
```

E então, no `GlobalExceptionHandler`, capture:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ResponseDto<Object>> handleValidationException(MethodArgumentNotValidException ex) {
	List<String> messageErrors = ex.getBindingResult().getFieldErrors()
		.stream()
		.map(err -> err.getField() + ": " + err.getDefaultMessage())
		.collect(Collectors.toList());

	ResponseDto<Object> responseDto = new ResponseDto<>(
			409L,
			"Não foi possível criar o registro.",
			null,
			messageErrors
	);

	return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
}
```

#### ✔️ Alternativa (menos recomendada):

Se você realmente quer validar no service, **não capture `Exception` diretamente na controller**, e deixe a exceção propagar para o `GlobalExceptionHandler`.

---

### Conclusão:

* A validação com `@Validated` em service lança `ConstraintViolationException`, mas você está engolindo com `catch (Exception e)` na controller.
* Mova a validação para o controller com `@Valid`, e use um `@ExceptionHandler(MethodArgumentNotValidException.class)` para interceptar falhas.
* Evite capturar `Exception` diretamente, exceto em casos bem controlados.
