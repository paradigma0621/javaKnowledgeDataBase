import java.lang.reflect.Field;

class Produto {
	
	private final Integer codigo = 12345;
	
	public Integer getCodigo() {
		return codigo;
	}
}

public class ReflexaoEx {  // Reflexão será usada para burlar o código

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Produto geladeira = new Produto();
		//Imprime código
		System.out.println(geladeira.getCodigo());  //12345
		
		try {
			//Obter o atributo (field)
			Field atributo = geladeira.getClass().getDeclaredField("codigo");
			
			//Alterar o modificador de acesso para o atributo se tornar acessível - uma vez que "codigo" é private
			atributo.setAccessible(true);
			
			//Atribuir um novo valor no atributo
			atributo.set(geladeira, 54321);
			
			//Imprime novo código			
			System.out.println(geladeira.getCodigo()); //54321
			
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
