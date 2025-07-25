
import socket
import os
from dotenv import load_dotenv


load_dotenv()


HOST = os.getenv('HOST', '127.0.0.1')
CRC_PORT = int(os.getenv('CRC_PORT', '9000'))

def receptor_crc():
    """Receptor simple en puerto CRC_PORT para recibir del emisor Java"""
    print("=== RECEPTOR PYTHON SIMPLE ===")
    print(f"Escuchando en {HOST}:{CRC_PORT}")
    print("Esperando mensajes del emisor Java...")
    

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:

        s.bind((HOST, CRC_PORT))
        s.listen()
        print("Servidor Python iniciado, esperando conexiones...")
        

        conn, addr = s.accept()
        with conn:
            print(f"Conexión entrante del proceso {addr}")
            
            while True:

                data = conn.recv(1024)
                if not data:
                    break  # ya se recibió todo
                
                print(f"Recibido: {data!r}")
                
                # Intentar decodificar como texto
                try:
                    mensaje = data.decode('utf-8')
                    print(f"Mensaje: \"{mensaje}\"")
                except UnicodeDecodeError:
                    print("(No se pudo decodificar como texto UTF-8)")
                
                # TODO responder al emisor Java si es necesario
                # conn.sendall(b"Mensaje recibido OK")

if __name__ == "__main__":
    try:
        receptor_crc()
    except KeyboardInterrupt:
        print("\nServidor detenido por el usuario")
    except OSError as e:
        if e.errno == 48:  # Address already in use
            print(f"Error: Puerto {CRC_PORT} ya está en uso")
        else:
            print(f"Error de socket: {e}")
    except Exception as e:
        print(f"Error inesperado: {e}")