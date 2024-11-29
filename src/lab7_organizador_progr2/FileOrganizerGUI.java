/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_organizador_progr2;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

/**
 *
 * @author royum
 */
public class FileOrganizerGUI extends JFrame {

    private JTree arbol_de_archivo;
    private DefaultTreeModel modelo_de_arbol;
    private JPopupMenu menu;
    private File seleccionarArchivo = null;
    private FuncionesOrganizador funcionesOrganizador;

    public FileOrganizerGUI() {
        setTitle("Navegador y Organizador de Archivos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        this.setLocationRelativeTo(null);

        JOptionPane.showMessageDialog(null, "AL DARLE CLIK A UNA CARPETA, DELE A CLIKC DERECHO PARA INGRESAR A LAS OPCIONES ");

        //aqui directorio inicial
        JFileChooser directorioInicial = new JFileChooser();
        directorioInicial.setDialogTitle("Selecciona la carpeta raiz");
        directorioInicial.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int eleccion = directorioInicial.showOpenDialog(this);
        if (eleccion == JFileChooser.APPROVE_OPTION) {
            File ruta_direcotorio = directorioInicial.getSelectedFile();
            funcionesOrganizador = new FuncionesOrganizador(ruta_direcotorio);
        } else {
            JOptionPane.showMessageDialog(this, "No seleccionaste ninguna carpeta. El programa se cerrara.");
            System.exit(0);
        }

        DefaultMutableTreeNode rutadelnodo = new DefaultMutableTreeNode(funcionesOrganizador.getCurrentDirectory());
        modelo_de_arbol = new DefaultTreeModel(rutadelnodo);
        arbol_de_archivo = new JTree(modelo_de_arbol);
        arbol_de_archivo.setRootVisible(true);
        cargararbol(rutadelnodo);
        JScrollPane treeScroll = new JScrollPane(arbol_de_archivo);

        //aqui se agrega los eventos al JTree
        arbol_de_archivo.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) arbol_de_archivo.getLastSelectedPathComponent();
            if (node != null) {
                seleccionarArchivo = (File) node.getUserObject();
            }
        });

        arbol_de_archivo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopupMenu(e);
                }
            }
        });

        //aqui el menu
        createPopupMenu();
        JButton salir = new JButton("Salir");
        salir.addActionListener(e -> {

            System.exit(0);

        });
        JButton btnRegresar = new JButton("Regresar");

        btnRegresar.addActionListener(e -> {

            JFileChooser nuevoDirectorio = new JFileChooser();
            nuevoDirectorio.setDialogTitle("Selecciona una nueva carpeta raíz");
            nuevoDirectorio.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int nuevaEleccion = nuevoDirectorio.showOpenDialog(this);
            if (nuevaEleccion == JFileChooser.APPROVE_OPTION) {
                File nuevaRuta = nuevoDirectorio.getSelectedFile();

                // Cambiar el directorio inicial
                funcionesOrganizador.Cambiar_Directorio_Inicial(nuevaRuta);

                // Limpiar el árbol existente
                DefaultMutableTreeNode nuevaRaiz = new DefaultMutableTreeNode(nuevaRuta);
                modelo_de_arbol.setRoot(nuevaRaiz);

                // Cargar nueva estructura del árbol
                cargararbol(nuevaRaiz);
                seleccionarArchivo = null; // Reiniciar selección
            } else {
                JOptionPane.showMessageDialog(this, "No seleccionaste ninguna carpeta.");
            }

        });
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelInferior.add(salir);
        panelInferior.add(btnRegresar);

        add(treeScroll, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
        add(treeScroll, BorderLayout.CENTER);
        setVisible(true);
    }

    private void cargararbol(DefaultMutableTreeNode rutanodito) {
        rutanodito.removeAllChildren(); // Limpiar nodos hijos antes de agregar nuevos

        List<File> files = funcionesOrganizador.ListarArchivos(false); // Obtener lista de archivos y carpetas
        for (File file : files) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(file);
            rutanodito.add(nodoHijo);

            // Verificar si es un directorio antes de añadir hijos
            if (file.isDirectory()) {
                addChildren(nodoHijo, file);
            }
        }
        modelo_de_arbol.reload(rutanodito); // Recargar modelo
    }

    private void addChildren(DefaultMutableTreeNode nodo_padre, File archivopadre) {
        File[] children = archivopadre.listFiles(); // Listar archivos y carpetas del directorio
        if (children != null) { // Asegurarse de que no sea nulo
            for (File child : children) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                nodo_padre.add(childNode);

                //aqui se verifica si es un directorio antes de seguir añadiendo hijos
                if (child.isDirectory()) {
                    addChildren(childNode, child);
                }
            }
        }
    }

    private void createPopupMenu() {
        menu = new JPopupMenu();

        // Organizar
        JMenuItem organizarItem = new JMenuItem("Organizar");
        organizarItem.addActionListener(e -> {
            if (seleccionarArchivo != null && seleccionarArchivo.isDirectory()) {
                funcionesOrganizador.Cambiar_Directorio_Inicial(seleccionarArchivo);
                funcionesOrganizador.OrganizarArchivos();
                refrescarnodo();
                JOptionPane.showMessageDialog(this, "Archivos organizados.");
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un directorio para organizar.");
            }
        });
        menu.add(organizarItem);

        // Renombrar
        JMenuItem renombrar_item = new JMenuItem("Renombrar");
        renombrar_item.addActionListener(e -> {
            if (seleccionarArchivo != null) {
                String newName = JOptionPane.showInputDialog(this, "Nuevo nombre:", seleccionarArchivo.getName());
                if (newName != null && !newName.isEmpty()) {
                    if (funcionesOrganizador.RenombrarArchivo(seleccionarArchivo, newName)) {
                        refrescarnodo();
                        JOptionPane.showMessageDialog(this, "Renombrado exitosamente.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al renombrar.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta para renombrar.");
            }
        });
        menu.add(renombrar_item);

        // Crear carpeta
        JMenuItem crearfolfer_item = new JMenuItem("Crear Carpeta");
        crearfolfer_item.addActionListener(e -> {
            String folderName = JOptionPane.showInputDialog(this, "Nombre de la carpeta:");
            if (folderName != null && !folderName.isEmpty()) {
                if (funcionesOrganizador.CrearNuevo_Folder(folderName)) {
                    refrescarnodo();
                    JOptionPane.showMessageDialog(this, "Carpeta creada.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al crear carpeta.");
                }
            }
        });
        menu.add(crearfolfer_item);

        // Crear archivo comercial
        JMenuItem crear_archivo_comercial = new JMenuItem("Crear Archivo Comercial");
        crear_archivo_comercial.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(this, "Nombre del archivo:");
            if (fileName != null && !fileName.isEmpty()) {
                if (funcionesOrganizador.CrearArchivo(fileName)) {
                    refrescarnodo();
                    JOptionPane.showMessageDialog(this, "Archivo creado.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al crear archivo.");
                }
            }
        });
        menu.add(crear_archivo_comercial);

        JMenuItem eliminarItem = new JMenuItem("Eliminar");
        eliminarItem.addActionListener(e -> {
            if (seleccionarArchivo != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Estás seguro de eliminar \"" + seleccionarArchivo.getName() + "\"?",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (funcionesOrganizador.Eiminar(seleccionarArchivo)) {
                        refrescarnodo();
                        JOptionPane.showMessageDialog(this, "Eliminado correctamente.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al eliminar.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta para eliminar.");
            }
        });
        menu.add(eliminarItem);

        // Escribir en archivo
        JMenuItem Registrar_Datos = new JMenuItem("Registrar Datos");
        Registrar_Datos.addActionListener(e -> {
            if (seleccionarArchivo != null && seleccionarArchivo.isFile()) {
                String content = JOptionPane.showInputDialog(this, "Texto a escribir:");
                if (content != null) {
                    if (funcionesOrganizador.RegistrarDatos_en_archivo(seleccionarArchivo, content)) {
                        JOptionPane.showMessageDialog(this, "Texto escrito.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al escribir.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un archivo para escribir.");
            }
        });
        menu.add(Registrar_Datos);

        // Mostrar detalles del archivo
        JMenuItem detailsItem = new JMenuItem("Detalles del Archivo");
        detailsItem.addActionListener(e -> {
            if (seleccionarArchivo != null) {
                String details = funcionesOrganizador.getFileDetails(seleccionarArchivo);
                JOptionPane.showMessageDialog(this, details, "Detalles del Archivo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta.");
            }
        });
        menu.add(detailsItem);

        // Copiar archivo o carpeta
        JMenuItem copiar = new JMenuItem("Copiar");
        copiar.addActionListener(e -> {
            if (seleccionarArchivo != null) {
                funcionesOrganizador.copiar(seleccionarArchivo);
                JOptionPane.showMessageDialog(this, "Archivo o carpeta copiado.");
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta para copiar.");
            }
        });
        menu.add(copiar);

        // Pegar archivo o carpeta
        JMenuItem pegarItem = new JMenuItem("Pegar");
        pegarItem.addActionListener(e -> {
            if (seleccionarArchivo != null && seleccionarArchivo.isDirectory()) {
                if (funcionesOrganizador.pegar(seleccionarArchivo)) {
                    refrescarnodo();
                    JOptionPane.showMessageDialog(this, "Archivo o carpeta pegado.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al pegar. Asegúrese de copiar un archivo/carpeta primero.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un directorio de destino para pegar.");
            }
        });
        menu.add(pegarItem);

        JMenuItem ordenar = new JMenuItem("Ordenar Archivos");
        ordenar.addActionListener(e -> {
            String[] criteria = {"Nombre", "Fecha", "Tipo", "Tamaño"};
            String seleccionar = (String) JOptionPane.showInputDialog(this, "Seleccione un criterio:", "Ordenar Archivos",
                    JOptionPane.QUESTION_MESSAGE, null, criteria, criteria[0]);
            if (seleccionar != null) {
                List<File> sortedFiles = funcionesOrganizador.OrdenarArchivos(seleccionar);
                StringBuilder result = new StringBuilder("Archivos Ordenados:\n");
                for (File file : sortedFiles) {
                    result.append(file.getName()).append("\n");
                }
                JOptionPane.showMessageDialog(this, result.toString(), "Archivos Ordenados", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(ordenar);
    }

    private void showPopupMenu(MouseEvent e) {
        if (seleccionarArchivo != null) {
            menu.show(arbol_de_archivo, e.getX(), e.getY());
        }
    }

    private void refrescarnodo() {
        DefaultMutableTreeNode rutanodote = (DefaultMutableTreeNode) modelo_de_arbol.getRoot();
        funcionesOrganizador.Cambiar_Directorio_Inicial(funcionesOrganizador.getCurrentDirectory());
        cargararbol(rutanodote);
    }

}
