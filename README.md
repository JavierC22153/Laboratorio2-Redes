# Proyecto de Comunicación con Codificación y Detección de Errores

Este proyecto implementa un sistema de transmisión de datos entre emisor y receptor utilizando codificación de errores (Hamming y CRC32) y comunicación por sockets. Se proporciona una versión en Java y otra en Python.

---

## 📁 Archivos Requeridos

### Java
- `Connector.java`
- `Coder.java`
- `CRC32Emisor.java`
- `AnalizadorTramas.java`
- `GeneralizedHammingDecoder.java`
- `Decoder.java`
- `.env`

### Python
- `conector.py`
- `emisorhamming.py`
- `receptorcrc32.py`
- `.env`

---

## ⚙️ Compilación (Java)

En la terminal, ejecutar desde el directorio del proyecto:

```bash
javac *.java 
```
## Ejecución del Programa

- Modo Receptor (Java)
```bash
java Connector haming
```

- Modo Emisor (Java)
```bash
java Connector crc
```

- Modo Receptor (Python)
```bash
python conector.py receptor
```

- Modo Emisor (Python)
```bash
python conector.py emisor
```

## Estructura del .env

- `HOST=127.0.0.1`
- `HAMMING_PORT=8000`
- `CRC_PORT=9000`

