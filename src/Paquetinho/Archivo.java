package Paquetinho;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Archivo {

    private File archivo;

    public Archivo() { }

    public void crearArchivo(String nombre) throws IOException {
        this.archivo = new File(asegurarExtensionDocx(nombre));
        if (!this.archivo.exists()) {
            this.archivo.createNewFile();
        }
    }

    public void setArchivo(String nombreORuta) {
        this.archivo = new File(asegurarExtensionDocx(nombreORuta));
    }

    public String leerArchivo() throws IOException {
        ArchivoLeer();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public void guardarContenido(String contenido) throws IOException {
        escribir(contenido, false);
    }

    public void agregarContenido(String contenido) throws IOException {
        escribir(contenido, true);
    }

    private void escribir(String contenido, boolean append) throws IOException {
        ArchivoEscribir();
        try (FileWriter fw = new FileWriter(archivo, append)) {
            fw.write(contenido);
        }
    }

    private String asegurarExtensionDocx(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre o ruta no puede estar vacio");
        }
        return nombre.toLowerCase().endsWith(".docx") ? nombre : nombre + ".docx";
    }

    private void ArchivoLeer() throws IOException {
        if (archivo == null) throw new IllegalStateException("No hay archivo seleccionado");
        if (!archivo.exists()) throw new IOException("El archivo no existe: " + archivo.getAbsolutePath());
        if (!archivo.isFile()) throw new IOException("La ruta no es un archivo valido");
        if (!archivo.canRead()) throw new IOException("No se puede leer el archivo");
    }

    private void ArchivoEscribir() throws IOException {
        if (archivo == null) throw new IllegalStateException("No hay archivo seleccionado");
        if (archivo.exists() && !archivo.canWrite()) {
            throw new IOException("No se puede escribir en el archivo");
        }
    }
}