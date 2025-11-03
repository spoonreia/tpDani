package entidades;

public class EmpleadoDePlanta extends Empleado{
	//DATOS
	private double valorDia;
	private String categoria;
	
	//CONSTRUCTOR
	public EmpleadoDePlanta(String nombre, int numLegajo, double valorDia, String categoria) {
		super(nombre, numLegajo);	//llamamos al constructor padre (Empleado)
		
		this.valorDia = valorDia;
		this.categoria = categoria;
	}
	
	public String getCategoria() {
		return categoria;
	}
	
	//calculo especifico
	public double calcularCosto(double cantDias) {
		return Math.ceil(cantDias) * this.valorDia; //Math.ceil() porque si la tarea dura medio dia el valor seria incorrecto
	}
}
