# Build del instalador de escritorio — TattooAge

Guía del proceso completo para generar `TattooAge-1.0.exe` (instalador Windows autónomo con JRE embebido).

---

## Requisitos previos

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | 24 | En `C:\Program Files\Java\jdk-24` |
| JavaFX SDK | 25 | En `C:\javafx-sdk-25` (descarga separada de openjfx.org) |
| .NET SDK | 9+ | Para instalar WiX como herramienta global |
| WiX Toolset | 4.0.5 | **NO usar 7.x** — requiere licencia OSMF de pago |
| Gradle | 8.9 | Vía wrapper `gradlew.bat` |

---

## 1. Instalar WiX 4.0.5

```powershell
dotnet tool install --global wix --version 4.0.5
```

Añadir al PATH:
```powershell
$env:PATH = "$env:USERPROFILE\.dotnet\tools;$env:PATH"
```

Instalar extensiones necesarias (en cualquier carpeta de trabajo):
```powershell
cd $env:TEMP
mkdir wix-work; cd wix-work
wix extension add WixToolset.UI.wixext/4.0.5
wix extension add WixToolset.Util.wixext/4.0.5
```

Copiar extensiones al caché global (para que jpackage las encuentre):
```powershell
Copy-Item -Recurse "$env:TEMP\wix-work\.wix\extensions\*" "$env:USERPROFILE\.wix\extensions\" -Force
```

> **Problema conocido:** `wix extension add -g` falla silenciosamente en algunas instalaciones.
> La solución es instalar localmente y copiar manualmente al perfil de usuario.

---

## 2. Generar el icono `.ico`

jpackage requiere un `.ico` para el icono de Windows. Se genera desde `logo.png` con PowerShell:

```powershell
Add-Type -AssemblyName System.Drawing
$src = "ruta\logo.png"
$dst = "ruta\logo.ico"
$sizes = @(256, 48, 32, 16)
# (ver script completo en historial — genera ICO multi-resolución)
```

El archivo se guarda en `app/src/main/resources/images/logo.ico`.

---

## 3. Clase Launcher (obligatoria para classpath + JavaFX)

Cuando JavaFX se carga desde el classpath (no como módulo JPMS), la clase principal
que extiende `Application` lanza el error **"JavaFX runtime components are missing"**.

Solución: crear `com.javafx.Launcher` que no extienda `Application`:

```java
// app/src/main/java/com/javafx/Launcher.java
package com.javafx;

import java.io.File;

public class Launcher {
    public static void main(String[] args) {
        // Redirigir temp de JasperReports fuera de Program Files
        String appData = System.getenv("APPDATA");
        String tempBase = appData != null ? appData : System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempBase, "TattooAge" + File.separator + "temp");
        tempDir.mkdirs();
        System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
        System.setProperty("net.sf.jasperreports.compiler.temp.dir", tempDir.getAbsolutePath());
        Main.main(args);
    }
}
```

En `build.gradle`, cambiar el `Main-Class` del jar a `Launcher`:

```groovy
manifest {
    attributes(
        'Main-Class': 'com.javafx.Launcher',
        'Class-Path': configurations.runtimeClasspath.files.collect { "lib/" + it.name }.join(' ')
    )
}
```

> El `run` task de Gradle sigue usando `Main` directamente — no se ve afectado.

---

## 4. Fix de JasperReports (permisos de escritura)

JasperReports escribe archivos `.java` temporales en el directorio actual (`user.dir`).
Cuando la app está instalada en `C:\Program Files\`, ese directorio es de solo lectura.

Solución en `ReportService.java`, antes de `JasperCompileManager.compile(d)`:

```java
String appData = System.getenv("APPDATA");
String tempBase = appData != null ? appData : System.getProperty("java.io.tmpdir");
File tempDir = new File(tempBase, "TattooAge" + File.separator + "temp");
tempDir.mkdirs();
DefaultJasperReportsContext jrCtx = DefaultJasperReportsContext.getInstance();
jrCtx.setProperty(JRCompiler.COMPILER_TEMP_DIR, tempDir.getAbsolutePath());

JasperReport report = JasperCompileManager.getInstance(jrCtx).compile(d);
```

Los temporales se escriben en `%APPDATA%\TattooAge\temp\` — siempre con permisos.

---

## 5. Compilar el jar

```powershell
cd "C:\...\proyecto_app_primerTrimestre"
.\gradlew.bat clean jar
```

Genera `app/build/libs/app.jar` con todas las dependencias en `app/build/libs/lib/`.

---

## 6. Construir el instalador

El proceso tiene 3 pasos: app-image → copiar DLLs nativas de JavaFX → empaquetar como EXE.

### 6a. Generar app-image

```powershell
$jpackage = "C:\Program Files\Java\jdk-24\bin\jpackage.exe"
$projectRoot = "C:\...\proyecto_app_primerTrimestre"
$dist = "$projectRoot\dist"

& $jpackage `
  --type app-image `
  --name "TattooAge" --app-version "1.0" --vendor "TattooAge" `
  --input "$projectRoot\app\build\libs" `
  --main-jar "app.jar" `
  --icon "$projectRoot\app\src\main\resources\images\logo.ico" `
  --dest $dist
```

### 6b. Copiar DLLs nativas de JavaFX

jpackage no incluye las DLLs nativas de JavaFX cuando JavaFX va en el classpath.
Hay que copiarlas manualmente desde el JavaFX SDK:

```powershell
$jfxBin = "C:\javafx-sdk-25\bin"
$runtimeBin = "$dist\TattooAge\runtime\bin"

@("decora_sse.dll","fxplugins.dll","glass.dll","glib-lite.dll","gstreamer-lite.dll",
  "javafx_font.dll","javafx_iio.dll","jfxmedia.dll","jfxwebkit.dll",
  "prism_common.dll","prism_d3d.dll","prism_sw.dll") | ForEach-Object {
    Copy-Item "$jfxBin\$_" "$runtimeBin\$_" -Force
}
```

### 6c. Limpiar opciones de módulo del cfg

El cfg generado incluye `--add-exports=javafx.graphics/...` que hace fallar el JVM
cuando JavaFX no está como módulo. Hay que eliminarlas:

```powershell
$cfg = "$dist\TattooAge\app\TattooAge.cfg"
(Get-Content $cfg) |
    Where-Object { $_ -notmatch "--add-exports" -and $_ -notmatch "--add-opens" } |
    Set-Content $cfg
```

### 6d. Empaquetar como EXE

```powershell
$env:PATH = "$env:USERPROFILE\.dotnet\tools;$env:PATH"

& $jpackage `
  --type exe `
  --app-image "$dist\TattooAge" `
  --name "TattooAge" --app-version "1.0" --vendor "TattooAge" `
  --icon "$projectRoot\app\src\main\resources\images\logo.ico" `
  --dest $dist `
  --win-dir-chooser --win-shortcut --win-menu
```

Resultado: `dist\TattooAge-1.0.exe` (~136 MB, JRE embebido, no requiere Java instalado).

---

## Problemas encontrados y soluciones

| Error | Causa | Solución |
|---|---|---|
| `WIX0144: extension not found` | `wix extension add -g` no funciona correctamente | Instalar localmente y copiar a `~\.wix\extensions\` |
| `Failed to launch JVM` | `--add-exports=javafx.graphics/...` en cfg, módulo no existe en JRE bundled | Eliminar esas líneas del TattooAge.cfg |
| `JavaFX runtime components are missing` | Main class extiende Application y JavaFX no está en el module path | Crear clase Launcher wrapper |
| `Error saving expressions class file: C:\Program Files\TattooAge\...` | JasperReports escribe temporales en `user.dir` = directorio protegido | Setear `JRCompiler.COMPILER_TEMP_DIR` a `%APPDATA%\TattooAge\temp` |
| DLLs nativas de JavaFX ausentes | jpackage no las incluye con classpath JARs | Copiar desde `C:\javafx-sdk-25\bin\` |
