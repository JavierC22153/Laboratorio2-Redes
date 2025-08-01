import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Connector {
    
    private static String HOST;
    private static int HAMMING_PORT;
    private static int CRC_PORT;
    private static String PAYLOAD;
    private static String valorBinario;
    static String nombreArchivo = "salida.txt";


    static {
        loadEnvConfig();
    }

    public static String aplicarRuido(String valorBinario, double probabilidad) {
        Random random = new Random();
        StringBuilder resultado = new StringBuilder();

        for (char bit : valorBinario.toCharArray()) {
            if (random.nextDouble() < probabilidad) {
                resultado.append(bit == '0' ? '1' : '0');
            } else {
                resultado.append(bit);
            }
        }

        return resultado.toString();
    }
    
    private static void loadEnvConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
            HOST = props.getProperty("HOST", "127.0.0.1");
            HAMMING_PORT = Integer.parseInt(props.getProperty("HAMMING_PORT", "8000"));
            CRC_PORT = Integer.parseInt(props.getProperty("CRC_PORT", "9000"));
            PAYLOAD = props.getProperty("PAYLOAD", "Hola Mundo desde Java");
        } catch (IOException e) {
            System.out.println("No se encontró archivo .env, usando valores por defecto");
            HOST = "127.0.0.1";
            HAMMING_PORT = 8000;
            CRC_PORT = 9000;
            PAYLOAD = "Hola Mundo desde Java";
        }
    }

    
    private static List<String> leerMensajesDeArchivo(String nombreArchivo) {
        List<String> mensajes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    mensajes.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo archivo " + nombreArchivo + ": " + e.getMessage());
        }
        return mensajes;
    }

    
    private static void mostrarArchivosDisponibles() {
        File directorio = new File(".");
        File[] archivos = directorio.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        
        if (archivos != null && archivos.length > 0) {
            System.out.println("\nArchivos .txt disponibles:");
            for (int i = 0; i < archivos.length; i++) {
                System.out.println((i + 1) + ". " + archivos[i].getName());
            }
        } else {
            System.out.println("No se encontraron archivos .txt en el directorio actual.");
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java Connector [hamming|crc]");
            System.out.println("  hamming - Modo receptor en puerto " + HAMMING_PORT);
            System.out.println("  crc     - Modo emisor en puerto " + CRC_PORT);
            return;
        }
        
        String modo = args[0].toLowerCase();
        
        try {
            switch (modo) {
                case "hamming":
                    modoReceptor(HAMMING_PORT);
                    break;
                case "crc":
                    modoEmisor(CRC_PORT);
                    break;
                default:
                    System.out.println("Modo no válido. Use 'hamming' o 'crc'");
                    System.out.println("  hamming - Modo receptor en puerto " + HAMMING_PORT);
                    System.out.println("  crc     - Modo emisor en puerto " + CRC_PORT);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private static void modoEmisor(int port) throws IOException, InterruptedException {
        System.out.println("=== MODO EMISOR (CRC) ===");
        System.out.println("Conectando a " + HOST + ":" + port);
        
        try (Socket socket = new Socket(InetAddress.getByName(HOST), port);
             OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream())) {
            
            System.out.println("Conexión establecida");
            
            
            Scanner scanner = new Scanner(System.in);
            List<String> mensajes = new ArrayList<>();
            
            System.out.println("\nSeleccione el origen de los mensajes:");
            System.out.println("1. Escribir mensaje manualmente");
            System.out.println("2. Leer desde archivo de texto");
            System.out.print("Opción (1 o 2): ");
            
            String opcion = scanner.nextLine().trim();
            
            if ("2".equals(opcion)) {
                
                mostrarArchivosDisponibles();
                System.out.print("\nIngrese el nombre del archivo (incluya la extensión .txt): ");
                String nombreArchivo = scanner.nextLine().trim();
                
                mensajes = leerMensajesDeArchivo(nombreArchivo);
                
                if (mensajes.isEmpty()) {
                    System.out.println("No se pudieron leer mensajes del archivo. Cambiando a modo manual.");
                    System.out.print("Escriba su mensaje: ");
                    mensajes.add(scanner.nextLine());
                } else {
                    System.out.println("Se leyeron " + mensajes.size() + " mensajes del archivo:");
                    for (int i = 0; i < mensajes.size(); i++) {
                        System.out.println("  " + (i + 1) + ". " + mensajes.get(i));
                    }
                    
                    System.out.print("\n¿Enviar todos los mensajes? (s/n): ");
                    String enviarTodos = scanner.nextLine().trim().toLowerCase();
                    
                    if (!"s".equals(enviarTodos) && !"si".equals(enviarTodos)) {
                        System.out.print("Ingrese el número del mensaje a enviar (1-" + mensajes.size() + "): ");
                        try {
                            int indice = Integer.parseInt(scanner.nextLine().trim()) - 1;
                            if (indice >= 0 && indice < mensajes.size()) {
                                String mensajeSeleccionado = mensajes.get(indice);
                                mensajes.clear();
                                mensajes.add(mensajeSeleccionado);
                            } else {
                                System.out.println("Índice inválido. Enviando el primer mensaje.");
                                String primerMensaje = mensajes.get(0);
                                mensajes.clear();
                                mensajes.add(primerMensaje);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Número inválido. Enviando el primer mensaje.");
                            String primerMensaje = mensajes.get(0);
                            mensajes.clear();
                            mensajes.add(primerMensaje);
                        }
                    }
                }
            } else {
                
                System.out.print("Escriba su mensaje: ");
                mensajes.add(scanner.nextLine());
            }
            
            scanner.close();

            
            for (int i = 0; i < mensajes.size(); i++) {
                String mensaje = mensajes.get(i);
                System.out.println("\n--- Procesando mensaje " + (i + 1) + " de " + mensajes.size() + " ---");
                
                
                Coder micodificador = new Coder();
                valorBinario = micodificador.coder_funct(mensaje).replace(" ", "");
                CRC32Emisor emisor = new CRC32Emisor();
                valorBinario = emisor.generarTramaConCRC(valorBinario);
                
                System.out.println("Mensaje original: " + mensaje);
                System.out.println("Trama original: " + valorBinario);
                
                
                valorBinario = aplicarRuido(valorBinario, 0.1);
                System.out.println("Trama con ruido: " + valorBinario);
                
                
                writer.write(valorBinario);
                if (i < mensajes.size() - 1) {
                    writer.write("\n"); 
                }
                writer.flush();
                
                System.out.println("Mensaje " + (i + 1) + " enviado exitosamente");
                
                
                if (mensajes.size() > 1 && i < mensajes.size() - 1) {
                    Thread.sleep(500);
                }
            }
            
            Thread.sleep(100); 
            System.out.println("\nTodos los mensajes enviados exitosamente");
            
        } catch (ConnectException e) {
            System.err.println("No se pudo conectar al servidor. ¿Está corriendo el receptor en puerto " + port + "?");
        }
    }
    
    
    private static void modoReceptor(int port) throws IOException {
        System.out.println("=== MODO RECEPTOR (HAMMING) ===");
        System.out.println("Escuchando en " + HOST + ":" + port);
        
        
        ServerSocket serverSocket = new ServerSocket();
        
        try {
            serverSocket.bind(new InetSocketAddress(HOST, port));
            System.out.println("Servidor iniciado, esperando conexiones...");
            
            
            while (true) {
                System.out.println("\n--- Esperando nueva conexión ---");
                
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader reader = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()))) {
                    
                    InetSocketAddress clientAddr = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                    System.out.println("Conexión entrante del proceso " + clientAddr);
                    
                    
                    StringBuilder receivedData = new StringBuilder();
                    String line;
                    
                    
                    while ((line = reader.readLine()) != null) {
                        receivedData.append(line);
                    }
                    
                    
                    if (receivedData.length() == 0) {
                        char[] buffer = new char[4096];
                        int bytesRead = reader.read(buffer);
                        if (bytesRead > 0) {
                            receivedData.append(buffer, 0, bytesRead);
                        }
                    }
                    
                    if (receivedData.length() > 0) {
                        procesarMensaje(receivedData.toString());
                    } else {
                        System.out.println("No se recibieron datos");
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error procesando cliente: " + e.getMessage());
                    
                }
            }
            
        } catch (BindException e) {
            System.err.println("Puerto " + port + " ya está en uso");
        } finally {
            
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }
    
    
    private static void procesarMensaje(String tramaRecibida) {
        try {
            System.out.println("Trama Recibida: " + tramaRecibida);
            
            
            int[] noError = new int[tramaRecibida.length()];
            for (int i = 0; i < tramaRecibida.length(); i++) {
                char c = tramaRecibida.charAt(i);
                if (c == '0' || c == '1') {
                    noError[i] = Character.getNumericValue(c);
                } else {
                    System.err.println("Carácter inválido en posición " + i + ": " + c);
                    return;
                }
            }
            
            System.out.println("Array binario: " + Arrays.toString(noError));
            
            AnalizadorTramas tramas_props = new AnalizadorTramas();
            tramas_props.analizarTrama(tramaRecibida);
            int n_ = tramas_props.getN();
            int m_ = tramas_props.getM();
            GeneralizedHammingDecoder decoder = new GeneralizedHammingDecoder(n_, m_);
            GeneralizedHammingDecoder.DecodingResult result1 = decoder.decode(noError);
            
            System.out.println("Resultado decodificación: " + result1);
            
            if (result1.getCorrectedFrame() != null) {
                StringBuilder sb = new StringBuilder();
                for (int bit : result1.getDecodedData()) {
                    sb.append(bit);
                }
                
                String resultado = sb.toString();
                Decoder decoder_asci = new Decoder();
                String mensajeDecodificado = decoder_asci.decoder_funct(resultado);
                
                System.out.println("Mensaje Decodificado: " + mensajeDecodificado);

                
                
                if (result1.getErrorType() == GeneralizedHammingDecoder.ErrorType.NO_ERROR ){
                     try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
                        writer.write(mensajeDecodificado+','+String.valueOf(n_)+","+String.valueOf(m_)+","+"N");
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                     try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
                        writer.write(mensajeDecodificado+','+String.valueOf(n_)+","+String.valueOf(m_)+","+"C");
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
            } else {
                System.out.println("No hay trama corregida disponible");
                System.out.println("Trama original sin codificar: " + Arrays.toString(noError));
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
                        writer.write("0"+','+String.valueOf(n_)+","+String.valueOf(m_)+","+"E");
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}