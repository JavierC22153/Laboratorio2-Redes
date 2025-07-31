def crc32_verificar(trama_bin):
    poly = 0x04C11DB7
    poly_len = 32

    data = int(trama_bin, 2)
    data_len = len(trama_bin)

    mask = 1 << (data_len - 1)

    for i in range(data_len - poly_len):
        if data & mask:
            data ^= poly << (data_len - poly_len - 1 - i)
        mask >>= 1

    return (data & ((1 << poly_len) - 1)) == 0

def binario_a_texto(bits):
    # Convierte string de bits a texto ASCII
    chars = []
    for i in range(0, len(bits), 8):
        byte = bits[i:i+8]
        if len(byte) < 8:
            break
        chars.append(chr(int(byte, 2)))
    return ''.join(chars)

# Función para procesar trama recibida
def procesar_trama(trama_bin):
    if len(trama_bin) < 33 or not all(c in '01' for c in trama_bin):
        raise ValueError("Trama inválida: debe contener solo 0 y 1 y tener al menos 33 bits.")

    if crc32_verificar(trama_bin):
        mensaje_bin = trama_bin[:-32]
        mensaje_texto = binario_a_texto(mensaje_bin)
        return True, mensaje_texto
    else:
        return False, None

# Opcional: modo prueba
if __name__ == "__main__":
    trama = input("Ingrese la trama recibida (mensaje + CRC de 32 bits): ").strip()
    try:
        valido, mensaje = procesar_trama(trama)
        if valido:
            print("Trama válida. Mensaje recibido:", mensaje)
        else:
            print("Error detectado en la trama. Se descarta el mensaje.")
    except ValueError as e:
        print("Error:", e)
