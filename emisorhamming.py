def calcular_paridad(d1, d2, d3, d4):
    p1 = d1 ^ d2 ^ d4
    p2 = d1 ^ d3 ^ d4
    p3 = d2 ^ d3 ^ d4
    return p1, p2, p3

def codificar_bloque_4bits(data_bits):
    d1, d2, d3, d4 = map(int, data_bits)
    p1, p2, p3 = calcular_paridad(d1, d2, d3, d4)
    return f"{p1}{p2}{d1}{p3}{d2}{d3}{d4}"

def hamming_codificar(data_binaria):
    bloques = [data_binaria[i:i+4] for i in range(0, len(data_binaria), 4)]
    resultado = ''
    for bloque in bloques:
        if len(bloque) < 4:
            bloque = bloque.ljust(4, '0')  # Rellenar con ceros si no es múltiplo de 4
        resultado += codificar_bloque_4bits(bloque)
    return resultado

# Solo para pruebas independientes
if __name__ == "__main__":
    entrada = input("Ingrese una cadena de bits (ej. 10110011): ").strip()
    try:
        codificado = hamming_codificar(entrada)
        print(f"Trama codificada completa: {codificado}")
    except ValueError as e:
        print(f"Error: {e}")
