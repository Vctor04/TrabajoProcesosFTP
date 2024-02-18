# TrabajoProcesosFTP
El proyecto tiene el objetivo de conectar con un servidor FTP mediante FileZilla para transferir archivos y hacer una copia de seguridad de una carpeta.
Este trabajo consta de dos partes:
## Parte 1: 
Crear una aplicación que solicite el nombre de una carpeta, la comprima, la nombre según fecha y hora actual y la aloje en un servidor FTP. 
En este caso la carpeta que comprimimos se llama carpetazip. Esta se comprime poniendole un nombre compuesto por el original más la fecha actual y la hora. 
Ejemplo: carpetazip_2024-02-16_09-47-01.zip. 
Después de esto, la mandamos al servidor FTP y este aloja la carpeta comprimida en la carpeta llamada administradores dentro de la carpeta miftp, siendo la primera la carpeta repositorio. 
## Parte 2: 
Crear una aplicación que mantenga sincronizado en tiempo real una determinada carpeta local con otra remota (vía FTP) del mismo nombre. La aplicación debe ser capaz de aprovechar la potencia proporcionada por la programación concurrente.
Para hacer esto he utilizado la misma carpeta que en apartado anterior. La sincronizo con el servidor FTP y mediante algunos metodos, recogemos la información que tiene la carpeta repositorio y la comparamos con la local, en función de si hay archivos o carpetas sobrantes o inexistentes en el repositorio eliminamos o añadimos la información necesaria.

