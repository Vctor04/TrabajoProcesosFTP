// Importación de las clases necesarias para interactuar con el servidor FTP y trabajar con archivos y directorios
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Declaración de la clase principal SincronizadorFTP
public class SincronizadorFTP {
    // Declaración de constantes para la configuración del servidor FTP y las rutas locales
    private static final String SERVIDOR_FTP = "localhost";
    private static final String USUARIO = "Victor";
    private static final String CONTRASENA = "victor";
    private static final String CARPETA_LOCAL = "C:\\Users\\PC\\Documents\\2-Dam\\Procesos y Servicios\\TEMA4\\ProyectoFileZilla\\carpetazip";
    private static final String CARPETA_REMOTA = "carpetazip";
    private static final long TIEMPO_REFRESCO = 15000; // Tiempo de espera entre sincronizaciones

    // Método principal del programa
    public static void main(String[] args) throws IOException {
        // Se define una tarea (task) como un Runnable que ejecutará el método sincronizar() repetidamente en un hilo separado
        Runnable task = () -> {
            while (true) {
                try {
                    sincronizar(); // Llama al método sincronizar()
                    Thread.sleep(TIEMPO_REFRESCO); // Espera el tiempo definido antes de la próxima sincronización
                } catch (InterruptedException | IOException e) { // Captura posibles excepciones
                    e.printStackTrace(); // Imprime la traza de la excepción
                }
            }
        };

        Thread thread = new Thread(task); // Se crea un hilo con la tarea definida
        thread.start(); // Se inicia la ejecución del hilo
    }

    // Método sincronizar(), encargado de la sincronización entre el servidor FTP y la carpeta local
    private static void sincronizar() throws IOException {
        // Se crea una instancia de FTPClient para interactuar con el servidor FTP
        FTPClient clienteFTP = new FTPClient();
        clienteFTP.connect(SERVIDOR_FTP); // Se conecta al servidor FTP
        clienteFTP.login(USUARIO, CONTRASENA); // Se realiza el inicio de sesión en el servidor FTP
        // Se obtienen las listas de carpetas y archivos tanto en el servidor FTP como en la carpeta local
        List<String> carpetasRemotas = obtenerCarpetasRemotas(clienteFTP, CARPETA_REMOTA);
        List<String> carpetasLocales = obtenerCarpetasLocales(CARPETA_LOCAL);
        List<String> archivosRemotos = obtenerArchivosRemotos(clienteFTP, CARPETA_REMOTA);
        List<String> archivosLocales = obtenerArchivosLocales(CARPETA_LOCAL);

        // Se recorren las carpetas remotas y se borran aquellas que no existen en la carpeta local
        for (String carpetaRemota : carpetasRemotas) {
            if (!carpetasLocales.contains(carpetaRemota)) {
                borrarCarpeta(clienteFTP, carpetaRemota);
            }
        }

        // Se recorren los archivos remotos y se borran aquellos que no existen en la carpeta local
        for (String archivoRemoto : archivosRemotos) {
            if (!archivosLocales.contains(archivoRemoto)) {
                borrarArchivo(clienteFTP, archivoRemoto);
            }
        }

        // Se recorren las carpetas locales y se sincronizan con las correspondientes en el servidor FTP
        for (String carpetaLocal : carpetasLocales) {
            // Se construyen las rutas de la carpeta local y remota
            String rutaCarpetaLocal = CARPETA_LOCAL + File.separator + carpetaLocal;
            String rutaCarpetaRemota = CARPETA_REMOTA + "/" + carpetaLocal;

            // Si la carpeta local no existe en el servidor FTP, se crea
            if (!carpetasRemotas.contains(carpetaLocal)) {
                añadirCarpeta(clienteFTP, carpetaLocal);
            } else {
                // Si la carpeta local existe en el servidor FTP, se verifica si está actualizada
                if (!carpetasRemotas.contains(carpetaLocal)) {
                    añadirCarpeta(clienteFTP, carpetaLocal);
                } else {
                    // Si la carpeta local no está actualizada en el servidor FTP, se sincroniza el contenido
                    long ultimaModificacionLocal = new File(rutaCarpetaLocal).lastModified();
                    if (estaCarpetaActualizada(clienteFTP, rutaCarpetaRemota, ultimaModificacionLocal)) {
                        // La carpeta local está actualizada, no hacer nada
                    } else {
                        // La carpeta local no está actualizada, se sincroniza su contenido eliminando y añadiendo nuevamente la carpeta en el servidor FTP
                        borrarCarpeta(clienteFTP, rutaCarpetaRemota);
                        añadirCarpeta(clienteFTP, carpetaLocal);
                    }
                }
            }
        }

        // Se recorren los archivos locales y se suben al servidor FTP aquellos que no existen o están desactualizados
        for (String archivoLocal : archivosLocales) {
            File localFile = new File(CARPETA_LOCAL + File.separator + archivoLocal);
            long ultimaModificacionLocal = localFile.lastModified();

            if (!archivosRemotos.contains(archivoLocal) || esArchivoActualizado(clienteFTP, CARPETA_REMOTA, archivoLocal, ultimaModificacionLocal)) {
                añadirArchivo(clienteFTP, archivoLocal);
            }
        }

        clienteFTP.disconnect(); // Se desconecta del servidor FTP
    }

    // Métodos auxiliares para obtener archivos y carpetas, y realizar operaciones de sincronización
    // Estos métodos están implementados más abajo, se describirán en la siguiente parte del comentario

    // Método para verificar si un archivo local está actualizado en el servidor FTP
    private static boolean esArchivoActualizado(FTPClient clienteFTP, String carpetaRemota, String nombreArchivo, long ultimaModificacionLocal) throws IOException {
        clienteFTP.changeWorkingDirectory(carpetaRemota); // Se cambia al directorio remoto especificado
        FTPFile[] archivosRemotos = clienteFTP.listFiles(); // Se obtiene la lista de archivos remotos

        // Se busca el archivo en la lista de archivos remotos
        for (FTPFile archivoRemoto : archivosRemotos) {
            if (archivoRemoto.getName().equals(nombreArchivo)) {
                long ultimaModificacionRemota = archivoRemoto.getTimestamp().getTimeInMillis(); // Se obtiene la fecha de última modificación del archivo remoto
                return ultimaModificacionLocal > ultimaModificacionRemota; // Se compara la fecha de última modificación local y remota
            }
        }

        return false; // Si el archivo no existe en el servidor FTP, se considera que no está actualizado
    }

    // Método para verificar si una carpeta local está actualizada en el servidor FTP
    private static boolean estaCarpetaActualizada(FTPClient clienteFTP, String carpetaRemota, long ultimaModificacionLocal) throws IOException {
        clienteFTP.changeWorkingDirectory(carpetaRemota); // Se cambia al directorio remoto especificado
        FTPFile[] carpetasRemotas = clienteFTP.listDirectories(); // Se obtiene la lista de carpetas remotas

        // Se recorren las carpetas remotas para verificar si alguna tiene una fecha de última modificación mayor que la local
        for (FTPFile carpeta : carpetasRemotas) {
            if (carpeta.getTimestamp().getTimeInMillis() > ultimaModificacionLocal) {
                return true; // Si se encuentra alguna carpeta actualizada, se retorna true
            }
        }

        return false; // Si ninguna carpeta está actualizada, se retorna false
    }

    // Método para obtener la lista de archivos remotos en una carpeta del servidor FTP
    private static List<String> obtenerArchivosRemotos(FTPClient clienteFTP, String carpeta) throws IOException {
        List<String> archivos = new ArrayList<>(); // Se crea una lista para almacenar los nombres de los archivos
        clienteFTP.changeWorkingDirectory(carpeta); // Se cambia al directorio remoto especificado
        for (String nombreArchivo : clienteFTP.listNames()) { // Se obtiene la lista de nombres de archivos remotos
            archivos.add(nombreArchivo); // Se añade cada nombre de archivo a la lista
        }
        return archivos; // Se retorna la lista de archivos remotos
    }

    // Método para obtener la lista de carpetas remotas en una carpeta del servidor FTP
    private static List<String> obtenerCarpetasRemotas(FTPClient clienteFTP, String carpeta) throws IOException {
        List<String> carpetas = new ArrayList<>(); // Se crea una lista para almacenar los nombres de las carpetas
        clienteFTP.changeWorkingDirectory(carpeta); // Se cambia al directorio remoto especificado
        for (FTPFile archivo : clienteFTP.listFiles()) { // Se obtiene la lista de archivos en el directorio remoto
            if (archivo.isDirectory()) { // Se verifica si el archivo es una carpeta
                carpetas.add(archivo.getName()); // Se añade el nombre de la carpeta a la lista
            }
        }
        return carpetas; // Se retorna la lista de carpetas remotas
    }

    // Método para obtener la lista de archivos locales en una carpeta
    private static List<String> obtenerArchivosLocales(String carpeta) {
        List<String> archivos = new ArrayList<>(); // Se crea una lista para almacenar los nombres de los archivos
        File directorio = new File(carpeta); // Se crea un objeto File con la ruta de la carpeta
        File[] listaArchivos = directorio.listFiles(); // Se obtiene la lista de archivos en la carpeta
        if (listaArchivos != null) { // Se verifica si la lista de archivos no es nula
            for (File archivo : listaArchivos) { // Se recorre la lista de archivos
                if (archivo.isFile()) { // Se verifica si el archivo es un archivo regular
                    archivos.add(archivo.getName()); // Se añade el nombre del archivo a la lista
                }
            }
        }
        return archivos; // Se retorna la lista de archivos locales
    }

    // Método para obtener la lista de carpetas locales en una carpeta (de forma recursiva)
    private static List<String> obtenerCarpetasLocales(String carpeta) {
        List<String> carpetas = new ArrayList<>(); // Se crea una lista para almacenar los nombres de las carpetas
        obtenerCarpetasLocalesRecursivo(new File(carpeta), carpetas, carpeta); // Se llama al método recursivo
        return carpetas; // Se retorna la lista de carpetas locales
    }

    // Método recursivo para obtener las carpetas locales
    private static void obtenerCarpetasLocalesRecursivo(File carpeta, List<String> carpetas, String carpetaBase) {
        File[] listaArchivos = carpeta.listFiles(); // Se obtiene la lista de archivos en la carpeta
        if (listaArchivos != null) { // Se verifica si la lista de archivos no es nula
            for (File archivo : listaArchivos) { // Se recorre la lista de archivos
                if (archivo.isDirectory()) { // Se verifica si el archivo es una carpeta
                    carpetas.add(archivo.getAbsolutePath().replace(carpetaBase, "").substring(1)); // Se añade la ruta relativa de la carpeta a la lista
                    obtenerCarpetasLocalesRecursivo(archivo, carpetas, carpetaBase); // Se llama recursivamente al método para la subcarpeta
                }
            }
        }
    }

    // Método para borrar un archivo en el servidor FTP
    private static void borrarArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        clienteFTP.deleteFile(archivo); // Se borra el archivo en el servidor FTP
    }

    // Método para borrar una carpeta en el servidor FTP (y sus subcarpetas y archivos)
    private static void borrarCarpeta(FTPClient clienteFTP, String carpeta) throws IOException {
        FTPFile[] archivos = clienteFTP.listFiles(carpeta); // Se obtiene la lista de archivos en la carpeta
        if (archivos != null && archivos.length > 0) { // Se verifica si la lista de archivos no es nula ni vacía
            for (FTPFile archivo : archivos) { // Se recorre la lista de archivos
                String nombreArchivo = archivo.getName(); // Se obtiene el nombre del archivo
                String rutaArchivo = carpeta + "/" + nombreArchivo; // Se construye la ruta completa del archivo
                if (archivo.isDirectory()) { // Se verifica si el archivo es una carpeta
                    borrarCarpeta(clienteFTP, rutaArchivo); // Se llama recursivamente al método para borrar la subcarpeta
                } else {
                    clienteFTP.deleteFile(rutaArchivo); // Se borra el archivo en el servidor FTP
                }
            }
        }
        clienteFTP.removeDirectory(carpeta); // Se borra la carpeta principal en el servidor FTP
    }

    // Método para añadir un archivo al servidor FTP
    private static void añadirArchivo(FTPClient clienteFTP, String nombreArchivo) throws IOException {
        FileInputStream fis = null; // Se declara un objeto FileInputStream para leer el archivo local
        try {
            File archivoLocal = new File(CARPETA_LOCAL + File.separator + nombreArchivo); // Se crea un objeto File con la ruta del archivo local
            fis = new FileInputStream(archivoLocal); // Se crea el FileInputStream con el archivo local
            boolean success = clienteFTP.storeFile(nombreArchivo, fis); // Se sube el archivo al servidor FTP
            if (!success) { // Se verifica si la operación fue exitosa
                System.err.println("No se pudo subir el archivo '" + nombreArchivo + "' al servidor FTP."); // Se imprime un mensaje de error
            }
        } finally {
            if (fis != null) {
                fis.close(); // Se cierra el FileInputStream
            }
        }
    }

    // Método para añadir una carpeta al servidor FTP (y sus archivos)
    private static void añadirCarpeta(FTPClient clienteFTP, String carpeta) throws IOException {
        clienteFTP.makeDirectory(carpeta); // Se crea la carpeta en el servidor FTP
        List<String> archivosLocales = obtenerArchivosLocales(CARPETA_LOCAL + File.separator + carpeta); // Se obtiene la lista de archivos en la carpeta local
        for (String archivo : archivosLocales) { // Se recorre la lista de archivos
            añadirArchivo(clienteFTP, carpeta + File.separator + archivo); // Se añade cada archivo al servidor FTP dentro de la carpeta creada
        }
    }
}