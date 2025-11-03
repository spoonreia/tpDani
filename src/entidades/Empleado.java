package entidades;

import java.util.Objects;

public abstract class Empleado {
	//DATOS
	private String nombre;
	private int numLegajo;
	private boolean estaDisponible;
	private int cantRetrasos;
	
	//CONSTRUCTOR
	public Empleado(String nombre, int numLegajo) {
		this.nombre = nombre;
		this.numLegajo = numLegajo;
		this.estaDisponible = true;
		this.cantRetrasos = 0;
	}
	
	//METODOS DE GESTION COMPARTIDA entre EmpleadoContratado y EmpleadoDePlanta
    // ------------------------------ SET Y SET  ------------------------------ 
	public String getNombre() {
		return nombre;
	}
	
	public int getNumLegajo() {
		return numLegajo;
	}
	
	public boolean estaDisponible() {
		return estaDisponible;
	}
	
	public int getCantRetrasos() {
		return cantRetrasos;
	}
	
    // ------------------------------ OTROS  ------------------------------ 
	public void registrarRetraso() {
		this.cantRetrasos++;
	}
	
	public void cambiarADisponible() {
		this.estaDisponible = true;
	}
	
	public void cambiarANoDisponible() {
		this.estaDisponible = false;
	}
	
	public boolean tieneRetrasos() {
		return this.cantRetrasos > 0;
	}

	public abstract double calcularCosto(double cantDias); //en cada tipo de empleado es diferente
	
	@Override	//pedido por el enunciado e interfaz
	public boolean equals(Object o) {
		//mismo objeto exacto en memoria
		if (this == o) return true;
		
		//es nulo o de una clase diferente?
		if (o == null || getClass() != o.getClass()) return false;
		
		//conversión y comparación de la clave
		Empleado empleado = (Empleado) o;
		return numLegajo == empleado.numLegajo; //la clave es el numLegajo
	}
	
	@Override
	public int hashCode() {
        //para generar un hash a partir del legajo
		return Objects.hash(numLegajo);
	}
	
	@Override
	public String toString() {	//se espera que solo se devuelva el numero de legajo
		return String.valueOf(this.numLegajo); //lo convierte a cadena
	}
}
