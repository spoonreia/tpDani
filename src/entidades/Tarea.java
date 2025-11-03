package entidades;

import java.util.Objects;

public class Tarea {
	//DATOS
	private String tituloID;
	private String descripcion;
	
	private double cantDias;
	private double cantRetrasos;
	
	private Empleado responsable;
	
	private String estado;
	
	//CONSTRUCTOR
	public Tarea(String tituloID, String descripcion, double cantDias) {
		this.tituloID = tituloID;
		this.descripcion = descripcion;
		this.cantDias = cantDias;
		
		this.cantRetrasos = 0;
		this.responsable = null;
		this.estado = Estado.pendiente;
	}
	
	
    // ------------------------------ SET Y SET  ------------------------------ 
	public String getTituloID() {
		return tituloID;
	}
	
	public Empleado getResponsable() {
		return responsable;
	}
	
	public String getEstado() {
		return estado;
	}
	
	public double getDiasTotales() {
		return this.cantDias + this.cantRetrasos;
	}
	
	public double getDiasEstimados() {
		return this.cantDias;
	}
	
	
    // ------------------------------ OTROS  ------------------------------ 
	public void asignarResponsable(Empleado e) {
		this.responsable = e;
		this.estado = Estado.activo;
	}
	
	public Empleado quitarResponsable() {
		Empleado empleadoLiberado = this.responsable;
		this.responsable = null;
		this.estado = Estado.pendiente;
		return empleadoLiberado;
	}
	
	public void finalizarTarea() {
		this.estado = Estado.finalizado;
	}
	
	public void registrarRetraso(double dias) {
		this.cantRetrasos += dias;
		
		//si se registra un retraso se le incrementa un retraso al responsable
		if(this.responsable != null) {
			this.responsable.registrarRetraso();
		}
	}
	
	public boolean huboRetraso() {
		return this.cantRetrasos > 0;
	}
	
	public double calcularCosto() {	//costo total
		if(this.responsable == null) {
			return 0;
		}
		return this.responsable.calcularCosto(this.getDiasTotales());
	}
	
	@Override
	public String toString() { //pedido por el enunciado e interfaz
		return this.tituloID;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Tarea tarea = (Tarea) o;
		return Objects.equals(tituloID, tarea.tituloID); //compara por título
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tituloID); //hash basado en el titulo
	}
}
