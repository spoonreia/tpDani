package entidades;

public class Cliente {
	//datos
	private String nombre;
	private String email;
	private String telefono;
	
	//constructor
	public Cliente(String nombre, String email, String telefono) {
		this.nombre = nombre;
		this.email = email;
		this.telefono = telefono;
	}
	
	//getters para usar en toString()
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
