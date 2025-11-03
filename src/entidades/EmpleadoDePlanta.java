package entidades;

public class EmpleadoDePlanta extends Empleado{
	private double valorDia;
	private String categoria;
	
	public EmpleadoDePlanta(String nombre, int numLegajo, double valorDia, String categoria) {
		super(nombre, numLegajo);
		
		this.valorDia = valorDia;
		this.categoria = categoria;
	}
	
	public String getCategoria() {
		return categoria;
	}
	
	public double calcularCosto(double cantDias) {
		return Math.ceil(cantDias) * this.valorDia; //Math.ceil() porque si la tarea dura medio dia el valor seria incorrecto
	}
}
