import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class Connector {
    
    private static String HOST;
    private static int HAMMING_PORT;
    private static int CRC_PORT;
    private static String PAYLOAD;
    private static String valorBinario;


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
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java SocketApp [hamming|crc]");
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
    
    // Modo Emisor (Cliente) - usa CRC_PORT
    private static void modoEmisor(int port) throws IOException, InterruptedException {
        System.out.println("=== MODO EMISOR (CRC) ===");
        System.out.println("Conectando a " + HOST + ":" + port);
        
        
        try (Socket socket = new Socket(InetAddress.getByName(HOST), port);
             OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream())) {
            
            System.out.println("Conexión establecida");
            
            //capa de aplicacion
            Scanner scanner = new Scanner(System.in);
            System.out.print("Escriba su mensaje: ");
            String PAYLOAD = scanner.nextLine();
            
            scanner.close();




            //capa de presentacion

            Coder micodificador = new Coder();

            valorBinario = micodificador.coder_funct(PAYLOAD).replace(" ", "");
            CRC32Emisor emisor = new CRC32Emisor();
            valorBinario = emisor.generarTramaConCRC(valorBinario);
            
            System.out.println("Mensaje original: " + PAYLOAD);
            System.out.println("Trama original: " + valorBinario);
            

            // Capa de ruido
            
            valorBinario = aplicarRuido(valorBinario, 0);
            System.out.println("Trama con ruido: " + valorBinario);
            // capa de enlace
            writer.write(valorBinario);
            writer.flush();
            
            Thread.sleep(100); // Pausa opcional
            
            System.out.println("Mensaje enviado exitosamente");
            
        } catch (ConnectException e) {
            System.err.println("No se pudo conectar al servidor. ¿Está corriendo el receptor en puerto " + port + "?");
        }
    }
    
    // Modo Receptor (Servidor) - usa HAMMING_PORT
    private static void modoReceptor(int port) throws IOException {
        System.out.println("=== MODO RECEPTOR (HAMMING) ===");
        System.out.println("Escuchando en " + HOST + ":" + port);
        

        try (ServerSocket serverSocket = new ServerSocket()) {
            
           
            serverSocket.bind(new InetSocketAddress(HOST, port));
            
            System.out.println("Servidor iniciado, esperando conexiones...");
            

            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {
                
                InetSocketAddress clientAddr = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                System.out.println("Conexión entrante del proceso " + clientAddr);
                
               
                char[] buffer = new char[1024];
                StringBuilder receivedData = new StringBuilder();
                
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) != -1) {
                    receivedData.append(buffer, 0, bytesRead);
                    
                    
                    if (bytesRead < 1024) {
                        break;
                    }
                }
                
                if (receivedData.length() > 0) {
                    System.out.println("Trama Recibida:");

                    
                    int[] noError = new int[receivedData.toString().length()];
                    for (int i = 0; i < receivedData.toString().length(); i++) {
                        noError[i] = Character.getNumericValue(receivedData.toString().charAt(i));
                    }
                    System.out.println(Arrays.toString(noError) + "\"");
                    GeneralizedHammingDecoder decoder = new GeneralizedHammingDecoder(12, 8);
                    GeneralizedHammingDecoder.DecodingResult result1 = decoder.decode(noError); 
                    System.out.println(result1);

                    if (result1.getCorrectedFrame() != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int bit : result1.getDecodedData()) {
                            sb.append(bit);
                        }

                        String resultado = sb.toString(); 
                        Decoder decoder_asci = new Decoder();
                        System.out.println("Mensaje Decodificado: " + decoder_asci.decoder_funct(resultado));

                    }else{
                        System.out.println("No hay trama corregida disponible");
                        System.out.println("Trama original sin codificar: " + Arrays.toString(noError));
                    }
                    

                
                
                    
                  
                } else {
                    System.out.println("No se recibieron datos");
                }
            }
        } catch (BindException e) {
            System.err.println("Puerto " + port + " ya está en uso");
        }
    }
}