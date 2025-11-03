package entidades;

public class EmpleadoContratado extends Empleado {
	//DATOS
	private double valorHora;
	
	//CONSTRUCTOR
	public EmpleadoContratado(String nombre, int numLegajo, double valorHora) {
		super(nombre, numLegajo);	//llamamos al constructor padre (Empleado)
		this.valorHora = valorHora;
	}
	
	//calculo espeficio
	@Override
	public double calcularCosto(double cantDias) {
		//0.5 dia * 8 = 4h
		//1 dia * 8 == 8h
		//...
		return (cantDias * 8) * this.valorHora;
	}
}
