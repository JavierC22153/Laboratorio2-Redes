import java.util.Arrays;

/**
 * Decodificador generalizado de códigos de Hamming
 * Para cualquier código (n,m) que cumpla (m + r + 1) <= 2^r
 * Puede corregir 1 error y detectar múltiples errores
 */
public class GeneralizedHammingDecoder {
    
    private int n; // longitud total de la trama (datos + paridad)
    private int m; // longitud de los datos
    private int r; // número de bits de paridad
    
    /**
     * Constructor del decodificador
     * @param n longitud total de la trama
     * @param m longitud de los datos
     */
    public GeneralizedHammingDecoder(int n, int m) {
        this.n = n;
        this.m = m;
        this.r = n - m;
        
        // Verificar que cumple la condición de Hamming
        if ((m + r + 1) > Math.pow(2, r)) {
            throw new IllegalArgumentException(
                "El código no cumple la condición de Hamming: (m + r + 1) <= 2^r");
        }
    }
    
    /**
     * Decodifica una trama recibida
     * @param receivedFrame trama recibida como array de bits
     * @return resultado de la decodificación
     */
    public DecodingResult decode(int[] receivedFrame) {
        if (receivedFrame.length != n) {
            throw new IllegalArgumentException(
                "La longitud de la trama debe ser " + n + " bits");
        }
        
        // Calcular el síndrome
        int syndrome = calculateSyndrome(receivedFrame);
        
        // Calcular paridad total para detectar errores múltiples
        int totalParity = calculateTotalParity(receivedFrame);
        
        // Analizar el síndrome y paridad total
        if (syndrome == 0 && totalParity == 0) {
            // No hay errores detectados
            return new DecodingResult(
                extractDataBits(receivedFrame),
                ErrorType.NO_ERROR,
                -1,
                "No se detectaron errores"
            );
        } else if (syndrome != 0 && totalParity != 0) {
            // Error simple - el síndrome indica la posición exacta
            int errorPosition = syndrome;
            
            if (errorPosition <= n) {
                // Error simple corregible
                int[] correctedFrame = Arrays.copyOf(receivedFrame, receivedFrame.length);
                correctedFrame[errorPosition - 1] ^= 1; // Corregir el bit
                
                return new DecodingResult(
                    extractDataBits(correctedFrame),
                    ErrorType.SINGLE_ERROR_CORRECTED,
                    errorPosition,
                    "Error simple corregido en la posición " + errorPosition
                );
            } else {
                return new DecodingResult(
                    extractDataBits(receivedFrame),
                    ErrorType.MULTIPLE_ERRORS_DETECTED,
                    errorPosition,
                    "Error detectado fuera del rango válido en posición " + errorPosition
                );
            }
        } else if (syndrome != 0 && totalParity == 0) {
            // Múltiples errores detectados (número par de errores)
            // Encontrar las posiciones de los errores comparando con patrones esperados
            int[] errorPositions = findMultipleErrorPositions(receivedFrame, syndrome);
            
            return new DecodingResult(
                extractDataBits(receivedFrame), // Devolver datos originales sin corregir
                ErrorType.MULTIPLE_ERRORS_DETECTED,
                errorPositions.length > 0 ? errorPositions[0] : syndrome,
                "Múltiples errores detectados. Primera posición encontrada: " + 
                (errorPositions.length > 0 ? errorPositions[0] : syndrome) + 
                " (síndrome: " + syndrome + ")"
            );
        } else {
            // syndrome == 0 && totalParity != 0 - Error en bit de paridad total
            return new DecodingResult(
                extractDataBits(receivedFrame),
                ErrorType.SINGLE_ERROR_CORRECTED,
                0, // Error en bit de paridad total (posición 0 conceptual)
                "Error detectado en bit de paridad total"
            );
        }
    }
    
    /**
     * Calcula la paridad total de toda la trama
     */
    private int calculateTotalParity(int[] frame) {
        int parity = 0;
        for (int bit : frame) {
            parity ^= bit;
        }
        return parity;
    }
    
    /**
     * Intenta encontrar las posiciones de errores múltiples
     * Esto es una aproximación ya que los errores múltiples no siempre
     * se pueden localizar exactamente con códigos de Hamming simples
     */
    private int[] findMultipleErrorPositions(int[] frame, int syndrome) {
        // Lista para almacenar posiciones de posibles errores
        java.util.List<Integer> errorPositions = new java.util.ArrayList<>();
        
        // Comparar bit por bit para encontrar discrepancias
        // Esto es una heurística básica
        int[] expectedFrame = generateExpectedFrame(frame);
        
        for (int i = 0; i < frame.length; i++) {
            if (frame[i] != expectedFrame[i]) {
                errorPositions.add(i + 1); // Posición basada en 1
            }
        }
        
        // Si no encontramos posiciones específicas, usar análisis del síndrome
        if (errorPositions.isEmpty()) {
            // El síndrome nos da una pista sobre dónde podrían estar los errores
            // En errores múltiples, esto es una aproximación
            if (syndrome <= n) {
                errorPositions.add(syndrome);
            }
            
            // Intentar encontrar una segunda posición basada en patrones comunes
            for (int i = 1; i <= n; i++) {
                if (i != syndrome) {
                    int testSyndrome = syndrome ^ i;
                    if (testSyndrome > 0 && testSyndrome <= n) {
                        errorPositions.add(i);
                        break;
                    }
                }
            }
        }
        
        return errorPositions.stream().mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * Genera una trama esperada basada en la reconstrucción de paridades
     * Esto es una aproximación para detectar errores múltiples
     */
    private int[] generateExpectedFrame(int[] frame) {
        // Esta es una implementación simplificada
        // En la práctica, necesitaríamos más información sobre la codificación original
        int[] expected = Arrays.copyOf(frame, frame.length);
        
        // Recalcular bits de paridad basados en los datos actuales
        for (int i = 0; i < r; i++) {
            int parityPosition = (int) Math.pow(2, i) - 1; // Convertir a índice base 0
            if (parityPosition < expected.length) {
                int parity = 0;
                for (int j = 0; j < n; j++) {
                    if (((j + 1) & (parityPosition + 1)) != 0) {
                        if (j != parityPosition) { // No incluir el bit de paridad en su propio cálculo
                            parity ^= frame[j];
                        }
                    }
                }
                expected[parityPosition] = parity;
            }
        }
        
        return expected;
    }
    private int calculateSyndrome(int[] frame) {
        int syndrome = 0;
        
        // Para cada bit de paridad
        for (int i = 0; i < r; i++) {
            int parityBit = (int) Math.pow(2, i); // Posición del bit de paridad (1, 2, 4, 8, ...)
            int parity = 0;
            
            // Calcular paridad para las posiciones correspondientes
            for (int j = 1; j <= n; j++) {
                if ((j & parityBit) != 0) {
                    parity ^= frame[j - 1];
                }
            }
            
            // Si hay error en esta paridad, agregar al síndrome
            if (parity != 0) {
                syndrome += parityBit;
            }
        }
        
        return syndrome;
    }
    
    /**
     * Extrae los bits de datos de la trama (sin los bits de paridad)
     */
    private int[] extractDataBits(int[] frame) {
        int[] dataBits = new int[m];
        int dataIndex = 0;
        
        for (int i = 1; i <= n; i++) {
            // Si la posición no es potencia de 2 (no es bit de paridad)
            if (!isPowerOfTwo(i)) {
                dataBits[dataIndex++] = frame[i - 1];
            }
        }
        
        return dataBits;
    }
    
    /**
     * Verifica si un número es potencia de 2
     */
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    /**
     * Clase para representar el resultado de la decodificación
     */
    public static class DecodingResult {
        private final int[] decodedData;
        private final ErrorType errorType;
        private final int errorPosition;
        private final String message;
        
        public DecodingResult(int[] decodedData, ErrorType errorType, 
                            int errorPosition, String message) {
            this.decodedData = decodedData;
            this.errorType = errorType;
            this.errorPosition = errorPosition;
            this.message = message;
        }
        
        public int[] getDecodedData() { return decodedData; }
        public ErrorType getErrorType() { return errorType; }
        public int getErrorPosition() { return errorPosition; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format(
                "Datos decodificados: %s\nTipo de error: %s\nPosición del error: %s\nMensaje: %s",
                Arrays.toString(decodedData),
                errorType,
                errorPosition == -1 ? "N/A" : errorPosition,
                message
            );
        }
    }
    
    /**
     * Enum para los tipos de error
     */
    public enum ErrorType {
        NO_ERROR,
        SINGLE_ERROR_CORRECTED,
        MULTIPLE_ERRORS_DETECTED
    }
    
    // Métodos auxiliares para obtener información del código
    public int getN() { return n; }
    public int getM() { return m; }
    public int getR() { return r; }
    
    /**
     * Método principal para probar la implementación
     */
    public static void main(String[] args) {
        // Ejemplo con código (7,4) - Hamming clásico
        GeneralizedHammingDecoder decoder = new GeneralizedHammingDecoder(7, 4);
        
        System.out.println("=== Pruebas del Decodificador de Hamming Generalizado ===");
        System.out.println("Código (7,4) con " + decoder.getR() + " bits de paridad\n");
        
        // Caso 1: Sin errores
        int[] noError = {1, 0, 1, 1, 0, 1, 0}; // Ejemplo de trama sin errores
        System.out.println("Caso 1 - Trama sin errores:");
        System.out.println("Entrada: " + Arrays.toString(noError));
        DecodingResult result1 = decoder.decode(noError);
        System.out.println(result1);
        System.out.println();
        
        // Caso 2: Error simple (posición 3)
        int[] singleError = {1, 0, 0, 1, 0, 1, 0}; // Error en posición 3
        System.out.println("Caso 2 - Error simple en posición 3:");
        System.out.println("Entrada: " + Arrays.toString(singleError));
        DecodingResult result2 = decoder.decode(singleError);
        System.out.println(result2);
        System.out.println();
        
        // Caso 3: Múltiples errores (errores en posiciones 1 y 2)
        int[] multipleErrors = {0, 1, 1, 1, 0, 1, 0}; // Errores en posiciones 1 y 2
        System.out.println("Caso 3 - Múltiples errores en posiciones 1 y 2:");
        System.out.println("Entrada: " + Arrays.toString(multipleErrors));
        DecodingResult result3 = decoder.decode(multipleErrors);
        System.out.println(result3);
        System.out.println();
        
        // Caso 4: Otro ejemplo de múltiples errores
        int[] multipleErrors2 = {1, 0, 0, 0, 0, 1, 0}; // Múltiples errores
        System.out.println("Caso 4 - Múltiples errores (patrón diferente):");
        System.out.println("Entrada: " + Arrays.toString(multipleErrors2));
        DecodingResult result4 = decoder.decode(multipleErrors2);
        System.out.println(result4);
    }
}