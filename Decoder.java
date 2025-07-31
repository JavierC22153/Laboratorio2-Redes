public class Decoder {
    

    public String decoder_funct(String binaryString) {
        if (binaryString == null || binaryString.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder texto = new StringBuilder();
        String cleanBinary = binaryString.replace(" ", ""); // Remover espacios
        
        // Procesar cada grupo de 8 bits
        for (int i = 0; i < cleanBinary.length(); i += 8) {
            // Verificar que tengamos al menos 8 bits
            if (i + 8 <= cleanBinary.length()) {
                String byte8bits = cleanBinary.substring(i, i + 8);
                
                // Convertir de binario a entero y luego a char
                int valorAscii = Integer.parseInt(byte8bits, 2);
                char caracter = (char) valorAscii;
                texto.append(caracter);
            }
        }
        
        return texto.toString();
    }
    

    public String decoder_funct_safe(String binaryString) {
        if (binaryString == null || binaryString.trim().isEmpty()) {
            throw new IllegalArgumentException("El string binario no puede estar vacío");
        }
        
        StringBuilder texto = new StringBuilder();
        String cleanBinary = binaryString.replace(" ", "").replace("\n", "").replace("\t", "");
        
        // Verificar que solo contenga 0s y 1s
        if (!cleanBinary.matches("[01]+")) {
            throw new IllegalArgumentException("El string debe contener solo 0s y 1s");
        }
        
        // Verificar que la longitud sea múltiplo de 8
        if (cleanBinary.length() % 8 != 0) {
            System.out.println("Advertencia: La longitud no es múltiplo de 8. " +
                             "Bits restantes serán ignorados.");
        }
        
        // Procesar cada grupo de 8 bits
        for (int i = 0; i < cleanBinary.length(); i += 8) {
            if (i + 8 <= cleanBinary.length()) {
                String byte8bits = cleanBinary.substring(i, i + 8);
                
                try {
                    int valorAscii = Integer.parseInt(byte8bits, 2);
                    
                    // Verificar que esté en rango ASCII imprimible (opcional)
                    if (valorAscii >= 32 && valorAscii <= 126) {
                        char caracter = (char) valorAscii;
                        texto.append(caracter);
                    } else if (valorAscii == 10 || valorAscii == 13) {
                        // Permitir salto de línea y retorno de carro
                        char caracter = (char) valorAscii;
                        texto.append(caracter);
                    } else {
                        // Caracter no imprimible, mostrar como código
                        texto.append("[ASCII:" + valorAscii + "]");
                    }
                    
                } catch (NumberFormatException e) {
                    System.err.println("Error al convertir: " + byte8bits);
                }
            }
        }
        
        return texto.toString();
    }
    
   
    public String decoder_from_int_array(int[] binaryArray) {
        if (binaryArray == null || binaryArray.length == 0) {
            return "";
        }
        
        StringBuilder binaryString = new StringBuilder();
        for (int bit : binaryArray) {
            binaryString.append(bit);
        }
        
        return decoder_funct(binaryString.toString());
    }
    

    // public static void main(String[] args) {
    //     Decoder decoder = new Decoder();
    //     Coder coder = new Coder();
        
    //     System.out.println("=== Pruebas del Decoder ===");
        
    //     // Prueba 1: Texto simple
    //     String textoOriginal = "Hola";
    //     String binario = coder.coder_funct(textoOriginal);
    //     String textoDecodificado = decoder.decoder_funct(binario);
        
    //     System.out.println("Texto original: " + textoOriginal);
    //     System.out.println("Binario: " + binario);
    //     System.out.println("Texto decodificado: " + textoDecodificado);
    //     System.out.println("¿Son iguales? " + textoOriginal.equals(textoDecodificado));
    //     System.out.println();
        
    //     // Prueba 2: String binario sin espacios
    //     String binarioSinEspacios = "0100100001101111011011000110000100100000001000010110001001000011";
    //     System.out.println("Binario sin espacios: " + binarioSinEspacios);
    //     System.out.println("Decodificado: " + decoder.decoder_funct(binarioSinEspacios));
    //     System.out.println();
        
    //     // Prueba 3: Con tu ejemplo de datos decodificados [0,1,1,0,0,0,0,1]
    //     int[] datosHamming = {0,1,1,0,0,0,0,1};
    //     System.out.println("Datos de Hamming: " + java.util.Arrays.toString(datosHamming));
    //     System.out.println("Decodificado: '" + decoder.decoder_from_int_array(datosHamming) + "'");
    //     System.out.println("ASCII: " + Integer.parseInt("01100001", 2) + " = '" + (char)97 + "'");
    // }
}