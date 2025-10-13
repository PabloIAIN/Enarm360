package com.example.enarm360.dtos.auth;
import lombok.*; 
import java.util.Set;
@NoArgsConstructor  
@AllArgsConstructor 
@Data
@Builder  
public class UsuarioInfo {
    private Long id;
    private String username;
    private String email;
    private String nombre;
    private String apellidos;
    private Set<String> roles;
    private Set<String> permisos;
    private Boolean activo;
    private Boolean emailVerificado;
    private Boolean telefonoVerificado;
    private Boolean requiereVerificacion;
}
