package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Rol;
import com.repuestosanelisa.model.Usuario;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.util.Config;
import com.repuestosanelisa.util.HashUtil;
import com.repuestosanelisa.util.NegocioException;
import com.repuestosanelisa.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class UsuarioService {

    private final Path archivo = Config.DATA_DIR.resolve("usuarios.xml");
    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioService() {
        boolean cargado = false;
        if (Files.exists(archivo)) {
            try {
                cargar();
                cargado = usuarios.stream().anyMatch(u -> u.isActivo() && u.getRol() == Rol.ADMINISTRADOR);
            } catch (NegocioException e) {
                cargado = false; // archivo de una versión anterior o dañado
            }
            if (!cargado) respaldarArchivoIncompatible();
        }
        if (!cargado) {
            usuarios.clear();
            usuarios.add(new Usuario("Anelisa", "anelisa", HashUtil.sha256("admin123"), Rol.ADMINISTRADOR, true));
            if (Config.CARGAR_DATOS_DEMO) {
                usuarios.add(new Usuario("Empleado 1", "empleado1", HashUtil.sha256("emp123"), Rol.EMPLEADO, true));
            }
            guardar();
        }
    }


    private void respaldarArchivoIncompatible() {
        try {
            Files.move(archivo, archivo.resolveSibling("usuarios.xml.bak"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException ignorado) {
            // si no se puede respaldar, guardar() lo sobrescribe
        }
    }


    public Usuario login(String usuario, String contrasena) {
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isEmpty()) {
            throw new NegocioException("Ingrese su usuario y contraseña");
        }
        // <AQUÍ VA MYSQL> — SELECT ... FROM usuarios WHERE usuario = ? AND activo = 1
        Usuario u = buscar(usuario.trim());
        if (u == null || !u.isActivo() || !u.getContrasenaHash().equals(HashUtil.sha256(contrasena))) {
            throw new NegocioException("Credenciales incorrectas o usuario deshabilitado");
        }
        Sesion.iniciar(u);
        return u;
    }

    public void logout() { Sesion.cerrar(); }



    public List<Usuario> listar() {
        Permisos.exigir(Modulo.USUARIOS);
        return usuarios.stream().sorted(Comparator.comparing(Usuario::getNombre, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public void crear(String nombre, String usuario, String contrasena, Rol rol, boolean activo) {
        Permisos.exigir(Modulo.USUARIOS);
        validarBasicos(nombre, usuario, rol);
        if (contrasena == null || contrasena.length() < 4) {
            throw new NegocioException("La contraseña debe tener al menos 4 caracteres");
        }
        if (buscar(usuario.trim()) != null) throw new NegocioException("El nombre de usuario ya existe");
        Usuario nuevo = new Usuario(nombre.trim(), usuario.trim(), HashUtil.sha256(contrasena), rol, activo);
        usuarios.add(nuevo);
        try {
            guardar();
        } catch (NegocioException e) {
            usuarios.remove(nuevo);
            throw e;
        }
    }


    public void editar(String usuario, String nombre, String contrasena, Rol rol, boolean activo) {
        Permisos.exigir(Modulo.USUARIOS);
        validarBasicos(nombre, usuario, rol);
        Usuario u = buscar(usuario);
        if (u == null) throw new NegocioException("El usuario no existe");
        if (contrasena != null && !contrasena.isEmpty() && contrasena.length() < 4) {
            throw new NegocioException("La contraseña debe tener al menos 4 caracteres");
        }
        Usuario yo = Sesion.actual();
        if (yo != null && yo.getUsuario().equalsIgnoreCase(u.getUsuario()) && (!activo || rol != Rol.ADMINISTRADOR)) {
            throw new NegocioException("No puede desactivarse ni quitarse el rol de Administrador a sí mismo");
        }
        long admins = usuarios.stream()
                .filter(x -> x != u && x.isActivo() && x.getRol() == Rol.ADMINISTRADOR).count()
                + (activo && rol == Rol.ADMINISTRADOR ? 1 : 0);
        if (admins == 0) throw new NegocioException("Debe quedar al menos un Administrador activo");

        u.setNombre(nombre.trim());
        u.setRol(rol);
        u.setActivo(activo);
        if (contrasena != null && !contrasena.isEmpty()) u.setContrasenaHash(HashUtil.sha256(contrasena));
        guardar();
    }



    private Usuario buscar(String usuario) {
        for (Usuario u : usuarios) if (u.getUsuario().equalsIgnoreCase(usuario)) return u;
        return null;
    }

    private void validarBasicos(String nombre, String usuario, Rol rol) {
        if (nombre == null || nombre.isBlank() || usuario == null || usuario.isBlank() || rol == null) {
            throw new NegocioException("Faltan datos obligatorios (nombre, usuario, contraseña y rol)");
        }
        if (usuario.trim().contains(" ")) throw new NegocioException("El nombre de usuario no puede tener espacios");
    }

    private void cargar() {

        try {
            Document d = XmlUtil.leer(archivo);
            for (Element e : XmlUtil.hijos(d.getDocumentElement(), "usuario")) {
                usuarios.add(new Usuario(XmlUtil.texto(e, "nombre"), XmlUtil.texto(e, "login"),
                        XmlUtil.texto(e, "passwordHash"), Rol.valueOf(XmlUtil.texto(e, "rol")),
                        Boolean.parseBoolean(XmlUtil.texto(e, "activo"))));
            }
        } catch (Exception ex) {
            throw new NegocioException("No se pudo leer usuarios.xml: " + ex.getMessage());
        }
    }

    private void guardar() {

        try {
            Document d = XmlUtil.nuevo("usuarios");
            for (Usuario u : usuarios) {
                Element e = XmlUtil.agregar(d, d.getDocumentElement(), "usuario", null);
                XmlUtil.agregar(d, e, "nombre", u.getNombre());
                XmlUtil.agregar(d, e, "login", u.getUsuario());
                XmlUtil.agregar(d, e, "passwordHash", u.getContrasenaHash());
                XmlUtil.agregar(d, e, "rol", u.getRol().name());
                XmlUtil.agregar(d, e, "activo", String.valueOf(u.isActivo()));
            }
            XmlUtil.escribir(d, archivo);
        } catch (Exception ex) {
            throw new NegocioException("No se pudo guardar usuarios.xml: " + ex.getMessage());
        }
    }
}

