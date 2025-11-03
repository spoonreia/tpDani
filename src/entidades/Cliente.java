package entidades;

public class Cliente {
	//DATOS
	private String nombre;
	private String email;
	private String telefono;
	
	//CONSTRUCTOR
	public Cliente(String nombre, String email, String telefono) {
		this.nombre = nombre;
		this.email = email;
		this.telefono = telefono;
	}
	
	//GETTERS PARA EL toString()
	public String getNombre() {
		return nombre;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getTelefono() {
		return telefono;
	}
	
	@Override
	public String toString() {
		return "Nombre: " + nombre + " ( email: " + email + ", tel: " + telefono + ")";
	}
}
