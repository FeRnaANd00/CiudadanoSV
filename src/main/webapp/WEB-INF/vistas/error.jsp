<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%--
  Página de error única (mapeada en web.xml para 400, 403, 404, 405, 500 y cualquier excepción).
  Nunca muestra detalles técnicos: el error real queda en el log de Tomcat.
--%>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:if test="${empty codigo or codigo == 0}"><c:set var="codigo" value="500"/></c:if>
<%-- Parámetros mal codificados en la URL (ej. %ZZ): Tomcat lanza una excepción, pero es un error del cliente --%>
<c:set var="excepcion" value="${requestScope['jakarta.servlet.error.exception']}"/>
<c:if test="${not empty excepcion and (excepcion['class'].simpleName.contains('BadRequest') or excepcion['class'].simpleName.contains('InvalidParameter'))}">
  <c:set var="codigo" value="400"/>
  <% response.setStatus(400); %>
</c:if>
<c:choose>
  <c:when test="${codigo == 403 and requestScope.errorCsrf}">
    <c:set var="icono" value="timer_off"/>
    <c:set var="titulo" value="El formulario venció"/>
    <c:set var="texto" value="Por seguridad, el formulario ya no es válido (pasó mucho tiempo o se abrió en otra pestaña). Vuelve a la página, recárgala e inténtalo de nuevo."/>
  </c:when>
  <c:when test="${codigo == 403}">
    <c:set var="icono" value="block"/>
    <c:set var="titulo" value="Acceso restringido"/>
    <c:set var="texto" value="Tu cuenta no tiene permiso para entrar a esta sección. Si crees que es un error, comunícate con el administrador."/>
  </c:when>
  <c:when test="${codigo == 404}">
    <c:set var="icono" value="search_off"/>
    <c:set var="titulo" value="Página no encontrada"/>
    <c:set var="texto" value="La página, el trámite o el expediente que buscas no existe o no está disponible para tu cuenta."/>
  </c:when>
  <c:when test="${codigo == 400}">
    <c:set var="icono" value="report"/>
    <c:set var="titulo" value="Solicitud no válida"/>
    <c:set var="texto" value="Los datos enviados no tienen el formato esperado. Vuelve a la página anterior y revisa el formulario."/>
  </c:when>
  <c:when test="${codigo == 405}">
    <c:set var="icono" value="do_not_disturb_on"/>
    <c:set var="titulo" value="Acción no permitida"/>
    <c:set var="texto" value="Esta dirección no se puede abrir de esa forma. Usa los botones y menús del portal."/>
  </c:when>
  <c:otherwise>
    <c:set var="icono" value="cloud_off"/>
    <c:set var="titulo" value="Ocurrió un problema"/>
    <c:set var="texto" value="No pudimos completar tu solicitud. Tus datos están a salvo; intenta de nuevo en unos minutos."/>
  </c:otherwise>
</c:choose>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | ${titulo}</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full flex items-center">
  <div class="mx-auto max-w-md px-4 py-12 text-center">
    <span class="mx-auto w-20 h-20 rounded-2xl ${codigo >= 500 or codigo == 403 ? 'bg-error-container text-error' : 'bg-primary-fixed text-primary'} flex items-center justify-center">
      <span class="material-symbols-outlined text-[44px]">${icono}</span>
    </span>
    <p class="text-label-sm uppercase tracking-wider text-secondary mt-5">Error ${codigo}</p>
    <h1 class="text-headline-md font-bold text-primary mt-1">${titulo}</h1>
    <p class="text-body-md text-on-surface-variant mt-2">${texto}</p>

    <div class="mt-6 flex flex-wrap items-center justify-center gap-2">
      <button type="button" class="boton-borde" onclick="history.back()">
        <span class="material-symbols-outlined text-[18px]">arrow_back</span> Volver
      </button>
      <c:choose>
        <c:when test="${sessionScope.usuario.funcionario}">
          <a href="${ctx}/admin/inicio" class="boton-primario"><span class="material-symbols-outlined text-[18px]">monitoring</span> Ir al panel</a>
        </c:when>
        <c:otherwise>
          <a href="${ctx}/inicio" class="boton-primario"><span class="material-symbols-outlined text-[18px]">home</span> Ir al inicio</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
