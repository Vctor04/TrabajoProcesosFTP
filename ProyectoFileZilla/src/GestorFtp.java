import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// Clase GestorFtp para gestionar las operaciones FTP
class GestorFtp {
    private final String server; // Variable para almacenar la dirección del servidor FTP
    private final int port; // Variable para almacenar el puerto del servidor FTP
    private final String user; // Variable para almacenar el nombre de usuario del servidor FTP
    private final String password; // Variable para almacenar la contraseña del servidor FTP
    private FTPClient ftp; // Variable para representar el cliente FTP

    // Constructor de la clase GestorFtp
    public GestorFtp(String server, int port, String user, String password) {
        this.server = server; // Asignación de la dirección del servidor FTP
        this.port = port; // Asignación del puerto del servidor FTP
        this.user = user; // Asignación del nombre de usuario del servidor FTP
        this.password = password; // Asignación de la contraseña del servidor FTP
        ftp = new FTPClient(); // Inicialización del cliente FTP
    }

    // Método para subir un archivo al servidor FTP
    public void uploadFile(String localFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(localFile)) { // Se crea un InputStream para leer el archivo local
            ftp.connect(server, port); // Se establece la conexión con el servidor FTP
            ftp.login(user, password); // Se realiza el inicio de sesión en el servidor FTP
            ftp.enterLocalPassiveMode(); // Se activa el modo pasivo para la transferencia de datos
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE); // Se establece el tipo de archivo a binario
            boolean done = ftp.storeFile(new File(localFile).getName(), inputStream); // Se sube el archivo al servidor FTP
            if (done) { // Si la operación es exitosa
                System.out.println("El archivo se ha subido correctamente al servidor FTP."); // Se muestra un mensaje de éxito
            } else { // Si ocurre un error
                System.out.println("Ha ocurrido un error al subir el archivo al servidor FTP."); // Se muestra un mensaje de error
            }
        }
    }

    // Método para desconectarse del servidor FTP
    public void disconnect() throws IOException {
        if (ftp.isConnected()) { // Si la conexión está establecida
            ftp.logout(); // Se cierra la sesión en el servidor FTP
            ftp.disconnect(); // Se desconecta del servidor FTP
        }
    }
}