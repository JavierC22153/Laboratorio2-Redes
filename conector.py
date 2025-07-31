import os
import socket
import random
import sys
from dotenv import load_dotenv
from emisorhamming import hamming_codificar
from receptorcrc32 import procesar_trama

# ========================
# Cargar configuración desde .env
# ========================
load_dotenv()
HOST = os.getenv("HOST", "localhost")
HAMMING_PORT = int(os.getenv("HAMMING_PORT", 8000))
CRC_PORT = int(os.getenv("CRC_PORT", 9000))
PROB_ERROR = float(os.getenv("ERROR_PROBABILITY", "0.01"))

# ========================
# CAPA PRESENTACIÓN
# ========================
def codificar_ascii_binario(mensaje):
    return ''.join(format(ord(c), '08b') for c in mensaje)

def decodificar_ascii_binario(bits):
    chars = []
    for i in range(0, len(bits), 8):
        byte = bits[i:i+8]
        if len(byte) < 8:
            break
        chars.append(chr(int(byte, 2)))
    return ''.join(chars)

# ========================
# CAPA RUIDO
# ========================
def aplicar_ruido(bits, probabilidad):
    return ''.join(
        '1' if bit == '0' and random.random() < probabilidad
        else '0' if bit == '1' and random.random() < probabilidad
        else bit
        for bit in bits
    )

# ========================
# MODO EMISOR
# ========================
def modo_emisor():
    print("=== MODO EMISOR (Sólo Hamming) ===")
    mensaje = input("Ingrese el mensaje a enviar: ")

    binario = codificar_ascii_binario(mensaje)
    trama = hamming_codificar(binario)
    puerto = HAMMING_PORT

    trama_ruido = aplicar_ruido(trama, PROB_ERROR)

    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((HOST, puerto))
            s.sendall(trama_ruido.encode())
        print(f"Mensaje enviado correctamente usando Hamming.")
        print("Mensaje original:", mensaje)
        print("Trama codificada:", trama)
        print("Trama con ruido:", trama_ruido)
    except ConnectionRefusedError:
        print(f"No se pudo conectar al receptor en el puerto {puerto}.")
        print("Asegúrate de que el receptor Hamming esté corriendo.")

# ========================
# MODO RECEPTOR
# ========================
def modo_receptor():
    print("=== MODO RECEPTOR (Sólo CRC) ===")
    puerto = CRC_PORT

    print(f"Escuchando en {HOST}:{puerto}...")

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, puerto))
        s.listen()
        conn, addr = s.accept()
        with conn:
            print(f"Conexión entrante desde {addr}")
            data = conn.recv(4096)
            if not data:
                print("No se recibió mensaje.")
                return

            trama_recibida = data.decode().strip()
            print("Trama recibida:", trama_recibida)

            valido, mensaje = procesar_trama(trama_recibida)
            if valido:
                print("Mensaje recibido correctamente (CRC):", mensaje)
            else:
                print("Error detectado en la trama CRC.")

# ========================
# MAIN
# ========================
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python conector.py [emisor|receptor]")
        sys.exit(1)

    modo = sys.argv[1].lower()

    if modo == "emisor":
        modo_emisor()
    elif modo == "receptor":
        modo_receptor()
    else:
        print("Modo no válido. Opciones: emisor | receptor")
