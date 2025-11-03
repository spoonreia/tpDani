package entidades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeSolution implements IHomeSolution {
    
    //DATOS
    private HashMap<Integer, Empleado> empleados; //clave = numLegajo
    private HashMap<Integer, Proyecto> proyectos; //clave = numID
    private int contadorLegajos;	//para que los legajos y proyectos sean unicos
    private int contadorProyectos;
    
    //CONSTRUCTOR
    public HomeSolution() {
        this.empleados = new HashMap<>();
        this.proyectos = new HashMap<>();
        this.contadorLegajos = 1000;
        this.contadorProyectos = 1;
    }
    
    
    // ------------------------------ REGISTRO DE EMPLEADOS ------------------------------

    private void validarDatosEmpleado(String nombre, double valor) {	//valida que nombre no sea vacio y valor > 0
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empleado no puede ser nulo o vacío");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a 0");
        }
    }
    
    private int generarNuevoLegajo() {
        return this.contadorLegajos++;
    }
    
    @Override
    public void registrarEmpleado(String nombre, double valor) throws IllegalArgumentException {
        validarDatosEmpleado(nombre, valor);
        
        int nuevoLegajo = generarNuevoLegajo();
        EmpleadoContratado empleado = new EmpleadoContratado(nombre, nuevoLegajo, valor);	//crea empleadoContratado con legajo unico
        this.empleados.put(nuevoLegajo, empleado);	//lo agrega a empleados
    }
    
    private void validarCategoria(String categoria) {	//verifica que la categoria puesta sea INICIAL, TECNICO o EXPERTO o que no sea nula
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }
        if (!categoria.equals("INICIAL") && !categoria.equals("TECNICO") && !categoria.equals("EXPERTO")) {
            throw new IllegalArgumentException("Categoría inválida. Debe ser INICIAL, TECNICO o EXPERTO");
        }
    }
    
    @Override
    public void registrarEmpleado(String nombre, double valor, String categoria) throws IllegalArgumentException {
        validarDatosEmpleado(nombre, valor);
        validarCategoria(categoria);
        
        int nuevoLegajo = generarNuevoLegajo();
        EmpleadoDePlanta empleado = new EmpleadoDePlanta(nombre, nuevoLegajo, valor, categoria);	//crea empleadoDePlanta con legajo unico
        this.empleados.put(nuevoLegajo, empleado);	//lo agrega a empleados
    }

    
    // ------------------------------ REGISTRO Y GESTION DE PROYECTOS ------------------------------
    
    
    private void validarDatosProyecto(String[] titulos, String[] descripcion, double[] dias,
            String domicilio, String[] cliente, String inicio, String fin) {	//valida todos los datos que se pongan al crear un proyecto
    	
			if (titulos == null || titulos.length == 0) {
				throw new IllegalArgumentException("Debe haber al menos una tarea");
			}
			if (descripcion == null || dias == null) {
				throw new IllegalArgumentException("Los arrays no pueden ser nulos");
			}
			if (domicilio == null || domicilio.trim().isEmpty()) {
				throw new IllegalArgumentException("El domicilio no puede ser nulo o vacío");
			}
			if (cliente == null || cliente.length != 3) {
				throw new IllegalArgumentException("Los datos del cliente son inválidos");
			}
			if (inicio == null || fin == null) {
				throw new IllegalArgumentException("Las fechas no pueden ser nulas");
			}
			if (fin.compareTo(inicio) < 0) {
				throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
			}

			//valida que todos los días sean positivos
			for (double dia : dias) {
				if (dia <= 0) {
					throw new IllegalArgumentException("Los días deben ser mayores a 0");
				}
			}
	}
    
    private int generarNuevoNumProyecto() {
        return this.contadorProyectos++;
    }
    
    @Override
    public void registrarProyecto(String[] titulos, String[] descripcion, double[] dias,
                                  String domicilio, String[] cliente, String inicio, String fin)
            throws IllegalArgumentException {
        
        validarDatosProyecto(titulos, descripcion, dias, domicilio, cliente, inicio, fin);
        
        int nuevoNumID = generarNuevoNumProyecto();
        Cliente nuevoCliente = new Cliente(cliente[0], cliente[1], cliente[2]);	//se pasa el nombre, email y telefono como se ve en Main.java
        Proyecto proyecto = new Proyecto(nuevoNumID, domicilio, nuevoCliente, inicio, fin);	//crea el proyecto
        
        // Agregar las tareas al proyecto
        for (int i = 0; i < titulos.length; i++) {
            Tarea tarea = new Tarea(titulos[i], descripcion[i], dias[i]);
            proyecto.agregarTarea(tarea);
        }
        
        this.proyectos.put(nuevoNumID, proyecto);	//suma el proyecto
    }
    
    // ------------------------------ ASIGNACION Y GESTION DE TAREAS ------------------------------ 
    
    @Override
    public void asignarResponsableEnTarea(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);
        verificarProyectoNoFinalizado(proyecto);
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        verificarTareaNoAsignada(tarea);	//ve si tiene empleado
        
        Empleado empleadoDisponible = buscarPrimerEmpleadoDisponible();
        if (empleadoDisponible == null) {	//si no hay empleados disponibles
            proyecto.setEstado(Estado.pendiente);
            throw new Exception("No hay empleados disponibles");
        }
        
        //si lo encontro, asigna empleado
        empleadoDisponible.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, empleadoDisponible);	//lo termina de asignar
        
        //si todas las tareas estan asignadas, cambiar estado a ACTIVO
        if (proyecto.getTareasNoAsignadas().length == 0) {
            proyecto.setEstado(Estado.activo);
        }
    }
    
    @Override
    public void asignarResponsableMenosRetraso(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);
        verificarProyectoNoFinalizado(proyecto);
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        verificarTareaNoAsignada(tarea);	//ve si tiene empleado
        
        Empleado empleadoMenosRetrasos = buscarEmpleadoConMenosRetrasos();
        if (empleadoMenosRetrasos == null) {	//si no hay empleados disponibles
            proyecto.setEstado(Estado.pendiente);
            throw new Exception("No hay empleados disponibles");
        }
        
        //si encontro, asigna empleado
        empleadoMenosRetrasos.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, empleadoMenosRetrasos);
        
        //si todas las tareas estan asignadas, cambiar estado a ACTIVO
        if (proyecto.getTareasNoAsignadas().length == 0) {
            proyecto.setEstado(Estado.activo);
        }
    }
    
    @Override
    public void registrarRetrasoEnTarea(Integer numero, String titulo, double cantidadDias)
            throws IllegalArgumentException {
        Proyecto proyecto = obtenerProyecto(numero);	//obtiene proyecto por numID
        
        if (cantidadDias <= 0) {
            throw new IllegalArgumentException("La cantidad de días debe ser mayor a 0");
        }
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);	//verifica que exista
        
        proyecto.registrarRetraso(tarea, cantidadDias);
    }
    
    @Override
    public void agregarTareaEnProyecto(Integer numero, String titulo, String descripcion, double dias)
            throws IllegalArgumentException {
        Proyecto proyecto = obtenerProyecto(numero);	//obtiene proyecto por numID
        verificarProyectoNoFinalizado(proyecto);	//verifica si no termino
        
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede ser nulo o vacío");
        }
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días deben ser mayores a 0");
        }
        
        Tarea nuevaTarea = new Tarea(titulo, descripcion, dias);
        proyecto.agregarTarea(nuevaTarea);	//la agrega al proyecto
    }
    
    @Override
    public void finalizarTarea(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);	//obtiene proyecto por numID
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);	//verificar si la tarea existe
        
        if (tarea.getEstado().equals(Estado.finalizado)) {	//verifica si la tarea ya fue finalizada
            throw new Exception("La tarea ya está finalizada");
        }
        
        //libera al empleado
        Empleado responsable = tarea.getResponsable();
        if (responsable != null) {
            responsable.cambiarADisponible();
        }
        
        tarea.finalizarTarea();
    }
    
    @Override
    public void finalizarProyecto(Integer numero, String fin) throws IllegalArgumentException {
        Proyecto proyecto = obtenerProyecto(numero);	//obtiene proyecto por numID
        
        if (fin == null || fin.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de finalización no puede ser nula");
        }
        
        //valida que la fechaRealfin sea posterior o igual a la fechaInicio
        if (fin.compareTo(proyecto.getFechaInicio().toString()) < 0) {
            throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a la fecha de inicio");
        }
        
        if (proyecto.getEstado().equals(Estado.pendiente)) {
            throw new IllegalArgumentException("No se puede finalizar un proyecto pendiente");
        }
        
        //libera todos los empleados asignados a tareas del proyecto
        for (Object obj : proyecto.getTareas()) {
            Tarea t = (Tarea) obj;
            if (t.getResponsable() != null) {
                t.getResponsable().cambiarADisponible();
                if (!t.getEstado().equals(Estado.finalizado)) {	//finaliza la tarea si no esta finalizada
                    t.finalizarTarea();
                }
            }
        }
        
        proyecto.finalizarProyecto(fin);	//cambia a finalizado guarda la fecha, guarda la fechaRealFin y calcula el costoFinal
    }
    
    
    // ------------------------------ REASIGNACION DE EMPLEADOS ------------------------------ 
    
    @Override
    public void reasignarEmpleadoEnProyecto(Integer numero, Integer legajo, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);	//obtiene proyecto por numID
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        
        if (tarea.getResponsable() == null) {
            throw new Exception("La tarea no tiene un empleado asignado previamente");
        }
        
        Empleado nuevoEmpleado = this.empleados.get(legajo);
        if (nuevoEmpleado == null) {
            throw new Exception("El empleado con legajo " + legajo + " no existe");
        }
        if (!nuevoEmpleado.estaDisponible()) {
            throw new Exception("El empleado no está disponible");
        }
        
        //libera empleado anterior
        Empleado empleadoAnterior = tarea.quitarResponsable();
        empleadoAnterior.cambiarADisponible();
        
        //asigna nuevo empleado
        nuevoEmpleado.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, nuevoEmpleado);
    }
    
    @Override
    public void reasignarEmpleadoConMenosRetraso(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);	//obtiene proyecto por numID
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        
        if (tarea.getResponsable() == null) {
            throw new Exception("La tarea no tiene un empleado asignado previamente");
        }
        
        Empleado empleadoMenosRetrasos = buscarEmpleadoConMenosRetrasos();
        if (empleadoMenosRetrasos == null) {
            throw new Exception("No hay empleados disponibles");
        }
        
        //libera empleado anterior
        Empleado empleadoAnterior = tarea.quitarResponsable();
        empleadoAnterior.cambiarADisponible();
        
        //asigna nuevo empleado
        empleadoMenosRetrasos.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, empleadoMenosRetrasos);
    }
    
    // ------------------------------ CONSULTAS Y REPORTES ------------------------------ 
    
    @Override
    public double costoProyecto(Integer numero) {	//calcula el costo  del proyecto
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.getCostoFinal();
    }
    
    @Override
    public List<Tupla<Integer, String>> proyectosFinalizados() {
        List<Tupla<Integer, String>> finalizados = new ArrayList<>();	//crea lista de Tupla
        
        for (Proyecto p : this.proyectos.values()) {	//recorre y pregunta los estados de los proyectos para añadirlo o no
            if (p.estaFinalizado()) {
                finalizados.add(new Tupla<>(p.getNumID(), p.getDomicilio()));
            }
        }
        
        return finalizados;
    }
    
    @Override
    public List<Tupla<Integer, String>> proyectosPendientes() {
        List<Tupla<Integer, String>> pendientes = new ArrayList<>();	//crea lista de Tupla
        
        for (Proyecto p : this.proyectos.values()) {	//recorre y pregunta los estados de los proyectos para añadirlo o no
            if (p.getEstado().equals(Estado.pendiente)) {
                pendientes.add(new Tupla<>(p.getNumID(), p.getDomicilio()));
            }
        }
        
        return pendientes;
    }
    
    @Override
    public List<Tupla<Integer, String>> proyectosActivos() {
        List<Tupla<Integer, String>> activos = new ArrayList<>();	//crea lista de Tupla
        
        for (Proyecto p : this.proyectos.values()) {	//recorre y pregunta los estados de los proyectos para añadirlo o no
            if (p.getEstado().equals(Estado.activo)) {
                activos.add(new Tupla<>(p.getNumID(), p.getDomicilio()));
            }
        }
        
        return activos;
    }
    
    @Override
    public Object[] empleadosNoAsignados() {	//recorre empleados y devuelve los que estan disponibles
        List<Integer> noAsignados = new ArrayList<>();
        
        for (Empleado e : this.empleados.values()) {
            if (e.estaDisponible()) {
                noAsignados.add(e.getNumLegajo());
            }
        }
        
        return noAsignados.toArray();
    }
    
    @Override
    public boolean estaFinalizado(Integer numero) {	//devuelve el estado del proyecto
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.estaFinalizado();
    }
    
    @Override
    public int consultarCantidadRetrasosEmpleado(Integer legajo) {	//devuelve los retrasos que tuvo un empleado
        Empleado empleado = this.empleados.get(legajo);
        if (empleado == null) {
        	throw new IllegalArgumentException("Empleado no puede ser null");
        }
        return empleado.getCantRetrasos();
    }
    
    @Override
    public List<Tupla<Integer, String>> empleadosAsignadosAProyecto(Integer numero) {	//devuelve la lista de los empleados asignados a un proyecto
        Proyecto proyecto = obtenerProyecto(numero);
        List<Tupla<Integer, String>> empleadosAsignados = new ArrayList<>();
        
        for (Empleado e : proyecto.getHistorialEmpleados()) {
            empleadosAsignados.add(new Tupla<>(e.getNumLegajo(), e.getNombre()));
        }
        
        return empleadosAsignados;
    }
    
    
    // ------------------------------ NUEVOS REQUERIMIENTOS ------------------------------ 
    
    @Override
    public Object[] tareasProyectoNoAsignadas(Integer numero) {
        Proyecto proyecto = obtenerProyecto(numero);
        
        // Si el proyecto está finalizado, lanzar excepción
        if (proyecto.estaFinalizado()) {
            throw new IllegalArgumentException("No se puede consultar tareas no asignadas de un proyecto finalizado");
        }
        
        return proyecto.getTareasNoAsignadas();
    }
    
    @Override
    public Object[] tareasDeUnProyecto(Integer numero) {
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.getTareas();
    }
    
    @Override
    public String consultarDomicilioProyecto(Integer numero) {
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.getDomicilio();
    }
    
    @Override
    public boolean tieneRestrasos(Integer legajo) {
        Empleado empleado = this.empleados.get(legajo);
        if (empleado == null) {
            return false;
        }
        return empleado.tieneRetrasos();
    }
    
    @Override
    public List<Tupla<Integer, String>> empleados() {	//devuelve lista de empleados
        List<Tupla<Integer, String>> listaEmpleados = new ArrayList<>();
        
        for (Empleado e : this.empleados.values()) {
            listaEmpleados.add(new Tupla<>(e.getNumLegajo(), e.getNombre()));
        }
        
        return listaEmpleados;
    }
    
    @Override
    public String consultarProyecto(Integer numero) {	//devuelve el toString del proyecto con sus datos
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.toString();
    }
    
    // ------------------------------ METODOS AUXILIARES ------------------------------ 
    
    private Proyecto obtenerProyecto(Integer numero) {
        Proyecto proyecto = this.proyectos.get(numero);	//obtiene proyecto por id
        if (proyecto == null) {	//verifica que exista
            throw new IllegalArgumentException("El proyecto con número " + numero + " no existe");
        }
        return proyecto;
    }
    
    private void verificarProyectoNoFinalizado(Proyecto proyecto) throws IllegalArgumentException {
        if (proyecto.estaFinalizado()) {
            throw new IllegalArgumentException("No se pueden realizar operaciones en un proyecto finalizado");
        }
    }
    
    private void verificarTareaExiste(Tarea tarea, String titulo) {
        if (tarea == null) {
            throw new IllegalArgumentException("La tarea '" + titulo + "' no existe en el proyecto");
        }
    }
    
    private void verificarTareaNoAsignada(Tarea tarea) throws Exception {
        if (tarea.getResponsable() != null) {
            throw new Exception("La tarea ya tiene un empleado asignado");
        }
    }
    
    private Empleado buscarPrimerEmpleadoDisponible() {
        for (Empleado e : this.empleados.values()) {
            if (e.estaDisponible()) {
                return e;
            }
        }
        return null;
    }
    
    private Empleado buscarEmpleadoConMenosRetrasos() {
        Empleado empleadoSeleccionado = null;
        int menorCantidadRetrasos = Integer.MAX_VALUE;
        
        //primero busca empleados sin retrasos
        for (Empleado e : this.empleados.values()) {
            if (e.estaDisponible() && e.getCantRetrasos() == 0) {
                return e;
            }
        }
        
        //si no hay sin retrasos, busca el que menos tenga
        for (Empleado e : this.empleados.values()) {
            if (e.estaDisponible() && e.getCantRetrasos() < menorCantidadRetrasos) {
                empleadoSeleccionado = e;
                menorCantidadRetrasos = e.getCantRetrasos();
            }
        }
        return empleadoSeleccionado;
    }
}