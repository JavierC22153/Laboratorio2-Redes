import math

def calcular_bits_paridad(m):
    """Calcula la cantidad de bits de paridad necesarios."""
    r = 0
    while (2**r) < (m + r + 1):
        r += 1
    return r

def insertar_paridad(bits, r):
    """Inserta espacios para los bits de paridad (posiciones 1,2,4,...)"""
    n = len(bits) + r
    resultado = []
    j = 0
    for i in range(1, n + 1):
        if i & (i - 1) == 0:
            resultado.append(0)
        else:
            resultado.append(bits[j])
            j += 1
    return resultado

def calcular_paridad_total(bits, r):
    """Calcula los bits de paridad para la trama"""
    n = len(bits)
    for i in range(r):
        pos = 2**i
        parity = 0
        for j in range(1, n + 1):
            if j & pos and j != pos:
                parity ^= bits[j - 1]
        bits[pos - 1] = parity
    return bits

def hamming_codificar(data_bits):
    """Codifica usando Hamming (n, m) dinámico"""
    bits = [int(b) for b in data_bits]
    m = len(bits)
    r = calcular_bits_paridad(m)
    
    # Insertar bits de paridad
    bits_con_paridad = insertar_paridad(bits, r)
    
    # Calcular bits de paridad
    trama = calcular_paridad_total(bits_con_paridad, r)
    
    return ''.join(map(str, trama))

# Ejemplo
if __name__ == "__main__":
    entrada = input("Ingrese cadena de bits: ").strip()
    codificado = hamming_codificar(entrada)
    print(f"Hamming codificado: {codificado}")
