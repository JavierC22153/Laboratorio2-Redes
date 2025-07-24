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

    # Retorna True si no hay error (residuo = 0), False si hay error
    return (data & ((1 << poly_len) - 1)) == 0


def main():
    print("Receptor CRC-32 (Detección de errores)")
    trama = input("Ingrese la trama recibida (mensaje + CRC de 32 bits): ").strip()

    try:
        if not all(c in '01' for c in trama) or len(trama) < 33:
            raise ValueError("Trama inválida. Debe contener solo 0 y 1, y tener al menos 33 bits.")

        if crc32_verificar(trama):
            print("Trama válida. No se detectaron errores.")
            print("Mensaje recibido:", trama[:-32])
        else:
            print("Error detectado en la trama. Se descarta el mensaje.")
    except ValueError as e:
        print(e)


if __name__ == "__main__":
    main()