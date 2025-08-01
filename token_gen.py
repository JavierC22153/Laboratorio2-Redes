import random
import string
import binascii

def generate_crc_safe_tokens(count=10000, min_len=2, max_len=5):
    """
    Genera tokens ASCII para pruebas CRC con longitudes controladas.
    Excluye "0" como token individual para evitar conflictos con indicadores de error.
    
    Args:
        count: Número de tokens a generar
        min_len: Longitud mínima del token (caracteres)
        max_len: Longitud máxima del token (caracteres)
    
    Returns:
        Lista de tokens generados (sin "0" individual)
    """
    tokens = []
    # Solo letras y dígitos para mantener simplicidad
    chars = string.ascii_letters + string.digits
    
    for _ in range(count):
        while True:
            length = random.randint(min_len, max_len)
            token = ''.join(random.choices(chars, k=length))
            
            # Evitar "0" como token único (reservado para errores)
            if token != "0":
                tokens.append(token)
                break
    
    return tokens

def compute_crc32(token):
    """Calcula CRC32 de un token string."""
    return binascii.crc32(token.encode('utf-8')) & 0xffffffff

def create_crc_frame(token):
    """
    Crea trama binaria: datos + CRC32.
    
    Args:
        token: String a procesar
    
    Returns:
        String binario (datos + 32 bits CRC)
    """
    crc = compute_crc32(token)
    crc_bin = format(crc, '032b')  # CRC siempre 32 bits
    
    # Convierte cada carácter a 8 bits ASCII
    token_bin = ''.join(format(ord(c), '08b') for c in token)
    
    # Trama final: datos + CRC
    return token_bin + crc_bin

def analyze_frame_sizes(tokens):
    """Analiza el tamaño de las tramas generadas."""
    sizes = []
    for token in tokens:
        frame = create_crc_frame(token)
        sizes.append(len(frame))
    
    print(f"=== ANÁLISIS DE TAMAÑOS ===")
    print(f"Tokens analizados: {len(tokens)}")
    print(f"Tamaño mínimo: {min(sizes)} bits")
    print(f"Tamaño máximo: {max(sizes)} bits")
    print(f"Tamaño promedio: {sum(sizes)/len(sizes):.1f} bits")
    print(f"CRC overhead: 32 bits constantes")

# Configuraciones recomendadas por tamaño (min_len >= 2 para evitar "0")
CONFIGS = {
    'muy_pequeño': {'min_len': 2, 'max_len': 2},  # 48 bits total (2 chars exacto)
    'pequeño': {'min_len': 2, 'max_len': 4},      # 48-64 bits total  
    'mediano': {'min_len': 3, 'max_len': 6},      # 56-80 bits total
    'grande': {'min_len': 5, 'max_len': 8}        # 72-96 bits total
}

def generate_by_size(size_category='pequeño', count=10000):
    """
    Genera tokens según categoría de tamaño predefinida.
    
    Args:
        size_category: 'muy_pequeño', 'pequeño', 'mediano', 'grande'
        count: Número de tokens
    """
    if size_category not in CONFIGS:
        print(f"Categoría inválida. Opciones: {list(CONFIGS.keys())}")
        return []
    
    config = CONFIGS[size_category]
    return generate_crc_safe_tokens(count, **config)

# Ejemplo de uso con diferentes tamaños
if __name__ == "__main__":
    print("=== GENERADOR DE TOKENS CRC OPTIMIZADO ===\n")
    
    # Ejemplo 1: Tokens muy pequeños
    print("1. TOKENS MUY PEQUEÑOS (2 caracteres exacto):")
    tiny_tokens = generate_by_size('muy_pequeño', 5)
    for token in tiny_tokens:
        frame = create_crc_frame(token)
        print(f"Token: '{token}' -> {len(frame)} bits (datos: {len(token)*8}, CRC: 32)")
    
    print("\n" + "="*50 + "\n")
    
    # Ejemplo 2: Tokens pequeños (recomendado)
    print("2. TOKENS PEQUEÑOS (2-4 caracteres) - RECOMENDADO:")
    small_tokens = generate_by_size('pequeño', 5)
    for token in small_tokens:
        frame = create_crc_frame(token)
        print(f"Token: '{token}' -> Trama: {frame[:20]}...{frame[-20:]}")
        print(f"       Longitud total: {len(frame)} bits")
    
    print("\n" + "="*50 + "\n")
    
    # Verificación de exclusión de "0"
    print("3. VERIFICACIÓN - NO SE GENERA '0' INDIVIDUAL:")
    test_tokens = generate_by_size('pequeño', 10000)
    zero_count = test_tokens.count("0")
    print(f"Tokens generados: {len(test_tokens)}")
    print(f"Apariciones de '0': {zero_count} ✅")
    print(f"'0' está reservado para indicar errores en el receptor")
    
    print("\n" + "="*50 + "\n")
    
    # Generar conjunto completo de 10,000 tokens
    print("4. GENERANDO CONJUNTO COMPLETO DE 10,000 TOKENS:")
    print("Generando tokens... (esto puede tomar unos segundos)")
    all_tokens = generate_by_size('pequeño', 10000)
    print(f"✅ {len(all_tokens)} tokens generados exitosamente")
    print(f"Ejemplos: {all_tokens[:10]}")
    print(f"Longitud promedio: {sum(len(t) for t in all_tokens)/len(all_tokens):.1f} caracteres")
    
    # Opcional: guardar en archivo
    save_option = input("\n¿Desea guardar los 10,000 tokens en un archivo? (s/n): ")
    if save_option.lower() in ['s', 'si', 'sí', 'y', 'yes']:
        filename = input("Nombre del archivo (sin extensión): ") or "tokens_crc"
        with open(f"{filename}.txt", 'w', encoding='utf-8') as f:
            for token in all_tokens:
                f.write(f"{token}\n")
        print(f"✅ Tokens guardados en {filename}.txt")
    
    print("\n" + "="*50 + "\n")
    
    # Análisis estadístico
    print("5. ANÁLISIS COMPARATIVO:")
    for category in CONFIGS.keys():
        print(f"\n--- {category.upper()} ---")
        tokens = generate_by_size(category, 1000)  # Muestra más pequeña para análisis
        analyze_frame_sizes(tokens)
    
    print("\n" + "="*50 + "\n")
    
    # Recomendación final
    print("RECOMENDACIÓN:")
    print("- CONFIGURADO para generar 10,000 tokens por defecto")
    print("- Para pruebas rápidas: usar 'muy_pequeño' (48 bits exacto)")
    print("- Para balance óptimo: usar 'pequeño' (48-64 bits)")
    print("- CRC32 siempre añade 32 bits fijos")
    print("- Token '0' está EXCLUIDO (reservado para errores)")
    print("- Tokens más cortos = menos probabilidad de errores múltiples")
    print("- Usa generate_by_size('pequeño', 10000) para conjunto completo")