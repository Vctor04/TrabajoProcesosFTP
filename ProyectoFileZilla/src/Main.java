import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // Se crea un BufferedReader para leer la entrada del usuario
        try {
            Scanner scanner = new Scanner(System.in); // Se crea un objeto Scanner para leer la entrada del usuario

            // Solicitar el nombre de la carpeta
            System.out.print("Ingrese el nombre de la carpeta (ruta absoluta): "); // Se solicita al usuario que ingrese la ruta absoluta de la carpeta
            String nombreCarpeta = scanner.nextLine(); // Se lee la entrada del usuario

            // Comprimir la carpeta
            String nombreCarpetaZip = comprimirCarpeta(nombreCarpeta); // Se comprime la carpeta y se obtiene el nombre del archivo zip resultante
            System.out.println(nombreCarpetaZip); // Se imprime el nombre del archivo zip resultante

            // Subir el archivo comprimido al servidor FTP
            transferToFTP(nombreCarpetaZip); // Se transfiere el archivo comprimido al servidor FTP

            System.out.println("¡La operación se ha completado con éxito!"); // Se muestra un mensaje de éxito
        } catch (IOException e) {
            e.printStackTrace(); // En caso de error, se imprime la traza de la excepción
        }
    }

    // Método para comprimir una carpeta
    private static String comprimirCarpeta(String nombreCarpeta) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // Se crea un formato de fecha para el nombre del archivo zip
        String nombreFicheroZip = nombreCarpeta + "_" + dateFormat.format(new Date()) + ".zip"; // Se construye el nombre del archivo zip con la fecha actual

        FileOutputStream fos = new FileOutputStream(nombreFicheroZip); // Se crea un FileOutputStream para escribir en el archivo zip
        ZipOutputStream zipOut = new ZipOutputStream(fos); // Se crea un ZipOutputStream para escribir en el archivo zip

        File carpeta = new File(nombreCarpeta); // Se crea un objeto File con la carpeta a comprimir
        comprimir(carpeta, carpeta.getName(), zipOut); // Se invoca el método para comprimir la carpeta

        zipOut.close(); // Se cierra el flujo de salida del archivo zip
        fos.close(); // Se cierra el FileOutputStream

        return nombreFicheroZip; // Se retorna el nombre del archivo zip
    }

    // Método recursivo para comprimir archivos y carpetas
    private static void comprimir(File entrada, String rutaRelativa, ZipOutputStream zipOut) throws IOException {
        byte[] buffer = new byte[1024]; // Buffer para leer y escribir datos en los archivos
        int len;

        if (entrada.isDirectory()) { // Si la entrada es una carpeta
            if (!rutaRelativa.isEmpty()) { // Si la ruta relativa no está vacía
                if (!rutaRelativa.endsWith(File.separator)) { // Si la ruta relativa no termina con el separador de archivos del sistema
                    rutaRelativa += File.separator; // Se añade el separador de archivos al final de la ruta relativa
                }
                ZipEntry entradaZip = new ZipEntry(rutaRelativa); // Se crea una entrada Zip para la carpeta
                zipOut.putNextEntry(entradaZip); // Se añade la entrada Zip al archivo zip
            }
            File[] archivos = entrada.listFiles(); // Se obtiene la lista de archivos en la carpeta
            for (File archivo : archivos) { // Se recorren los archivos
                comprimir(archivo, rutaRelativa + archivo.getName(), zipOut); // Se invoca recursivamente el método para comprimir cada archivo
            }
        } else { // Si la entrada es un archivo
            FileInputStream fis = new FileInputStream(entrada); // Se crea un FileInputStream para leer el archivo
            ZipEntry entradaZip = new ZipEntry(rutaRelativa); // Se crea una entrada Zip para el archivo
            zipOut.putNextEntry(entradaZip); // Se añade la entrada Zip al archivo zip
            while ((len = fis.read(buffer)) > 0) { // Mientras se puedan leer datos del archivo
                zipOut.write(buffer, 0, len); // Se escribe en el archivo zip
            }
            fis.close(); // Se cierra el FileInputStream
        }
    }

    // Método para transferir el archivo comprimido al servidor FTP
    private static void transferToFTP(String nombreFicheroZip) {
        String server = "localhost"; // Dirección del servidor FTP
        int port = 21; // Puerto del servidor FTP
        String user = "Victor"; // Nombre de usuario del servidor FTP
        String password = "victor"; // Contraseña del servidor FTP

        try {
            // Se instancia un objeto GestorFtp con los datos del servidor FTP
            GestorFtp gestorFtp = new GestorFtp(server, port, user, password);
            gestorFtp.uploadFile(nombreFicheroZip); // Se sube el archivo al servidor FTP
            gestorFtp.disconnect(); // Se cierra la conexión FTP
        } catch (IOException e) {
            e.printStackTrace(); // En caso de error, se imprime la traza de la excepción
        }
    }
}