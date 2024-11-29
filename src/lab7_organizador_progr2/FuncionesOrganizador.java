/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_organizador_progr2;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author royum
 */
public class FuncionesOrganizador {

    private File DirectorioActual;
    private File archivoCopiado;

    // Constructor
    public FuncionesOrganizador(File DirectorioInicial) {
        
        if (DirectorioInicial != null && DirectorioInicial.isDirectory()) {
            
            this.DirectorioActual = DirectorioInicial;
            
        }else{
            this.DirectorioActual = new File(System.getProperty("user.home"));
        }
    }

    
    public void Cambiar_Directorio_Inicial(File directorio) {
        
        if (directorio.isDirectory()) {
            
            this.DirectorioActual = directorio;
            
        }
    }

    
    public File getCurrentDirectory() {
        
        return DirectorioActual;
        
    }

    public List<File> ListarArchivos(boolean Oculto) {
        
        File[] files = DirectorioActual.listFiles();
        
        if (files == null) {
            
            return Collections.emptyList();
            
        }
        return Arrays.stream(files)
                
                .filter(file -> Oculto || !file.isHidden())
                .collect(Collectors.toList());
        
    }


    public void OrganizarArchivos() {
        
        File[] files = DirectorioActual.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String extension = getFileExtension(file);
                    File TiposArchivos = new File(DirectorioActual, extension);
                    if (!TiposArchivos.exists()) {
                        TiposArchivos.mkdir();
                    }
                    file.renameTo(new File(TiposArchivos, file.getName()));
                }
            }
        }
    }

    // Sort files by criteria
    public List<File> OrdenarArchivos(String criteria) {
        List<File> files = ListarArchivos(false);
        Comparator<File> comparator = switch (criteria.toLowerCase()) {
            case "nombre" ->
                Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
            case "fecha" ->
                Comparator.comparingLong(File::lastModified);
            case "Tipo" ->
                Comparator.comparing(this::getFileExtension);
            case "Tamaño" ->
                Comparator.comparingLong(File::length);
            default ->
                null;
        };
        if (comparator != null) {
            files.sort(comparator);
        }
        return files;
    }

   
    public boolean RenombrarArchivo(File file, String newName) {
        if (file != null && newName != null && !newName.trim().isEmpty()) {
            File renamedFile = new File(file.getParent(), newName);
            return file.renameTo(renamedFile);
        }
        return false;
    }

   
    public boolean CrearArchivo(String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            File nuevo = new File(DirectorioActual, fileName + ".com");
            try {
                return nuevo.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    
    public boolean CrearNuevo_Folder(String Nomvbre_folder) {
        if (Nomvbre_folder != null && !Nomvbre_folder.trim().isEmpty()) {
            File nuevo = new File(DirectorioActual, Nomvbre_folder);
            return nuevo.mkdir();
        }
        return false;
    }

    // Copiar archivo o carpeta
    public void copiar(File archivo) {
        if (archivo != null) {
            archivoCopiado = archivo;
        }
    }

    private void copiarDirectorio(Path fuente, Path destino) throws IOException {
        Files.walk(fuente).forEach(path -> {
            try {
                Path targetPath = destino.resolve(fuente.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean pegar(File destino) {
        if (archivoCopiado == null || destino == null || !destino.isDirectory()) {
            return false;
        }

        try {
            if (archivoCopiado.isDirectory()) {
                copiarDirectorio(archivoCopiado.toPath(), destino.toPath().resolve(archivoCopiado.getName()));
            } else {
                Files.copy(archivoCopiado.toPath(), destino.toPath().resolve(archivoCopiado.getName()), StandardCopyOption.REPLACE_EXISTING);
            }
            archivoCopiado = null; 
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean RegistrarDatos_en_archivo(File archivo, String data) {
        if (archivo != null && archivo.isFile() && data != null) {
            try (FileWriter writer = new FileWriter(archivo, true)) {
                writer.write(data + System.lineSeparator());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex > 0) ? name.substring(lastIndex + 1).toLowerCase() : "Desconocido";
    }
    
    public boolean Eiminar(File archivo_carpeta){
        
        if(archivo_carpeta==null|| !archivo_carpeta.exists()){
            return false;
        }
        
        if(archivo_carpeta.isDirectory()){
            
            File[] contenido=archivo_carpeta.listFiles();
            if(contenido!=null){
                
                for (File file : contenido) {
                    
                    Eiminar(file);
                    
                }
                
            }
            
        }
        return archivo_carpeta.delete();
        
    }

    
    public String getFileDetails(File file) {
        if (file == null) {
            return "No file selected.";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder details = new StringBuilder();
        details.append("Nombre: ").append(file.getName()).append("\n");
        details.append("Path: ").append(file.getAbsolutePath()).append("\n");
        details.append("Tamanio: ").append(file.length()).append(" bytes\n");
        details.append("Ultima modificaion: ").append(sdf.format(new Date(file.lastModified()))).append("\n");
        details.append("Tipo: ").append(file.isDirectory() ? "Directorio" : getFileExtension(file)).append("\n");
        return details.toString();
    }
}
