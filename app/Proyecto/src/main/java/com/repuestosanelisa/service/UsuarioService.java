package com.repuestosanelisa.service;

import com.repuestosanelisa.model.Rol;
import com.repuestosanelisa.model.Usuario;
import com.repuestosanelisa.security.Modulo;
import com.repuestosanelisa.security.Permisos;
import com.repuestosanelisa.security.Sesion;
import com.repuestosanelisa.util.Db;
import com.repuestosanelisa.util.HashUtil;
import com.repuestosanelisa.util.NegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioService {

    private static final String COLUMNAS = "nombre, usuario, contrasena_hash, rol, activo";


    public Usuario login(String usuario, String contrasena) {
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isEmpty()) {
            throw new NegocioException("Ingrese su usuario y contraseña");
        }
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
        return Db.consulta(c -> {
            List<Usuario> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNAS + " FROM usuarios ORDER BY nombre");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(leer(rs));
            }
            return lista;
        });
    }

    public void crear(String nombre, String usuario, String contrasena, Rol rol, boolean activo) {
        Permisos.exigir(Modulo.USUARIOS);
        validarBasicos(nombre, usuario, rol);
        if (contrasena == null || contrasena.length() < 4) {
            throw new NegocioException("La contraseña debe tener al menos 4 caracteres");
        }
        if (buscar(usuario.trim()) != null) throw new NegocioException("El nombre de usuario ya existe");
        Db.consulta(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO usuarios (nombre, usuario, contrasena_hash, rol, activo) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, nombre.trim());
                ps.setString(2, usuario.trim());
                ps.setString(3, HashUtil.sha256(contrasena));
                ps.setString(4, rol.name());
                ps.setBoolean(5, activo);
                ps.executeUpdate();
            }
            return null;
        });
    }

    public void editar(String usuario, String nombre, String contrasena, Rol rol, boolean activo) {
        Permisos.exigir(Modulo.USUARIOS);
        validarBasicos(nombre, usuario, rol);
        Usuario u = buscar(usuario);
        if (u == null) throw new NegocioException("El usuario no existe");
        boolean cambiaClave = contrasena != null && !contrasena.isEmpty();
        if (cambiaClave && contrasena.length() < 4) {
            throw new NegocioException("La contraseña debe tener al menos 4 caracteres");
        }
        Usuario yo = Sesion.actual();
        boolean soyYo = yo != null && yo.getUsuario().equalsIgnoreCase(u.getUsuario());
        if (soyYo && (!activo || rol != Rol.ADMINISTRADOR)) {
            throw new NegocioException("No puede desactivarse ni quitarse el rol de Administrador a sí mismo");
        }

        Db.transaccion(c -> {
            long otrosAdmins;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMINISTRADOR' AND activo = 1 AND usuario <> ?")) {
                ps.setString(1, u.getUsuario());
                try (ResultSet rs = ps.executeQuery()) { rs.next(); otrosAdmins = rs.getLong(1); }
            }
            if (otrosAdmins + (activo && rol == Rol.ADMINISTRADOR ? 1 : 0) == 0) {
                throw new NegocioException("Debe quedar al menos un Administrador activo");
            }
            String sql = "UPDATE usuarios SET nombre = ?, rol = ?, activo = ?"
                    + (cambiaClave ? ", contrasena_hash = ?" : "") + " WHERE usuario = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                int i = 1;
                ps.setString(i++, nombre.trim());
                ps.setString(i++, rol.name());
                ps.setBoolean(i++, activo);
                if (cambiaClave) ps.setString(i++, HashUtil.sha256(contrasena));
                ps.setString(i, u.getUsuario());
                ps.executeUpdate();
            }
            return null;
        });
        if (soyYo) yo.setNombre(nombre.trim());
    }


    private Usuario buscar(String usuario) {
        return Db.consulta(c -> buscar(c, usuario));
    }

    private Usuario buscar(Connection c, String usuario) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNAS + " FROM usuarios WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? leer(rs) : null; }
        }
    }

    private Usuario leer(ResultSet rs) throws SQLException {
        return new Usuario(rs.getString("nombre"), rs.getString("usuario"), rs.getString("contrasena_hash"),
                Rol.valueOf(rs.getString("rol")), rs.getBoolean("activo"));
    }

    private void validarBasicos(String nombre, String usuario, Rol rol) {
        if (nombre == null || nombre.isBlank() || usuario == null || usuario.isBlank() || rol == null) {
            throw new NegocioException("Faltan datos obligatorios (nombre, usuario, contraseña y rol)");
        }
        if (usuario.trim().contains(" ")) throw new NegocioException("El nombre de usuario no puede tener espacios");
    }
}
