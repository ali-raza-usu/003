package apps;
import java.io.*;

public class Calculator {
	public static void main(String[] args) {
		
		Calculator calc = new Calculator();
		System.out.println("Which operation you want to perform." +
				"\n1. add\n2. subtract\n3. multiply\n4. divide\n ");
		String operation = readConsole();
		
		System.out.println("Enter first value : ");
		int val1 = Integer.parseInt(readConsole());
		
		System.out.println("Enter second value : ");
		int val2 = Integer.parseInt(readConsole());
		
		
		int result = 0; 
		
		switch(operation){
		case "1":
			result  = calc.add(val1, val2);
			break;
		
		case "2":
			result  = calc.subtract(val1, val2);
			break;
			
		case "3":
			result  = calc.multiply(val1, val2);
			break;
			
		case "4":
			result  = calc.div(val1, val2);
			break;
		}

		System.out.println("result of operation is " + result);
	}



	private static String readConsole()  {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String operation = null;
		try {
			operation = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return operation;
	}
	
	
	
	private int add(int v1, int v2){
		return v1+v2;
	}
	
	private int subtract(int v1, int v2){
		return v1-v2;
	}
	
	private int multiply(int v1, int v2){
		return v1 + v2;
	}
	
	private int div(int v1, int v2){
		return v1/v2;
	}
	

}
