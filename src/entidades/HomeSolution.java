package entidades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeSolution implements IHomeSolution {
    
    // Datos
    private HashMap<Integer, Empleado> empleados; // clave = numLegajo
    private HashMap<Integer, Proyecto> proyectos; // clave = numID
    private int contadorLegajos;
    private int contadorProyectos;
    
    // Constructor
    public HomeSolution() {
        this.empleados = new HashMap<>();
        this.proyectos = new HashMap<>();
        this.contadorLegajos = 1000;
        this.contadorProyectos = 1;
    }
    
    // ============================================================
    // REGISTRO DE EMPLEADOS
    // ============================================================
    
    @Override
    public void registrarEmpleado(String nombre, double valor) throws IllegalArgumentException {
        validarDatosEmpleado(nombre, valor);
        
        int nuevoLegajo = generarNuevoLegajo();
        EmpleadoContratado empleado = new EmpleadoContratado(nombre, nuevoLegajo, valor);
        this.empleados.put(nuevoLegajo, empleado);
    }
    
    @Override
    public void registrarEmpleado(String nombre, double valor, String categoria) throws IllegalArgumentException {
        validarDatosEmpleado(nombre, valor);
        validarCategoria(categoria);
        
        int nuevoLegajo = generarNuevoLegajo();
        EmpleadoDePlanta empleado = new EmpleadoDePlanta(nombre, nuevoLegajo, valor, categoria);
        this.empleados.put(nuevoLegajo, empleado);
    }
    
    private void validarDatosEmpleado(String nombre, double valor) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empleado no puede ser nulo o vacío");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a 0");
        }
    }
    
    private void validarCategoria(String categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }
        if (!categoria.equals("INICIAL") && !categoria.equals("TECNICO") && !categoria.equals("EXPERTO")) {
            throw new IllegalArgumentException("Categoría inválida. Debe ser INICIAL, TECNICO o EXPERTO");
        }
    }
    
    private int generarNuevoLegajo() {
        return this.contadorLegajos++;
    }
    
    // ============================================================
    // REGISTRO Y GESTIÓN DE PROYECTOS
    // ============================================================
    
    @Override
    public void registrarProyecto(String[] titulos, String[] descripcion, double[] dias,
                                  String domicilio, String[] cliente, String inicio, String fin)
            throws IllegalArgumentException {
        
        validarDatosProyecto(titulos, descripcion, dias, domicilio, cliente, inicio, fin);
        
        int nuevoNumID = generarNuevoNumProyecto();
        Cliente nuevoCliente = new Cliente(cliente[0], cliente[1], cliente[2]);
        Proyecto proyecto = new Proyecto(nuevoNumID, domicilio, nuevoCliente, inicio, fin);
        
        // Agregar las tareas al proyecto
        for (int i = 0; i < titulos.length; i++) {
            Tarea tarea = new Tarea(titulos[i], descripcion[i], dias[i]);
            proyecto.agregarTarea(tarea);
        }
        
        this.proyectos.put(nuevoNumID, proyecto);
    }
    
    private void validarDatosProyecto(String[] titulos, String[] descripcion, double[] dias,
                                     String domicilio, String[] cliente, String inicio, String fin) {
        if (titulos == null || titulos.length == 0) {
            throw new IllegalArgumentException("Debe haber al menos una tarea");
        }
        if (descripcion == null || dias == null) {
            throw new IllegalArgumentException("Los arrays no pueden ser nulos");
        }
        if (titulos.length != descripcion.length || titulos.length != dias.length) {
            throw new IllegalArgumentException("Los arrays deben tener la misma longitud");
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
        
        // Validar que todos los días sean positivos
        for (double dia : dias) {
            if (dia <= 0) {
                throw new IllegalArgumentException("Los días deben ser mayores a 0");
            }
        }
    }
    
    private int generarNuevoNumProyecto() {
        return this.contadorProyectos++;
    }
    
    // ============================================================
    // ASIGNACIÓN Y GESTIÓN DE TAREAS
    // ============================================================
    
    @Override
    public void asignarResponsableEnTarea(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);
        verificarProyectoNoFinalizado(proyecto);
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        verificarTareaNoAsignada(tarea);
        
        Empleado empleadoDisponible = buscarPrimerEmpleadoDisponible();
        if (empleadoDisponible == null) {
            proyecto.setEstado(Estado.pendiente);
            throw new Exception("No hay empleados disponibles");
        }
        
        // Asignar empleado
        empleadoDisponible.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, empleadoDisponible);
        
        // Si todas las tareas están asignadas, cambiar estado a ACTIVO
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
        verificarTareaNoAsignada(tarea);
        
        Empleado empleadoMenosRetrasos = buscarEmpleadoConMenosRetrasos();
        if (empleadoMenosRetrasos == null) {
            proyecto.setEstado(Estado.pendiente);
            throw new Exception("No hay empleados disponibles");
        }
        
        // Asignar empleado
        empleadoMenosRetrasos.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, empleadoMenosRetrasos);
        
        // Si todas las tareas están asignadas, cambiar estado a ACTIVO
        if (proyecto.getTareasNoAsignadas().length == 0) {
            proyecto.setEstado(Estado.activo);
        }
    }
    
    @Override
    public void registrarRetrasoEnTarea(Integer numero, String titulo, double cantidadDias)
            throws IllegalArgumentException {
        Proyecto proyecto = obtenerProyecto(numero);
        
        if (cantidadDias <= 0) {
            throw new IllegalArgumentException("La cantidad de días debe ser mayor a 0");
        }
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        
        proyecto.registrarRetraso(tarea, cantidadDias);
    }
    
    @Override
    public void agregarTareaEnProyecto(Integer numero, String titulo, String descripcion, double dias)
            throws IllegalArgumentException {
        Proyecto proyecto = obtenerProyecto(numero);
        verificarProyectoNoFinalizado(proyecto);
        
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede ser nulo o vacío");
        }
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días deben ser mayores a 0");
        }
        
        Tarea nuevaTarea = new Tarea(titulo, descripcion, dias);
        proyecto.agregarTarea(nuevaTarea);
    }
    
    @Override
    public void finalizarTarea(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        
        if (tarea.getEstado().equals(Estado.finalizado)) {
            throw new Exception("La tarea ya está finalizada");
        }
        
        // Liberar al empleado
        Empleado responsable = tarea.getResponsable();
        if (responsable != null) {
            responsable.cambiarADisponible();
        }
        
        tarea.finalizarTarea();
    }
    
    @Override
    public void finalizarProyecto(Integer numero, String fin) throws IllegalArgumentException {
        Proyecto proyecto = obtenerProyecto(numero);
        
        if (fin == null || fin.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de finalización no puede ser nula");
        }
        
        // Validar que la fecha de fin sea posterior o igual a la fecha de inicio
        // Y NO anterior a la fecha estimada de fin (si finaliza antes sería incorrecto)
        if (fin.compareTo(proyecto.getFechaInicio().toString()) < 0) {
            throw new IllegalArgumentException("La fecha de finalización no puede ser anterior a la fecha de inicio");
        }
        
        if (proyecto.getEstado().equals(Estado.pendiente)) {
            throw new IllegalArgumentException("No se puede finalizar un proyecto pendiente (sin empleados asignados)");
        }
        
        // Liberar todos los empleados asignados a tareas del proyecto
        for (Object obj : proyecto.getTareas()) {
            Tarea t = (Tarea) obj;
            if (t.getResponsable() != null) {
                t.getResponsable().cambiarADisponible();
                // Finalizar la tarea si no estaba finalizada
                if (!t.getEstado().equals(Estado.finalizado)) {
                    t.finalizarTarea();
                }
            }
        }
        
        proyecto.finalizarProyecto(fin);
    }
    
    // ============================================================
    // REASIGNACIÓN DE EMPLEADOS
    // ============================================================
    
    @Override
    public void reasignarEmpleadoEnProyecto(Integer numero, Integer legajo, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);
        
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
        
        // Liberar empleado anterior
        Empleado empleadoAnterior = tarea.quitarResponsable();
        empleadoAnterior.cambiarADisponible();
        
        // Asignar nuevo empleado
        nuevoEmpleado.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, nuevoEmpleado);
    }
    
    @Override
    public void reasignarEmpleadoConMenosRetraso(Integer numero, String titulo) throws Exception {
        Proyecto proyecto = obtenerProyecto(numero);
        
        Tarea tarea = proyecto.getTarea(titulo);
        verificarTareaExiste(tarea, titulo);
        
        if (tarea.getResponsable() == null) {
            throw new Exception("La tarea no tiene un empleado asignado previamente");
        }
        
        Empleado empleadoMenosRetrasos = buscarEmpleadoConMenosRetrasos();
        if (empleadoMenosRetrasos == null) {
            throw new Exception("No hay empleados disponibles");
        }
        
        // Liberar empleado anterior
        Empleado empleadoAnterior = tarea.quitarResponsable();
        empleadoAnterior.cambiarADisponible();
        
        // Asignar nuevo empleado
        empleadoMenosRetrasos.cambiarANoDisponible();
        proyecto.asignarEmpleadoATarea(tarea, empleadoMenosRetrasos);
    }
    
    // ============================================================
    // CONSULTAS Y REPORTES
    // ============================================================
    
    @Override
    public double costoProyecto(Integer numero) {
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.getCostoFinal();
    }
    
    @Override
    public List<Tupla<Integer, String>> proyectosFinalizados() {
        List<Tupla<Integer, String>> finalizados = new ArrayList<>();
        
        for (Proyecto p : this.proyectos.values()) {
            if (p.estaFinalizado()) {
                finalizados.add(new Tupla<>(p.getNumID(), p.getDomicilio()));
            }
        }
        
        return finalizados;
    }
    
    @Override
    public List<Tupla<Integer, String>> proyectosPendientes() {
        List<Tupla<Integer, String>> pendientes = new ArrayList<>();
        
        for (Proyecto p : this.proyectos.values()) {
            if (p.getEstado().equals(Estado.pendiente)) {
                pendientes.add(new Tupla<>(p.getNumID(), p.getDomicilio()));
            }
        }
        
        return pendientes;
    }
    
    @Override
    public List<Tupla<Integer, String>> proyectosActivos() {
        List<Tupla<Integer, String>> activos = new ArrayList<>();
        
        for (Proyecto p : this.proyectos.values()) {
            if (p.getEstado().equals(Estado.activo)) {
                activos.add(new Tupla<>(p.getNumID(), p.getDomicilio()));
            }
        }
        
        return activos;
    }
    
    @Override
    public Object[] empleadosNoAsignados() {
        List<Integer> noAsignados = new ArrayList<>();
        
        for (Empleado e : this.empleados.values()) {
            if (e.estaDisponible()) {
                noAsignados.add(e.getNumLegajo());
            }
        }
        
        return noAsignados.toArray();
    }
    
    @Override
    public boolean estaFinalizado(Integer numero) {
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.estaFinalizado();
    }
    
    @Override
    public int consultarCantidadRetrasosEmpleado(Integer legajo) {
        Empleado empleado = this.empleados.get(legajo);
        if (empleado == null) {
            return 0;
        }
        return empleado.getCantRetrasos();
    }
    
    @Override
    public List<Tupla<Integer, String>> empleadosAsignadosAProyecto(Integer numero) {
        Proyecto proyecto = obtenerProyecto(numero);
        List<Tupla<Integer, String>> empleadosAsignados = new ArrayList<>();
        
        for (Empleado e : proyecto.getHistorialEmpleados()) {
            empleadosAsignados.add(new Tupla<>(e.getNumLegajo(), e.getNombre()));
        }
        
        return empleadosAsignados;
    }
    
    // ============================================================
    // NUEVOS REQUERIMIENTOS
    // ============================================================
    
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
    public List<Tupla<Integer, String>> empleados() {
        List<Tupla<Integer, String>> listaEmpleados = new ArrayList<>();
        
        for (Empleado e : this.empleados.values()) {
            listaEmpleados.add(new Tupla<>(e.getNumLegajo(), e.getNombre()));
        }
        
        return listaEmpleados;
    }
    
    @Override
    public String consultarProyecto(Integer numero) {
        Proyecto proyecto = obtenerProyecto(numero);
        return proyecto.toString();
    }
    
    // ============================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================================
    
    private Proyecto obtenerProyecto(Integer numero) {
        Proyecto proyecto = this.proyectos.get(numero);
        if (proyecto == null) {
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
        
        // Primero buscar empleados sin retrasos
        for (Empleado e : this.empleados.values()) {
            if (e.estaDisponible() && e.getCantRetrasos() == 0) {
                return e;
            }
        }
        
        // Si no hay sin retrasos, buscar el que menos tenga
        for (Empleado e : this.empleados.values()) {
            if (e.estaDisponible() && e.getCantRetrasos() < menorCantidadRetrasos) {
                empleadoSeleccionado = e;
                menorCantidadRetrasos = e.getCantRetrasos();
            }
        }
        
        return empleadoSeleccionado;
    }
}