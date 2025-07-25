import java.net.*;
import java.io.*;
import java.util.Properties;

public class Connector {
    
    private static String HOST;
    private static int HAMMING_PORT;
    private static int CRC_PORT;
    private static String PAYLOAD;
    

    static {
        loadEnvConfig();
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
        
        // AF_INET = IPv4, SOCK_STREAM = TCP
        try (Socket socket = new Socket(InetAddress.getByName(HOST), port);
             OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream())) {
            
            System.out.println("Conexión establecida");
            System.out.println("Enviando: " + PAYLOAD);
            
            // Enviar payload
            writer.write(PAYLOAD);
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
            
            // bind() - reserva/asigna el socket a IP:puerto específica
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
                    System.out.println("Recibido:");
                    System.out.println("\"" + receivedData.toString() + "\"");
                    
                  
                } else {
                    System.out.println("No se recibieron datos");
                }
            }
        } catch (BindException e) {
            System.err.println("Puerto " + port + " ya está en uso");
        }
    }
}