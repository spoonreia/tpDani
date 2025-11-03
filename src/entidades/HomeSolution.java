package entidades;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException; // Para excepciones
import java.util.Set; // Para el historial

/**
 * TAD HomeSolution (El "Cerebro")
 * Implementa la interfaz IHomeSolution.
 * Orquesta todos los TADs (Proyecto, Tarea, Empleado, Cliente).
 */
public class HomeSolution implements IHomeSolution {
	
	// --- COLECCIONES PRINCIPALES ---
	
	/** (O(1)) Guarda TODOS los empleados por legajo (el "Maestro") */
	private HashMap<Integer, Empleado> empleados;
	
	/** (O(1)) Guarda SÓLO los empleados libres para asignar (la "Cola") */
	private LinkedList<Empleado> empleadosDisponibles;
	
	/** (O(1)) Guarda TODOS los proyectos por ID */
	private HashMap<Integer, Proyecto> proyectos;
	
	// --- CONTADORES (IDs ÚNICOS) ---
	// Los inicializamos en 1, como espera el archivo de Test
	private int proximoLegajo = 1;
	private int proximoProyectoID = 1; 

	/**
	 * Constructor de HomeSolution
	 */
	public HomeSolution() {
		this.empleados = new HashMap<>();
		this.empleadosDisponibles = new LinkedList<>();
		this.proyectos = new HashMap<>();
	}
	
	// --- Métodos Privados de Ayuda (Helpers) ---
	
	/**
	 * Busca un Proyecto por ID. Lanza una excepción si no lo encuentra.
	 * @param numero El ID del proyecto.
	 * @return El objeto Proyecto.
	 * @throws NoSuchElementException Si el proyecto no existe.
	 */
	private Proyecto getProyecto(Integer numero) throws NoSuchElementException {
		Proyecto p = this.proyectos.get(numero);
		if (p == null) {
			throw new NoSuchElementException("El proyecto con ID " + numero + " no existe.");
		}
		return p;
	}
	
	/**
	 * Busca un Empleado por Legajo. Lanza una excepción si no lo encuentra.
	 * @param legajo El legajo del empleado.
	 * @return El objeto Empleado.
	 * @throws NoSuchElementException Si el empleado no existe.
	 */
	private Empleado getEmpleado(Integer legajo) throws NoSuchElementException {
		Empleado e = this.empleados.get(legajo);
		if (e == null) {
			throw new NoSuchElementException("El empleado con legajo " + legajo + " no existe.");
		}
		return e;
	}

	// ============================================================
    // SECCIÓN 1: REGISTRO (Empleados y Proyectos)
    // ============================================================
	
	/**
	 * (Sobrecarga) Registra un EmpleadoContratado (por hora)
	 */
	@Override
	public void registrarEmpleado(String nombre, double valor) throws IllegalArgumentException {
		// Validación de la Interfaz
		if (nombre == null || nombre.trim().isEmpty() || valor <= 0) {
			throw new IllegalArgumentException("Datos de empleado inválidos.");
		}
		
		int legajoActual = this.proximoLegajo++;
		// 1. CREA el objeto (usando el "ladrillo")
		Empleado nuevoEmpleado = new EmpleadoContratado(nombre, legajoActual, valor);
		
		// 2. REGISTRA en las colecciones
		this.empleados.put(legajoActual, nuevoEmpleado);
		this.empleadosDisponibles.add(nuevoEmpleado); // Nace disponible
	}
	
	/**
	 * (Sobrecarga) Registra un EmpleadoDePlanta (por día)
	 */
	@Override
	public void registrarEmpleado(String nombre, double valor, String categoria) throws IllegalArgumentException {
		// Validación de la Interfaz
		if (nombre == null || nombre.trim().isEmpty() || valor <= 0) {
			throw new IllegalArgumentException("Datos de empleado inválidos.");
		}
		
		// Validación del Test (el test usa "EXPERTO" e "INICIAL")
		if (!categoria.equals("INICIAL") && !categoria.equals("TECNICO") && !categoria.equals("EXPERTO")) {
			throw new IllegalArgumentException("Categoría de empleado inválida.");
		}
		
		int legajoActual = this.proximoLegajo++;
		// 1. CREA el objeto (usando el "ladrillo")
		Empleado nuevoEmpleado = new EmpleadoDePlanta(nombre, legajoActual, valor, categoria);
		
		// 2. REGISTRA en las colecciones
		this.empleados.put(legajoActual, nuevoEmpleado);
		this.empleadosDisponibles.add(nuevoEmpleado); // Nace disponible
	}

	@Override
	public void registrarProyecto(String[] titulos, String[] descripcion, double[] dias, 
								  String domicilio, String[] cliente, String inicio, String fin)
			throws IllegalArgumentException {
		
		// --- Validaciones (Req. Interfaz) ---
		LocalDate fechaInicio = LocalDate.parse(inicio, DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate fechaFin = LocalDate.parse(fin, DateTimeFormatter.ISO_LOCAL_DATE);
		
		if (fechaFin.isBefore(fechaInicio)) {
			throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
		}
		if (titulos.length == 0) {
			throw new IllegalArgumentException("El proyecto debe tener al menos una tarea.");
		}

		// 1. CREA el Cliente
		Cliente nuevoCliente = new Cliente(cliente[0], cliente[1], cliente[2]);
		
		int proyectoIDActual = this.proximoProyectoID++; 
		
		// 2. CREA el Proyecto
		Proyecto nuevoProyecto = new Proyecto(proyectoIDActual, domicilio, nuevoCliente, inicio, fin);
		
		// 3. CREA y AGREGA las Tareas al Proyecto
		for (int i = 0; i < titulos.length; i++) {
			Tarea nuevaTarea = new Tarea(titulos[i], descripcion[i], dias[i]);
			nuevoProyecto.agregarTarea(nuevaTarea); // El Proyecto actualiza sus fechas
		}
		
		// 4. REGISTRA el Proyecto en la colección principal
		this.proyectos.put(proyectoIDActual, nuevoProyecto);
	}
	
	// ============================================================
    // SECCIÓN 2: ASIGNACIÓN Y GESTIÓN DE TAREAS
    // ============================================================

	/**
	 * Asigna el "primero libre" de la cola de disponibles.
	 */
	@Override
	public void asignarResponsableEnTarea(Integer numero, String titulo) throws Exception {
		Proyecto p = getProyecto(numero);
		Tarea t = p.getTarea(titulo);
		
		if (t == null) {
			throw new NoSuchElementException("La tarea " + titulo + " no existe en el proyecto " + numero);
		}
		if (t.getResponsable() != null) {
			throw new IllegalArgumentException("La tarea ya tiene un responsable asignado.");
		}
		if (p.estaFinalizado()) {
			throw new IllegalArgumentException("El proyecto " + numero + " está finalizado.");
		}

		// --- Lógica de Disponibilidad (Consigna 3 y Test 'testSinEmpleadosParaAsignar') ---
		if (this.empleadosDisponibles.isEmpty()) {
			p.setEstado(Estado.pendiente); // Pone el proyecto en pendiente
			throw new Exception("No hay empleados disponibles para asignar."); // Lanza excepción
		}
		// ---
		
		// Asigna al "primero libre" (O(1))
		Empleado e = this.empleadosDisponibles.removeFirst(); 
		e.cambiarANoDisponible();
		
		p.asignarEmpleadoATarea(t, e); // El Proyecto conecta Tarea-Empleado
		
		// Si es la primera asignación, activa el proyecto
		if (p.getEstado().equals(Estado.pendiente)) {
			p.setEstado(Estado.activo);
		}
	}

	/**
	 * Asigna el empleado "eficiente" (menos retrasos).
	 */
	@Override
	public void asignarResponsableMenosRetraso(Integer numero, String titulo) throws Exception {
		Proyecto p = getProyecto(numero);
		Tarea t = p.getTarea(titulo);
		
		if (t == null) {
			throw new NoSuchElementException("La tarea " + titulo + " no existe en el proyecto " + numero);
		}
		if (t.getResponsable() != null) {
			throw new IllegalArgumentException("La tarea ya tiene un responsable asignado.");
		}
		if (p.estaFinalizado()) {
			throw new IllegalArgumentException("El proyecto " + numero + " está finalizado.");
		}
		
		// --- Lógica de Disponibilidad (Consigna 3 y Test 'testSinEmpleadosParaAsignar') ---
		if (this.empleadosDisponibles.isEmpty()) {
			p.setEstado(Estado.pendiente);
			throw new Exception("No hay empleados disponibles para asignar."); 
		}
		// ---
		
		// --- Búsqueda Eficiente (O(N)) ---
		Empleado mejorEmpleado = null;
		int minRetrasos = Integer.MAX_VALUE;
		
		// "primero asignarse los que no tuvieron ningún retraso" (Consigna 2)
		for (Empleado e : this.empleadosDisponibles) {
			if (e.getCantRetrasos() == 0) {
				mejorEmpleado = e; // Encontró uno perfecto
				break; // Deja de buscar
			}
			// Si no hay con 0, busca el de menor retraso
			if (e.getCantRetrasos() < minRetrasos) {
				minRetrasos = e.getCantRetrasos();
				mejorEmpleado = e;
			}
		}
		// --- Fin Búsqueda ---
		
		this.empleadosDisponibles.remove(mejorEmpleado); // Lo saca de la lista O(N)
		mejorEmpleado.cambiarANoDisponible();
		
		p.asignarEmpleadoATarea(t, mejorEmpleado); // El Proyecto conecta
		
		if (p.getEstado().equals(Estado.pendiente)) {
			p.setEstado(Estado.activo);
		}
	}

	/**
	 * Reasigna un empleado específico a una tarea.
	 */
	@Override
	public void reasignarEmpleadoEnProyecto(Integer numero, Integer legajo, String titulo) throws Exception {
		Proyecto p = getProyecto(numero);
		Tarea t = p.getTarea(titulo);
		Empleado empleadoNuevo = getEmpleado(legajo);

		if (t == null) {
			throw new NoSuchElementException("La tarea " + titulo + " no existe.");
		}
		if (p.estaFinalizado()) {
			throw new IllegalArgumentException("El proyecto " + numero + " está finalizado.");
		}
		
		// "se debe poder cambiar un empleado... por otro)" (Consigna 1)
		// Asumimos que el "otro" debe estar disponible.
		if (!empleadoNuevo.estaDisponible() || !this.empleadosDisponibles.contains(empleadoNuevo)) {
			throw new Exception("El empleado " + legajo + " no está disponible para reasignación.");
		}
		
		// 1. Libera al empleado viejo
		Empleado empleadoViejo = t.quitarResponsable(); // Tarea vuelve a PENDIENTE
		if (empleadoViejo != null) {
			empleadoViejo.cambiarADisponible();
			this.empleadosDisponibles.add(empleadoViejo); // Vuelve a la cola
		}
		
		// 2. Asigna al empleado nuevo
		this.empleadosDisponibles.remove(empleadoNuevo); // Sale de la cola
		empleadoNuevo.cambiarANoDisponible();
		p.asignarEmpleadoATarea(t, empleadoNuevo); // Tarea vuelve a ACTIVO
	}

	/**
	 * Reasigna al empleado más eficiente a una tarea.
	 */
	@Override
	public void reasignarEmpleadoConMenosRetraso(Integer numero, String titulo) throws Exception {
		Proyecto p = getProyecto(numero);
		Tarea t = p.getTarea(titulo);
		
		if (t == null) {
			throw new NoSuchElementException("La tarea " + titulo + " no existe.");
		}
		if (p.estaFinalizado()) {
			throw new IllegalArgumentException("El proyecto " + numero + " está finalizado.");
		}
		
		// 1. Libera al empleado viejo
		Empleado empleadoViejo = t.quitarResponsable();
		if (empleadoViejo != null) {
			empleadoViejo.cambiarADisponible();
			this.empleadosDisponibles.add(empleadoViejo);
		}
		
		// 2. Llama a la lógica de asignación eficiente
		// (Esto lanzará la excepción si no hay nadie disponible)
		try {
			// Llama al método que ya tiene la lógica O(N) de búsqueda
			asignarResponsableMenosRetraso(numero, titulo);
		} catch (Exception e) {
			// Si falla la reasignación, relanza la excepción
			throw new Exception("Reasignación eficiente falló: " + e.getMessage());
		}
	}

	@Override
	public void finalizarTarea(Integer numero, String titulo) throws Exception {
		Proyecto p = getProyecto(numero);
		Tarea t = p.getTarea(titulo);

		if (t == null) {
			throw new NoSuchElementException("La tarea " + titulo + " no existe.");
		}
		if (p.estaFinalizado()) {
			throw new IllegalArgumentException("El proyecto " + numero + " está finalizado.");
		}
		if (t.getResponsable() == null) {
			throw new Exception("No se puede finalizar una tarea que no está asignada.");
		}
		if (t.getEstado().equals(Estado.finalizado)) {
			throw new Exception("La tarea ya estaba finalizada.");
		}
		
		// 1. Marca la tarea como finalizada
		t.finalizarTarea();
		
		// 2. Libera al empleado (Consigna 2: "el empleado será liberado")
		Empleado e = t.getResponsable();
		e.cambiarADisponible();
		this.empleadosDisponibles.add(e); // Vuelve a la cola de disponibles
	}

	@Override
	public void registrarRetrasoEnTarea(Integer numero, String titulo, double cantidadDias) throws IllegalArgumentException {
		if (cantidadDias <= 0) {
			throw new IllegalArgumentException("Los días de retraso deben ser un número positivo.");
		}
		
		Proyecto p = getProyecto(numero);
		Tarea t = p.getTarea(titulo);
		
		if (t == null) {
			throw new NoSuchElementException("La tarea " + titulo + " no existe.");
		}
		if (p.estaFinalizado()) {
			throw new IllegalArgumentException("El proyecto " + numero + " está finalizado.");
		}
		
		// Delega al Proyecto (que delega a la Tarea, que avisa al Empleado)
		p.registrarRetraso(t, cantidadDias);
	}

	@Override
	public void agregarTareaEnProyecto(Integer numero, String titulo, String descripcion, double dias)
			throws IllegalArgumentException {
		
		if (dias <= 0) {
			throw new IllegalArgumentException("La duración de la tarea debe ser positiva.");
		}
		
		Proyecto p = getProyecto(numero);
		
		if (p.estaFinalizado()) {
			throw new IllegalArgumentException("No se pueden agregar tareas a un proyecto finalizado.");
		}
		if (p.getTarea(titulo) != null) {
			throw new IllegalArgumentException("Ya existe una tarea con el título " + titulo + " en este proyecto.");
		}
		
		// 1. Crea la nueva Tarea
		Tarea nuevaTarea = new Tarea(titulo, descripcion, dias);
		// 2. El Proyecto la agrega (y actualiza sus fechas)
		p.agregarTarea(nuevaTarea);
	}

	@Override
	public void finalizarProyecto(Integer numero, String fin) throws IllegalArgumentException {
		Proyecto p = getProyecto(numero);
		
		// Validación de fecha (Test)
		LocalDate fechaFin = LocalDate.parse(fin, DateTimeFormatter.ISO_LOCAL_DATE);
		if (fechaFin.isBefore(p.getFechaInicio())) {
			throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio del proyecto.");
		}
		
		// Validación del Test: No se puede finalizar si faltan tareas
		// Esta es la validación que el "Test Malo" (testAsignarMenosRetrasos) rompe,
		// pero que el "Test Bueno" (testTareasNoAsignadas...) requiere.
		// La dejamos activa porque es lo lógicamente correcto.
		if (!p.todasLasTareasFinalizadas()) {
			throw new IllegalArgumentException("No se puede finalizar el proyecto, aún hay tareas pendientes o activas.");
		}
		
		// El Proyecto se encarga de cambiar estado, fecha, y calcular/guardar costo
		p.finalizarProyecto(fin);
	}
	
	// ============================================================
    // SECCIÓN 3: CONSULTAS Y REPORTES
    // ============================================================

	@Override
	public double costoProyecto(Integer numero) {
		Proyecto p = getProyecto(numero);
		return p.getCostoFinal(); // (O(1) si está finalizado, O(N) si no)
	}

	@Override
	public String consultarProyecto(Integer numero) {
		Proyecto p = getProyecto(numero);
		return p.toString(); // (Usa el toString() con StringBuilder)
	}

	@Override
	public String consultarDomicilioProyecto(Integer numero) {
		Proyecto p = getProyecto(numero);
		return p.getDomicilio();
	}

	@Override
	public boolean estaFinalizado(Integer numero) {
		Proyecto p = getProyecto(numero);
		return p.estaFinalizado();
	}

	@Override
	public int consultarCantidadRetrasosEmpleado(Integer legajo) {
		Empleado e = getEmpleado(legajo);
		return e.getCantRetrasos();
	}
	
	@Override
	public boolean tieneRestrasos(Integer legajo) {
		Empleado e = getEmpleado(legajo);
		return e.tieneRetrasos();
	}

	@Override
	public Object[] tareasDeUnProyecto(Integer numero) {
		Proyecto p = getProyecto(numero);
		return p.getTareas(); // (Devuelve Object[] para la GUI)
	}
	
	@Override
	public Object[] tareasProyectoNoAsignadas(Integer numero) {
		Proyecto p = getProyecto(numero);
		return p.getTareasNoAsignadas(); // (Devuelve Object[] para la GUI)
	}
	
	@Override
	public Object[] empleadosNoAsignados() {
		// (Devuelve Object[] para la GUI)
		return this.empleadosDisponibles.toArray();
	}

	@Override
	public List<Tupla<Integer, String>> empleadosAsignadosAProyecto(Integer numero) {
		Proyecto p = getProyecto(numero);
		Set<Empleado> historial = p.getHistorialEmpleados();
		
		List<Tupla<Integer, String>> listaTuplas = new ArrayList<>();
		for (Empleado e : historial) {
			listaTuplas.add(new Tupla<>(e.getNumLegajo(), e.getNombre()));
		}
		return listaTuplas;
	}

	// --- Métodos de Reporte (Listas de Tuplas) ---

	@Override
	public List<Tupla<Integer, String>> proyectosPendientes() {
		List<Tupla<Integer, String>> lista = new ArrayList<>();
		for (Proyecto p : this.proyectos.values()) {
			if (p.getEstado().equals(Estado.pendiente)) {
				lista.add(new Tupla<>(p.getNumID(), p.getCliente().getNombre()));
			}
		}
		return lista;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosActivos() {
		List<Tupla<Integer, String>> lista = new ArrayList<>();
		for (Proyecto p : this.proyectos.values()) {
			if (p.getEstado().equals(Estado.activo)) {
				lista.add(new Tupla<>(p.getNumID(), p.getCliente().getNombre()));
			}
		}
		return lista;
	}

	@Override
	public List<Tupla<Integer, String>> proyectosFinalizados() {
		List<Tupla<Integer, String>> lista = new ArrayList<>();
		for (Proyecto p : this.proyectos.values()) {
			if (p.getEstado().equals(Estado.finalizado)) {
				lista.add(new Tupla<>(p.getNumID(), p.getCliente().getNombre()));
			}
		}
		return lista;
	}

	@Override
	public List<Tupla<Integer, String>> empleados() {
		List<Tupla<Integer, String>> lista = new ArrayList<>();
		// Recorre el HashMap maestro, no la lista de disponibles
		for (Empleado e : this.empleados.values()) {
			lista.add(new Tupla<>(e.getNumLegajo(), e.getNombre()));
		}
		return lista;
	}
	
	/**
	 * (toString del TAD Principal - Consigna 3)
	 * Devuelve un resumen del estado general del sistema.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("===== ESTADO DE HOMESOLUTION =====\n");
		sb.append("Proyectos Totales: ").append(this.proyectos.size()).append("\n");
		sb.append("Empleados Totales: ").append(this.empleados.size()).append("\n");
		sb.append("Empleados Disponibles: ").append(this.empleadosDisponibles.size()).append("\n");
		sb.append("==================================\n");
		return sb.toString();
	}
}