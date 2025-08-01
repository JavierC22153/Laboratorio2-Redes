import os
import socket
import random
import sys
from dotenv import load_dotenv
from emisorhamming import hamming_codificar
from receptorcrc32 import procesar_trama
import time

nombre_archivo = "salida.txt"



load_dotenv()
HOST = os.getenv("HOST", "localhost")
HAMMING_PORT = int(os.getenv("HAMMING_PORT", 8000))
CRC_PORT = int(os.getenv("CRC_PORT", 9000))
PROB_ERROR = float(os.getenv("ERROR_PROBABILITY", "0.01"))




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




def aplicar_ruido(bits, probabilidad):
    return ''.join(
        '1' if bit == '0' and random.random() < probabilidad
        else '0' if bit == '1' and random.random() < probabilidad
        else bit
        for bit in bits
    )




def leer_mensajes_archivo(ruta_archivo):
    """Lee mensajes desde un archivo de texto, uno por línea."""
    try:
        with open(ruta_archivo, 'r', encoding='utf-8') as archivo:
            mensajes = [linea.strip() for linea in archivo if linea.strip()]
        return mensajes
    except FileNotFoundError:
        print(f"Error: No se encontró el archivo '{ruta_archivo}'")
        return None
    except Exception as e:
        print(f"Error al leer el archivo: {e}")
        return None

def enviar_mensaje(mensaje, numero_mensaje=None):
    """Envía un mensaje individual usando Hamming."""
    binario = codificar_ascii_binario(mensaje)
    trama = hamming_codificar(binario)
    puerto = HAMMING_PORT
    
    trama_ruido = aplicar_ruido(trama, PROB_ERROR)
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((HOST, puerto))
            s.sendall(trama_ruido.encode())
        
        prefijo = f"Mensaje {numero_mensaje}: " if numero_mensaje else "Mensaje: "
        print(f"\n{prefijo}'{mensaje}' enviado correctamente.")
        print("Trama codificada:", trama)
        print("Trama con ruido:", trama_ruido)
        return True
    except ConnectionRefusedError:
        print(f"No se pudo conectar al receptor en el puerto {puerto}.")
        print("Asegúrate de que el receptor Hamming esté corriendo.")
        return False

def enviar_mensaje_conexion_persistente(socket_conn, mensaje, numero_mensaje=None):
    """Envía un mensaje usando una conexión existente."""
    binario = codificar_ascii_binario(mensaje)
    trama = hamming_codificar(binario)
    
    trama_ruido = aplicar_ruido(trama, PROB_ERROR)
    
    try:
        socket_conn.sendall(trama_ruido.encode())
        
        prefijo = f"Mensaje {numero_mensaje}: " if numero_mensaje else "Mensaje: "
        print(f"\n{prefijo}'{mensaje}' enviado correctamente.")
        print("Trama codificada:", trama)
        print("Trama con ruido:", trama_ruido)
        return True
    except Exception as e:
        print(f"Error al enviar mensaje: {e}")
        return False




def modo_emisor():
    print("=== MODO EMISOR (Sólo Hamming) ===")
    print("Opciones:")
    print("1. Ingresar mensaje manualmente")
    print("2. Cargar mensajes desde archivo de texto")
    print("3. Modo interactivo (múltiples mensajes en una conexión)")
    
    opcion = input("Seleccione una opción (1, 2 o 3): ").strip()
    
    if opcion == "1":
        
        mensaje = input("Ingrese el mensaje a enviar: ")
        enviar_mensaje(mensaje)
    
    elif opcion == "2":
        
        ruta_archivo = input("Ingrese la ruta del archivo de texto: ").strip()
        
        
        if ruta_archivo.startswith('"') and ruta_archivo.endswith('"'):
            ruta_archivo = ruta_archivo[1:-1]
        elif ruta_archivo.startswith("'") and ruta_archivo.endswith("'"):
            ruta_archivo = ruta_archivo[1:-1]
        
        mensajes = leer_mensajes_archivo(ruta_archivo)
        
        if mensajes is None:
            return
        
        if not mensajes:
            print("El archivo está vacío o no contiene mensajes válidos.")
            return
        
        print(f"\nSe encontraron {len(mensajes)} mensajes en el archivo.")
        print("¿Desea enviar todos los mensajes? (s/n): ", end="")
        confirmar = input().strip().lower()
        
        if confirmar not in ['s', 'si', 'sí', 'y', 'yes']:
            print("Operación cancelada.")
            return
        
        
        # print("¿Desea usar una sola conexión para todos los mensajes? (s/n): ", end="")
        # usar_conexion_unica = input().strip().lower()
        
        # if usar_conexion_unica in ['s', 'si', 'sí', 'y', 'yes']:
            
        #     enviar_mensajes_conexion_persistente(mensajes)
        # else:
            
        enviar_mensajes_conexiones_separadas(mensajes)
    
    elif opcion == "3":
        
        modo_interactivo()
    
    else:
        print("Opción no válida. Seleccione 1, 2 o 3.")

def enviar_mensajes_conexiones_separadas(mensajes):
    """Envía mensajes usando conexiones separadas para cada mensaje."""
    
    print("¿Desea establecer un intervalo entre envíos? (s/n): ", end="")
    usar_intervalo = input().strip().lower()
    
    intervalo = 0
    if usar_intervalo in ['s', 'si', 'sí', 'y', 'yes']:
        try:
            intervalo = float(input("Ingrese el intervalo en segundos (ej: 1.5): "))
        except ValueError:
            print("Intervalo inválido, se usará 0 segundos.")
            intervalo = 0
    
    
    exitosos = 0
    fallidos = 0
    
    for i, mensaje in enumerate(mensajes, 1):
        if enviar_mensaje(mensaje, i):
            exitosos += 1
        else:
            fallidos += 1
            break  
        
        
        if i < len(mensajes) and intervalo > 0:
            print(f"Esperando {intervalo} segundos...")
            time.sleep(intervalo)
    
    print(f"\n=== RESUMEN ===")
    print(f"Mensajes enviados exitosamente: {exitosos}")
    print(f"Mensajes fallidos: {fallidos}")
    print(f"Total de mensajes procesados: {exitosos + fallidos}")

def enviar_mensajes_conexion_persistente(mensajes):
    """Envía mensajes usando una sola conexión persistente."""
    puerto = HAMMING_PORT
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            print(f"Conectando al receptor en {HOST}:{puerto}...")
            s.connect((HOST, puerto))
            print("✅ Conexión establecida!")
            
            
            print("¿Desea establecer un intervalo entre envíos? (s/n): ", end="")
            usar_intervalo = input().strip().lower()
            
            intervalo = 0
            if usar_intervalo in ['s', 'si', 'sí', 'y', 'yes']:
                try:
                    intervalo = float(input("Ingrese el intervalo en segundos (ej: 1.5): "))
                except ValueError:
                    print("Intervalo inválido, se usará 0 segundos.")
                    intervalo = 0
            
            exitosos = 0
            fallidos = 0
            
            for i, mensaje in enumerate(mensajes, 1):
                if enviar_mensaje_conexion_persistente(s, mensaje, i):
                    exitosos += 1
                else:
                    fallidos += 1
                    break
                
                
                if i < len(mensajes) and intervalo > 0:
                    print(f"Esperando {intervalo} segundos...")
                    time.sleep(intervalo)
            
            print(f"\n=== RESUMEN ===")
            print(f"Mensajes enviados exitosamente: {exitosos}")
            print(f"Mensajes fallidos: {fallidos}")
            print(f"Total de mensajes procesados: {exitosos + fallidos}")
            
    except ConnectionRefusedError:
        print(f"No se pudo conectar al receptor en el puerto {puerto}.")
        print("Asegúrate de que el receptor esté corriendo.")
    except Exception as e:
        print(f"Error de conexión: {e}")

def modo_interactivo():
    """Modo interactivo para enviar múltiples mensajes en una conexión."""
    puerto = HAMMING_PORT
    
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            print(f"Conectando al receptor en {HOST}:{puerto}...")
            s.connect((HOST, puerto))
            print("✅ Conexión establecida!")
            print("Escriba sus mensajes (escriba 'salir' para terminar):")
            
            mensaje_contador = 0
            
            while True:
                mensaje = input("\nMensaje: ").strip()
                
                if mensaje.lower() in ['salir', 'exit', 'quit']:
                    print("Cerrando conexión...")
                    break
                
                if not mensaje:
                    print("Mensaje vacío, intente de nuevo.")
                    continue
                
                mensaje_contador += 1
                if enviar_mensaje_conexion_persistente(s, mensaje, mensaje_contador):
                    print("✅ Mensaje enviado correctamente.")
                else:
                    print("❌ Error al enviar mensaje.")
                    break
            
            print(f"\nTotal de mensajes enviados: {mensaje_contador}")
            
    except ConnectionRefusedError:
        print(f"No se pudo conectar al receptor en el puerto {puerto}.")
        print("Asegúrate de que el receptor esté corriendo.")
    except Exception as e:
        print(f"Error de conexión: {e}")




def modo_receptor():
    print("=== MODO RECEPTOR (Sólo CRC) ===")
    puerto = CRC_PORT
    
    print(f"Iniciando servidor en {HOST}:{puerto}...")
    print("Presione Ctrl+C para detener el servidor")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_socket.bind((HOST, puerto))
        server_socket.listen(5)
        
        try:
            while True:
                print(f"\nEsperando conexión en {HOST}:{puerto}...")
                conn, addr = server_socket.accept()
                
                print(f"Cliente conectado desde {addr}")
                print("Conexión establecida. Esperando mensajes...")
                
                mensaje_contador = 0
                
                try:
                    while True:  
                        try:
                            
                            conn.settimeout(30.0)  
                            data = conn.recv(4096)
                            
                            if not data:
                                print("Cliente cerró la conexión.")
                                break
                            
                            mensaje_contador += 1
                            trama_recibida = data.decode().strip()
                            
                            print(f"\n--- MENSAJE {mensaje_contador} ---")
                            print("Trama recibida:", trama_recibida)
                            
                            
                            valido, mensaje = procesar_trama(trama_recibida)
                            if valido:
                                print("✅ Mensaje recibido correctamente (CRC):", mensaje)
                                with open(nombre_archivo, 'a') as archivo:
                                    archivo.write(f"{mensaje}\n")


                            else:
                                with open(nombre_archivo, 'a') as archivo:
                                    archivo.write("0\n")
                                print("❌ Error detectado en la trama CRC.")
                            
                            print(f"--- FIN MENSAJE {mensaje_contador} ---")
                            print("Esperando siguiente mensaje...")
                            
                        except socket.timeout:
                            print("Timeout esperando mensaje. Conexión cerrada por inactividad.")
                            break
                        except ConnectionResetError:
                            print("Cliente desconectado inesperadamente.")
                            break
                        except Exception as e:
                            print(f"Error al recibir mensaje: {e}")
                            break
                            
                except Exception as e:
                    print(f"Error en la conexión con {addr}: {e}")
                finally:
                    conn.close()
                    print(f"Conexión con {addr} cerrada. Total mensajes recibidos: {mensaje_contador}")
                    
        except KeyboardInterrupt:
            print(f"\n\nServidor detenido por el usuario.")
        except Exception as e:
            print(f"Error del servidor: {e}")




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