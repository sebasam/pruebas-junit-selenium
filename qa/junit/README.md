# Pruebas JUnit de la API del SIGR

Suite JUnit 4.13.2 (con Hamcrest) que prueba los servicios REST del SIGR usando solo el JDK 21 (`java.net.http`).
Cada clase levanta su propio SIGR con base de datos limpia, en un puerto libre.

## Ejecución
```bash
cd qa/junit
bash ejecutar.sh          # compila con javac, ejecuta la suite e imprime el informe
```
Requisitos: JDK 17 o superior y Node.js 22. Las librerías están en `lib/`, no se descarga nada.
Alternativa con Maven: `mvn test` (ver `pom.xml`; requiere acceso a Maven Central).

## Clases
| Clase | Qué prueba |
|---|---|
| `AutenticacionTest` | Login, registro y roles |
| `LoginParametrizadoTest`, `RegistroParametrizadoTest` | Tablas de datos (runner `Parameterized`) |
| `MenuTest` | CRUD del menú y permisos |
| `PedidosTest` | Total y orden de estados |
| `ReservasTest`, `ReservasLimitesTest` | Regla de 2 horas y valores límite |
| `CajaTest` | Reporte de ventas y cierre único |
| `RobustezYFormulariosTest` | Errores generales, tiempos y contrato HTML de los formularios |
| `PruebaBase`, `ServidorSigr`, `ClienteApi`, `Respuesta`, `Json` | Infraestructura reutilizable |
| `SuiteSigr`, `EjecutarPruebas` | Suite y ejecutor con informe |
