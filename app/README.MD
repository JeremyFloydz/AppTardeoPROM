# WebCheckerApp

WebCheckerApp es una aplicación de Android que permite a los usuarios verificar periódicamente una página web en busca de una palabra clave específica. Si la palabra clave se encuentra en el contenido de la página web, la aplicación envía una notificación al usuario.


## MEJORAS AGREGADAS POR MI:

### 1. **Estado de ejecución (detenido / en ejecución)**

La aplicación mantiene un semáforo que indica si el proceso de verificación está en ejecución o detenido.

- Cuando el proceso está en ejecución, el estado se muestra como **"En ejecución"** en la interfaz de usuario.
- Cuando el proceso está detenido, el estado se muestra como **"Detenido"**.

Esta funcionalidad se controla mediante un botón "Play" para iniciar la verificación y un botón "Stop" para detenerla. El semáforo se guarda en las preferencias compartidas de la aplicación, lo que permite mantener el estado entre reinicios de la aplicación.

### 2. **Validación de URL**

La aplicación valida la URL ingresada por el usuario para asegurarse de que sea una URL válida antes de iniciar el proceso de verificación.

- Si la URL no es válida, la aplicación muestra un mensaje de error en el campo de texto correspondiente y no inicia el proceso de verificación.
- La validación se realiza utilizando una expresión regular que comprueba si la URL tiene el formato adecuado.

