package mx.edu.cesba.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import Citas.Cita;
import Citas.CitaDAO;
import Medicos.Medico;
import Medicos.MedicoDAO;
import Pacientes.Paciente;
import Pacientes.PacienteDAO;
import Utilidades.Permisos;

@WebServlet("/CitaServlet")
public class CitaServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/vistas/citas.jsp";
    private static final String VISTA_FORM = "/WEB-INF/vistas/cita_form.jsp";
    private static final String VISTA_MANTENIMIENTO = "/WEB-INF/vistas/mantenimiento.jsp";

    private static final boolean EN_MANTENIMIENTO = false;

    private static final List<String> HORARIOS = generarHorarios();

    private final CitaDAO dao = new CitaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();

    private static List<String> generarHorarios() {
        List<String> lista = new ArrayList<>();
        LocalTime hora = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(20, 0);
        while (hora.isBefore(fin)) {
            lista.add(hora.toString());
            hora = hora.plusMinutes(30);
        }
        return lista;
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!autorizado(request, response)) {
            return;
        }

        if (EN_MANTENIMIENTO) {
            request.getRequestDispatcher(VISTA_MANTENIMIENTO).forward(request, response);
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        switch (accion) {
            case "nuevo":
                nuevo(request, response);
                break;

            case "editar":
                Cita existente = dao.buscarPorId(leerId(request, "id"));
                if (existente == null) {
                    response.sendRedirect(
                            request.getContextPath() + "/CitaServlet");
                } else if (!puedeGestionar(request, existente)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    mostrarFormulario(request, response, existente, null);
                }
                break;

            default:
                listar(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!autorizado(request, response)) {
            return;
        }

        if (EN_MANTENIMIENTO) {
            response.sendRedirect(request.getContextPath() + "/CitaServlet");
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        switch (accion) {
            case "guardar":
                guardar(request, response);
                break;

            case "confirmar":
                confirmar(request, response);
                break;

            case "cancelar":
                cancelar(request, response);
                break;

            default:
                response.sendRedirect(
                        request.getContextPath() + "/CitaServlet");
        }
    }

    private boolean autorizado(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setHeader("Cache-Control",
                "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession sesion = request.getSession(false);

        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/Login.jsp");
            return false;
        }

        String rol = (String) sesion.getAttribute("rol");

        if (!Permisos.puede(rol, Permisos.CITAS)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        if (!Permisos.veTodasLasCitas(rol)) {
            Integer propio = dao.medicoIdDeUsuario(
                    (String) sesion.getAttribute("usuario"));

            if (propio == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Tu cuenta no está vinculada a un médico activo. "
                        + "Pide al administrador que la vincule.");
                return false;
            }

            request.setAttribute("medicoPropio", propio);
        }

        return true;
    }

    private Integer medicoPropio(HttpServletRequest request) {
        return (Integer) request.getAttribute("medicoPropio");
    }

    private boolean puedeGestionar(HttpServletRequest request, Cita cita) {
        Integer propio = medicoPropio(request);
        return propio == null || propio == cita.getMedicoId();
    }

    private List<Medico> medicosVisibles(HttpServletRequest request) {

        Integer propio = medicoPropio(request);

        if (propio == null) {
            return medicoDAO.listar("", true);
        }

        List<Medico> lista = new ArrayList<>();
        Medico yo = medicoDAO.buscarPorId(propio);
        if (yo != null) {
            lista.add(yo);
        }
        return lista;
    }

    private void listar(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        LocalDate fecha = leerFecha(request.getParameter("fecha"),
                LocalDate.now());

        Integer propio = medicoPropio(request);
        Integer medicoId;
        String medicoParam = request.getParameter("medicoId");

        if (propio != null) {
            medicoId = propio;
        } else if (medicoParam == null) {
            String usuario = (String) request.getSession(false)
                    .getAttribute("usuario");
            medicoId = dao.medicoIdDeUsuario(usuario);
        } else {
            medicoId = leerMedicoFiltro(medicoParam);
        }

        String estadoFiltro = leerEstadoFiltro(request.getParameter("estado"));

        List<Cita> citas = dao.listar(fecha, medicoId,
                estadoFiltro.isEmpty() ? null : estadoFiltro);

        String msg = request.getParameter("msg");
        if ("guardado".equals(msg)) {
            request.setAttribute("mensaje", "Cita guardada.");
        } else if ("cancelada".equals(msg)) {
            request.setAttribute("mensaje", "Cita cancelada.");
        } else if ("confirmada".equals(msg)) {
            request.setAttribute("mensaje", "Cita confirmada.");
        } else if ("errorconfirmar".equals(msg)) {
            request.setAttribute("mensaje",
                    "No se pudo confirmar la cita. Solo se pueden confirmar "
                    + "citas programadas.");
            request.setAttribute("mensajeError", true);
        } else if ("error".equals(msg)) {
            request.setAttribute("mensaje",
                    "No se pudo cancelar la cita. Solo se pueden cancelar "
                    + "citas programadas o confirmadas.");
            request.setAttribute("mensajeError", true);
        }

        request.setAttribute("citas", citas);
        request.setAttribute("fecha", fecha);
        request.setAttribute("medicoId", medicoId);
        request.setAttribute("estadoFiltro", estadoFiltro);
        request.setAttribute("medicos", medicosVisibles(request));
        request.setAttribute("soloPropia", propio != null);

        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void nuevo(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Cita c = new Cita();

        LocalDate fecha = leerFecha(request.getParameter("fecha"),
                LocalDate.now());
        if (fecha.isBefore(LocalDate.now())) {
            fecha = LocalDate.now();
        }
        c.setFecha(fecha);

        Integer propio = medicoPropio(request);
        Integer medicoId = (propio != null)
                ? propio
                : leerMedicoFiltro(request.getParameter("medicoId"));
        if (medicoId != null) {
            c.setMedicoId(medicoId);
        }

        mostrarFormulario(request, response, c, null);
    }

    private void mostrarFormulario(HttpServletRequest request,
            HttpServletResponse response, Cita cita, String error)
            throws ServletException, IOException {

        List<String> horarios = new ArrayList<>(HORARIOS);
        if (cita.getHora() != null
                && !horarios.contains(cita.getHora().toString())) {
            horarios.add(cita.getHora().toString());
            Collections.sort(horarios);
        }

        request.setAttribute("cita", cita);
        request.setAttribute("error", error);
        request.setAttribute("pacientes", pacienteDAO.listar("", true));
        request.setAttribute("medicos", medicosVisibles(request));
        request.setAttribute("horarios", horarios);

        request.getRequestDispatcher(VISTA_FORM).forward(request, response);
    }

    private void guardar(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Cita c = new Cita();

        c.setId(leerId(request, "id"));
        c.setPacienteId(leerId(request, "pacienteId"));
        c.setMedicoId(leerId(request, "medicoId"));
        c.setMotivo(texto(request, "motivo"));
        c.setObservaciones(texto(request, "observaciones"));

        boolean esNueva = c.getId() == 0;

        c.setEstado(esNueva ? "PROGRAMADA" : texto(request, "estado"));

        Cita original = esNueva ? null : dao.buscarPorId(c.getId());

        if (original != null && !puedeGestionar(request, original)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String error = null;

        Integer propio = medicoPropio(request);
        if (propio != null && c.getMedicoId() != propio) {
            error = "Solo puedes gestionar las citas de tu propia agenda.";
        }

        try {
            c.setFecha(LocalDate.parse(texto(request, "fecha")));
        } catch (DateTimeParseException e) {
            error = "La fecha no es válida.";
        }

        String horaTexto = texto(request, "hora");
        boolean horaPermitida = HORARIOS.contains(horaTexto)
                || (original != null && original.getHora() != null
                && original.getHora().toString().equals(horaTexto));

        if (horaPermitida) {
            c.setHora(LocalTime.parse(horaTexto));
        }

        if (error == null) {
            error = validar(c, original, esNueva);
        }

        if (error != null) {
            mostrarFormulario(request, response, c, error);
            return;
        }

        String usuario = (String) request.getSession(false)
                .getAttribute("usuario");

        try {
            boolean ok = esNueva ? dao.insertar(c, usuario) : dao.actualizar(c);

            if (ok) {
                response.sendRedirect(urlLista(request, c.getFecha(), null,
                        "", "guardado"));
                return;
            }

            error = "No se pudo guardar la cita. Intenta de nuevo.";

        } catch (SQLException e) {
            error = traducirError(e);
        }

        mostrarFormulario(request, response, c, error);
    }

    private void cancelar(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        int id = leerId(request, "id");
        Cita cita = id > 0 ? dao.buscarPorId(id) : null;

        if (cita != null && !puedeGestionar(request, cita)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        boolean ok = cita != null && dao.cancelar(id);

        volverALista(request, response, ok ? "cancelada" : "error");
    }

    private void confirmar(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        int id = leerId(request, "id");
        Cita cita = id > 0 ? dao.buscarPorId(id) : null;

        if (cita != null && !puedeGestionar(request, cita)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        boolean ok = cita != null && dao.confirmar(id);

        volverALista(request, response, ok ? "confirmada" : "errorconfirmar");
    }

    private void volverALista(HttpServletRequest request,
            HttpServletResponse response, String msg) throws IOException {

        LocalDate fecha = leerFecha(request.getParameter("fecha"),
                LocalDate.now());
        Integer medicoId = leerMedicoFiltro(request.getParameter("medicoId"));
        String estado = leerEstadoFiltro(request.getParameter("estado"));

        response.sendRedirect(urlLista(request, fecha, medicoId, estado, msg));
    }

    private String validar(Cita c, Cita original, boolean esNueva) {

        if (!esNueva && original == null) {
            return "La cita que intentas editar no existe.";
        }
        if (c.getPacienteId() == 0) {
            return "Selecciona un paciente.";
        }
        if (c.getMedicoId() == 0) {
            return "Selecciona un médico.";
        }
        if (c.getHora() == null) {
            return "Selecciona una hora válida.";
        }
        if (!Arrays.asList(Cita.ESTADOS).contains(c.getEstado())) {
            return "El estado seleccionado no es válido.";
        }
        if (c.getMotivo() != null && c.getMotivo().length() > 250) {
            return "El motivo no puede tener más de 250 caracteres.";
        }
        if (c.getObservaciones() != null
                && c.getObservaciones().length() > 500) {
            return "Las observaciones no pueden tener más de 500 caracteres.";
        }

        if (original == null || original.getPacienteId() != c.getPacienteId()) {
            Paciente p = pacienteDAO.buscarPorId(c.getPacienteId());
            if (p == null || !p.isActivo()) {
                return "El paciente seleccionado no está disponible.";
            }
        }
        if (original == null || original.getMedicoId() != c.getMedicoId()) {
            Medico m = medicoDAO.buscarPorId(c.getMedicoId());
            if (m == null || !m.isActivo()) {
                return "El médico seleccionado no está disponible.";
            }
        }

        if (esNueva && LocalDateTime.of(c.getFecha(), c.getHora())
                .isBefore(LocalDateTime.now())) {
            return "No se puede agendar una cita en una fecha u hora que ya pasó.";
        }

        if (!"CANCELADA".equals(c.getEstado())
                && dao.pacienteOcupado(c.getPacienteId(), c.getFecha(),
                        c.getHora(), c.getId())) {
            return "El paciente ya tiene una cita a esa fecha y hora. "
                    + "Elige otro horario.";
        }

        return null;
    }

    private String traducirError(SQLException e) {

        if (e.getErrorCode() == 1062) {
            return "Ese médico ya tiene una cita activa a esa fecha y hora. "
                    + "Elige otro horario.";
        }

        e.printStackTrace();
        return "No se pudo guardar la cita. Intenta de nuevo.";
    }

    private String urlLista(HttpServletRequest request, LocalDate fecha,
            Integer medicoId, String estado, String msg) {

        return request.getContextPath() + "/CitaServlet"
                + "?fecha=" + fecha
                + "&medicoId=" + (medicoId == null ? "" : medicoId)
                + "&estado=" + estado
                + "&msg=" + msg;
    }

    private int leerId(HttpServletRequest request, String parametro) {
        try {
            return Integer.parseInt(request.getParameter(parametro));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate leerFecha(String texto, LocalDate porDefecto) {
        if (texto == null) {
            return porDefecto;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return porDefecto;
        }
    }

    private Integer leerMedicoFiltro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String leerEstadoFiltro(String texto) {
        if (texto != null && Arrays.asList(Cita.ESTADOS).contains(texto)) {
            return texto;
        }
        return "";
    }

    private String texto(HttpServletRequest request, String nombre) {
        String valor = request.getParameter(nombre);
        return valor == null ? "" : valor.trim();
    }
}
