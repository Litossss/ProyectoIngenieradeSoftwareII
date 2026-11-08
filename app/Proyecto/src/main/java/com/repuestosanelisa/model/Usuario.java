package com.repuestosanelisa.model;

/** Cuenta de acceso (RF04 / RF06). La contraseña se guarda solo como hash SHA-256. */
public class Usuario {
    private String nombre;
    private final String usuario;
    private String contrasenaHash;
    private Rol rol;
    private boolean activo;

    public Usuario(String nombre, String usuario, String contrasenaHash, Rol rol, boolean activo) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.activo = activo;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsuario() { return usuario; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String h) { this.contrasenaHash = h; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getEstado() { return activo ? "Activo" : "Inactivo"; }
}
