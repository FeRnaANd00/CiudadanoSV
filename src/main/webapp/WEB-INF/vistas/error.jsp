<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Error ${codigo}</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full flex items-center">
  <div class="mx-auto max-w-md px-4 py-12 text-center">
    <c:choose>
      <c:when test="${codigo == 403}">
        <span class="material-symbols-outlined text-[56px] text-error">block</span>
        <h1 class="text-headline-md font-bold text-primary mt-3">Acceso restringido</h1>
        <p class="text-body-md text-on-surface-variant mt-2">Esta sección es solo para funcionarios autorizados.</p>
      </c:when>
      <c:when test="${codigo == 404}">
        <span class="material-symbols-outlined text-[56px] text-outline">search_off</span>
        <h1 class="text-headline-md font-bold text-primary mt-3">Página no encontrada</h1>
        <p class="text-body-md text-on-surface-variant mt-2">La página o el trámite que buscas no existe.</p>
      </c:when>
      <c:otherwise>
        <span class="material-symbols-outlined text-[56px] text-error">error</span>
        <h1 class="text-headline-md font-bold text-primary mt-3">Ocurrió un problema</h1>
        <p class="text-body-md text-on-surface-variant mt-2">No pudimos completar tu solicitud. Intenta de nuevo en unos minutos.</p>
      </c:otherwise>
    </c:choose>
    <a href="${ctx}/inicio" class="boton-primario mt-6">
      <span class="material-symbols-outlined text-[18px]">home</span> Ir al inicio
    </a>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
