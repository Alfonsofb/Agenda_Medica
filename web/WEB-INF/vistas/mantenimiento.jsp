<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Utilidades.Html"%>
<%
    String ctx = request.getContextPath();

    String usuarioSesion = (String) session.getAttribute("usuario");
    String nombreSesion = (String) session.getAttribute("nombre");
    String rolSesion = (String) session.getAttribute("rol");

    if (nombreSesion == null) {
        nombreSesion = usuarioSesion;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agenda Médica | En mantenimiento</title>
    <link rel="stylesheet" href="<%= ctx %>/css/estilos.css?v=2">
</head>

<body>

<header>
    <div class="marca">
        <a href="<%= ctx %>/index.jsp">&#9764; Agenda Médica</a>
    </div>

    <div class="usuario">
        <strong><%= Html.esc(nombreSesion) %></strong><br>
        Usuario: <%= Html.esc(usuarioSesion) %> | Rol: <%= Html.esc(rolSesion) %>
        <br><br>
        <a class="boton" href="<%= ctx %>/LogoutServlet">Cerrar sesión</a>
    </div>
</header>

<main class="contenedor">

    <div class="encabezado-seccion">
        <h1>Agenda de citas</h1>
        <a class="boton secundario" href="<%= ctx %>/index.jsp">Volver al panel</a>
    </div>

    <section class="panel">
        <div class="mensaje error" role="status">
            Este apartado está en mantenimiento. Vuelve a intentarlo más tarde.
        </div>
    </section>

</main>

<footer>
    Agenda Médica &copy; 2026 | Sistema de gestión del consultorio
</footer>

</body>
</html>
