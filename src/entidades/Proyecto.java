package entidades;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Proyecto {
	//datos
	private int numID;
	private String domicilio;
	private Cliente cliente;
	private String estado;
	
	private LocalDate fechaInicio;
	private LocalDate fechaEstimadaFin;
	private LocalDate fechaRealFin;
	private LocalDate fechaEstimadaFinInicial;
	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
	
	//para buscar tareas en O(1)
	private HashMap<String, Tarea> tareas;
	
	//para guardar el historial de empleados en 0(1)
	private Set<Empleado> historialEmpleados;
	
	private double costoFinal;
	
	public Proyecto(int numID, String domicilio, Cliente cliente, String inicio, String fin) {
		this.numID = numID;
		this.domicilio = domicilio;
		this.cliente = cliente;
		
		this.fechaInicio = LocalDate.parse(inicio, formatter);
		this.fechaEstimadaFin = LocalDate.parse(fin, formatter);
		this.fechaEstimadaFinInicial = LocalDate.parse(fin, formatter);
		this.fechaRealFin = LocalDate.parse(fin, formatter);
		
		this.estado = Estado.pendiente;
		this.costoFinal = 0;
		
		this.tareas = new HashMap<>();
		this.historialEmpleados = new HashSet<Empleado>();
	}
	
	public int getNumID() {
        return numID;
    }
    public String getDomicilio() {
        return domicilio;
    }
    public String getEstado() {
        return estado;
    }
    public Cliente getCliente() {
        return cliente;
    }
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    
    public Tarea getTarea(String tituloID) { //trae tarea por titulo
    	return this.tareas.get(tituloID);
    }
    
    public Object[] getTareas() {	//la GUI (GestionProyectos.java) lo necesita como Object[]
    	return this.tareas.values().toArray();
    }
    
    public void setEstado(String nuevoEstado) { //cambia el estado del proyecto
    	this.estado = nuevoEstado;
    }
    
    public boolean estaFinalizado() {	//verifica que este finalizado
    	return this.estado.equals(Estado.finalizado);
    }
    
    public void finalizarProyecto(String fechaFin) {
    	this.estado = Estado.finalizado;
    	this.fechaRealFin = LocalDate.parse(fechaFin, DateTimeFormatter.ISO_LOCAL_DATE);
    	
    	//recalcula costoFinal
    	this.costoFinal = this.calcularCostoTotal();	//lo guarda
    }
    
    public boolean todasLasTareasFinalizadas() {
    	if(tareas.isEmpty()) {	//no se puede finalizar si un proyecto no tiene tareas
    		return false;
    	}
    	
    	for(Tarea t : this.tareas.values()) {
    		if(!t.getEstado().equals(Estado.finalizado)) {	//si encuentra una sin finalizar
    			return false;
    		}
    	}
    	return true; //todas finalizadas
    }
    
    public Object[] getTareasNoAsignadas() {
    	List<Tarea> noAsignadas = new ArrayList<>();	//crea una lista temporal
    	
    	for(Tarea t : this.tareas.values()) {
    		if(t.getResponsable() == null) {	//si encuentra una tarea sin responsable la agrega a la lista
    			noAsignadas.add(t);
    		}
    	}
    	return noAsignadas.toArray();
    }
    
    public void agregarTarea(Tarea nuevaTarea) {
    	this.tareas.put(nuevaTarea.getTituloID(), nuevaTarea); //agrega tarea al HashMap
    	
    	double dias = Math.ceil(nuevaTarea.getDiasEstimados()); //redondea 0.5 a 1 para calcular los dias
    	
    	this.fechaEstimadaFin = this.fechaEstimadaFin.plusDays((long) dias); //suma los dias de la tarea a la fecha estimada
    	this.fechaRealFin = this.fechaRealFin.plusDays((long) dias); // al principio la estimada y real son iguales
    }
    
    public void asignarEmpleadoATarea(Tarea t, Empleado e) {
    	t.asignarResponsable(e);	//guarda al responsable en la tarea
    	this.historialEmpleados.add(e);	//agrega a la lista de empleados
    }
    
    public void registrarRetraso(Tarea t, double dias) {
    	t.registrarRetraso(dias);	//agrega 1 dia a la tarea
    	
    	long redondearDias = (long) Math.ceil(dias);
    	this.fechaRealFin = this.fechaRealFin.plusDays(redondearDias); //agrega los dias a la fechaRealFin
    }
    
    public Set<Empleado> getHistorialEmpleados(){	//requisito de IHomeSolution
    	return this.historialEmpleados;
    }
    
    public double getCostoFinal() {
    	//si el proyecto no esta finalizado, calula el costo del momento sin incrementos
    	if(!this.estaFinalizado()) {
    		return this.calcularCostoTotal();
    	}
    	
    	return this.costoFinal;
    }
    
    public double calcularCostoTotal() {
        double costoBase = 0;
        boolean huboRetraso = false;
                  
        // Primero verificar si hay retrasos en las tareas
        for(Tarea t : this.tareas.values()) {
            costoBase += t.calcularCosto(); //POLIMORFISMO usando Empleado.calcularCosto()
            if(t.huboRetraso()) {
                huboRetraso = true;
            }
        }
        
        if (this.estaFinalizado() && this.fechaRealFin.isAfter(this.fechaEstimadaFinInicial)) {
            huboRetraso = true;
        }
        
        double bonusPlanta = 0;
        for(Empleado e : this.historialEmpleados) {
            //verifica si es de planta y si no tuvo retrasos
            if(e instanceof EmpleadoDePlanta && e.getCantRetrasos() == 0) {
                double costoTareas = 0;
                for(Tarea t : this.tareas.values()) {
                    if(e.equals(t.getResponsable())) {
                        costoTareas += t.calcularCosto();
                    }
                }
                bonusPlanta += costoTareas * 0.02;
            }
        }
        
        double costoIntermedio = costoBase + bonusPlanta;
        
        double bonusGeneral;
        if(huboRetraso) {
            bonusGeneral = 0.25; //25%
        } else {
            bonusGeneral = 0.35; //35%
        }
        
        return costoIntermedio + (costoIntermedio * bonusGeneral);
    }
    

    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();	//Pide usar StringBuilder
    	
    	sb.append("PROYECTO N°: ").append(this.numID).append("\n");
        sb.append("DOMICILIO: ").append(this.domicilio).append("\n");
        sb.append("ESTADO: ").append(this.estado).append("\n");
        sb.append("CLIENTE: ").append(this.cliente.toString()).append("\n\n");
        
        sb.append("--- FECHAS ---\n");
        sb.append("Inicio: ").append(this.fechaInicio.toString()).append("\n");
        sb.append("Fin Previsto: ").append(this.fechaEstimadaFin.toString()).append("\n");
        sb.append("Fin Real: ").append(this.fechaRealFin.toString()).append("\n\n");
        
        sb.append("--- TAREAS ---\n");
        if (this.tareas.isEmpty()) {
            sb.append(" (Sin tareas asignadas)\n");
        }
        for (Tarea t : this.tareas.values()) {
            sb.append("- ").append(t.getTituloID()).append(" (");
            sb.append(t.getEstado()).append(")\n");
            sb.append("  Responsable: ");
            if (t.getResponsable() != null) {
                sb.append(t.getResponsable().getNombre());
                sb.append(" (Leg: ").append(t.getResponsable().getNumLegajo()).append(")\n");
            } else {
                sb.append("--- SIN ASIGNAR ---\n");
            }
        }
        
        sb.append("\n--- COSTO TOTAL (al momento) ---\n");
        //formatea el costo a 2 decimales
        sb.append("$ ").append(String.format("%.2f", this.getCostoFinal()));

        return sb.toString();
    }
}
