import java.util.Arrays;

public class GeneralizedHammingDecoder {
    
    private int n; // longitud total de la trama (datos + paridad)
    private int m; // longitud de los datos
    private int r; // número de bits de paridad
    
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
    
    public DecodingResult decode(int[] receivedFrame) {
        if (receivedFrame.length != n) {
            throw new IllegalArgumentException(
                "La longitud de la trama debe ser " + n + " bits");
        }
        
        // Calcular síndrome
        int syndrome = calculateSyndrome(receivedFrame);
        
        // Analizar el síndrome
        if (syndrome == 0) {
            // No hay errores detectados
            return new DecodingResult(
                extractDataBits(receivedFrame),
                receivedFrame, // La trama original es la correcta
                ErrorType.NO_ERROR,
                -1,
                "No se detectaron errores"
            );
        } else {
            // El síndrome indica la posición del error
            int errorPosition = syndrome;
            
            if (errorPosition <= n) {
                // Error simple corregible
                int[] correctedFrame = Arrays.copyOf(receivedFrame, receivedFrame.length);
                correctedFrame[errorPosition - 1] ^= 1; // Corregir el bit
                
                // Verificar que la corrección es válida calculando el nuevo síndrome
                int newSyndrome = calculateSyndrome(correctedFrame);
                
                if (newSyndrome == 0) {
                    return new DecodingResult(
                        extractDataBits(correctedFrame),
                        correctedFrame, // Devolver la trama corregida
                        ErrorType.SINGLE_ERROR_CORRECTED,
                        errorPosition,
                        "Error simple corregido en la posición " + errorPosition
                    );
                } else {
                    // Múltiples errores detectados - no se puede corregir con seguridad
                    return new DecodingResult(
                        extractDataBits(receivedFrame),
                        null, // No hay trama corregida disponible
                        ErrorType.MULTIPLE_ERRORS_DETECTED,
                        errorPosition,
                        "Múltiples errores detectados (síndrome: " + syndrome + ")"
                    );
                }
            } else {
                return new DecodingResult(
                    extractDataBits(receivedFrame),
                    null, // No hay trama corregida disponible
                    ErrorType.MULTIPLE_ERRORS_DETECTED,
                    errorPosition,
                    "Error detectado fuera del rango válido en posición " + errorPosition
                );
            }
        }
    }
    
    private int calculateSyndrome(int[] frame) {
        int syndrome = 0;
        
        // Para cada bit de paridad
        for (int i = 0; i < r; i++) {
            int parityBit = (int) Math.pow(2, i); // Posición del bit de paridad (1, 2, 4, 8, ...)
            int parity = 0;
            
            // Calcular paridad para todas las posiciones que tienen este bit de paridad
            for (int j = 1; j <= n; j++) {
                if ((j & parityBit) != 0) {
                    parity ^= frame[j - 1];
                }
            }
            
            // Si la paridad no es 0, hay error en este grupo
            if (parity != 0) {
                syndrome += parityBit;
            }
        }
        
        return syndrome;
    }
    
    // Extraer bits de datos (omitir bits de paridad)
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
    
    // Verificar si un número es potencia de 2
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    // Clase resultado
    public static class DecodingResult {
        private final int[] decodedData;
        private final int[] correctedFrame;
        private final ErrorType errorType;
        private final int errorPosition;
        private final String message;
        
        public DecodingResult(int[] decodedData, int[] correctedFrame, ErrorType errorType, 
                            int errorPosition, String message) {
            this.decodedData = decodedData;
            this.correctedFrame = correctedFrame;
            this.errorType = errorType;
            this.errorPosition = errorPosition;
            this.message = message;
        }
        
        public int[] getDecodedData() { return decodedData; }
        public int[] getCorrectedFrame() { return correctedFrame; }
        public ErrorType getErrorType() { return errorType; }
        public int getErrorPosition() { return errorPosition; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            String frameInfo = correctedFrame != null ? 
                "\nTrama corregida: " + Arrays.toString(correctedFrame) : "";
            
            return String.format(
                "Datos decodificados: %s\nTipo de error: %s\nPosición del error: %s\nMensaje: %s%s",
                Arrays.toString(decodedData),
                errorType,
                errorPosition == -1 ? "N/A" : errorPosition,
                message,
                frameInfo
            );
        }
    }
    
    // Enum para tipos de error
    public enum ErrorType {
        NO_ERROR,
        SINGLE_ERROR_CORRECTED,
        MULTIPLE_ERRORS_DETECTED
    }
    
    // Getters
    public int getN() { return n; }
    public int getM() { return m; }
    public int getR() { return r; }
    

}