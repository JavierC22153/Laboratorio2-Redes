def calcular_paridad(d1, d2, d3, d4):
    # Cálculo de bits de paridad
    p1 = d1 ^ d2 ^ d4
    p2 = d1 ^ d3 ^ d4
    p3 = d2 ^ d3 ^ d4
    return p1, p2, p3

def hamming_emisor(data_bits):
    if len(data_bits) != 4 or any(b not in '01' for b in data_bits):
        raise ValueError("El mensaje debe tener exactamente 4 bits binarios.")

    # Convertir a enteros
    d1 = int(data_bits[0])
    d2 = int(data_bits[1])
    d3 = int(data_bits[2])
    d4 = int(data_bits[3])

    # Calcular bits de paridad
    p1, p2, p3 = calcular_paridad(d1, d2, d3, d4)

    # Posiciones en la trama: p1 p2 d1 p3 d2 d3 d4
    trama = f"{p1}{p2}{d1}{p3}{d2}{d3}{d4}"
    return trama

if __name__ == "__main__":
    entrada = input("Ingrese un mensaje de 4 bits (ej. 1011): ").strip()

    try:
        trama = hamming_emisor(entrada)
        print("\n--- Resultado del Emisor Hamming(7,4) ---")
        print(f"Mensaje original: {entrada}")
        print(f"Trama codificada (7 bits): {trama}")
    except ValueError as e:
        print(f"Error: {e}")