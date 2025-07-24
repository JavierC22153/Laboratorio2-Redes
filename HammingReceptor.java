import java.util.Scanner;

public class HammingReceptor {
    
    
    public static int calcularSindrome(int p1, int p2, int d1, int p3, int d2, int d3, int d4) {
      
        int p1_esperado = d1 ^ d2 ^ d4;
        int p2_esperado = d1 ^ d3 ^ d4;
        int p3_esperado = d2 ^ d3 ^ d4;
        
        // Calcular síndrome
        int s1 = p1 ^ p1_esperado;
        int s2 = p2 ^ p2_esperado;
        int s3 = p3 ^ p3_esperado;
        
       
        return s1 + (s2 * 2) + (s3 * 4);
    }
    
   
    public static String hammingReceptor(String tramaRecibida) {
        if (tramaRecibida.length() != 7 || !tramaRecibida.matches("[01]+")) {
            throw new IllegalArgumentException("La trama debe tener exactamente 7 bits binarios.");
        }
        
       
        int p1 = Character.getNumericValue(tramaRecibida.charAt(0));
        int p2 = Character.getNumericValue(tramaRecibida.charAt(1));
        int d1 = Character.getNumericValue(tramaRecibida.charAt(2));
        int p3 = Character.getNumericValue(tramaRecibida.charAt(3));
        int d2 = Character.getNumericValue(tramaRecibida.charAt(4));
        int d3 = Character.getNumericValue(tramaRecibida.charAt(5));
        int d4 = Character.getNumericValue(tramaRecibida.charAt(6));
        
        
        int sindrome = calcularSindrome(p1, p2, d1, p3, d2, d3, d4);
        
        System.out.println("Trama recibida: " + tramaRecibida);
        System.out.println("Síndrome de error: " + sindrome);
        
        
        int[] bits = {p1, p2, d1, p3, d2, d3, d4};
        
        if (sindrome == 0) {
            System.out.println("✓ No se detectaron errores");
        } else {
            System.out.println("⚠ Error detectado en la posición: " + sindrome);
            
            bits[sindrome - 1] = 1 - bits[sindrome - 1];
            System.out.println("✓ Error corregido");
            
            
            String tramaCorregida = "";
            for (int bit : bits) {
                tramaCorregida += bit;
            }
            System.out.println("Trama corregida: " + tramaCorregida);
        }
        

        String mensajeCorregido = "" + bits[2] + bits[4] + bits[5] + bits[6]; // d1 d2 d3 d4
        
        return mensajeCorregido;
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Ingrese la trama recibida de 7 bits (ej. 1101001): ");
        String entrada = scanner.nextLine().trim();
        
        try {
            String mensajeDecodificado = hammingReceptor(entrada);
            
            System.out.println("\n--- Resultado del Receptor Hamming(7,4) ---");
            System.out.println("Mensaje decodificado (4 bits): " + mensajeDecodificado);
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        scanner.close();
    }
}