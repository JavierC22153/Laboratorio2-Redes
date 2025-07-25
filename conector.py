import os
import socket
import random
import sys
from dotenv import load_dotenv
from emisorhamming import hamming_codificar

# ========================
# Cargar configuración desde .env
# ========================
load_dotenv()
HOST = os.getenv("HOST", "localhost")
HAMMING_PORT = int(os.getenv("HAMMING_PORT", 8000))
CRC_PORT = int(os.getenv("CRC_PORT", 9000))
PAYLOAD = os.getenv("PAYLOAD", "Hola Mundo desde Python")
PROB_ERROR = float(os.getenv("ERROR_PROBABILITY", "0.01"))

# ========================
# Utilidades
# ========================
def codificar_ascii_binario(mensaje):
    return ''.join([format(ord(c), '08b') for c in mensaje])

def aplicar_ruido(bits, probabilidad):
    return ''.join(
        '1' if bit == '0' and random.random() < probabilidad
        else '0' if bit == '1' and random.random() < probabilidad
        else bit
        for bit in bits
    )

def crc_codificar(data):
    poly = '10011'
    data_padded = data + '0000'
    data_padded = list(data_padded)
    for i in range(len(data)):
        if data_padded[i] == '1':
            for j in range(len(poly)):
                data_padded[i + j] = str(int(data_padded[i + j] != poly[j]))
    crc = ''.join(data_padded[-4:])
    return data + crc

# ========================
# Modo Receptor (servidor)
# ========================
def modo_receptor(port):
    print("=== MODO RECEPTOR (HAMMING) ===")
    print(f"Escuchando en {HOST}:{port}...")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, port))
        s.listen()
        conn, addr = s.accept()
        with conn:
            print(f"Conexión entrante desde {addr}")
            data = conn.recv(4096)
            if data:
                bits = data.decode()
                print(f"Mensaje recibido:\n\"{bits}\"\n")
            else:
                print("No se recibió ningún mensaje.")

# ========================
# Modo Emisor (cliente)
# ========================
def modo_emisor(port, algoritmo):
    print(f"=== MODO EMISOR ({algoritmo.upper()}) ===")
    print(f"Conectando a {HOST}:{port}...")
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((HOST, port))
            
            # Codificar binario
            binario = codificar_ascii_binario(PAYLOAD)
            if algoritmo == "hamming":
                binario_codificado = hamming_codificar(binario)
            elif algoritmo == "crc":
                binario_codificado = crc_codificar(binario)
            else:
                raise ValueError("Algoritmo no soportado")

            # Aplicar ruido
            binario_con_ruido = aplicar_ruido(binario_codificado, PROB_ERROR)

            # Mostrar y enviar
            print("Mensaje original:", PAYLOAD)
            print("Binario codificado:", binario_codificado)
            print("Binario con ruido:", binario_con_ruido)

            s.sendall((binario_con_ruido + "\n").encode())
            print("Mensaje enviado con éxito.\n")
    except ConnectionRefusedError:
        print(f"Error: No se pudo conectar al puerto {port}. ¿Está corriendo el receptor?")

# ========================
# Entrada principal
# ========================
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python conector.py [hamming|crc]")
        sys.exit(1)

    modo = sys.argv[1].lower()

    if modo == "hamming":
        modo_receptor(HAMMING_PORT)
    elif modo == "crc":
        modo_emisor(CRC_PORT, "crc")
    elif modo == "hamming-emisor":
        modo_emisor(HAMMING_PORT, "hamming")
    else:
        print("Modo no válido. Use uno de los siguientes:")
        print("  hamming          → modo receptor (puerto 8000)")
        print("  hamming-emisor   → modo emisor (puerto 8000, Hamming)")
        print("  crc              → modo emisor (puerto 9000, CRC)")
