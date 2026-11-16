<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Mis solicitudes</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-4xl px-4 sm:px-6 py-6 space-y-5">

    <section class="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
      <div>
        <p class="text-label-sm uppercase text-secondary">Gestión ciudadana</p>
        <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Mis solicitudes</h1>
        <p class="text-body-sm text-on-surface-variant mt-1">Seguimiento de todos tus trámites.</p>
      </div>
      <a href="${ctx}/inicio#servicios" class="boton-primario self-start sm:self-auto">
        <span class="material-symbols-outlined text-[18px]">add_circle</span> Nuevo trámite
      </a>
    </section>

    <c:if test="${not empty errorBD}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">database</span>
        <p class="text-label-md"><c:out value="${errorBD}"/></p>
      </div>
    </c:if>

    <c:if test="${empty errorBD}">
      <%-- Resumen --%>
      <section class="grid grid-cols-3 gap-3">
        <div class="rounded-xl bg-surface-container-lowest border border-surface-container p-3">
          <p class="text-caption uppercase text-secondary">En proceso</p>
          <p class="text-headline-md font-bold text-primary">${enProceso}</p>
        </div>
        <div class="rounded-xl bg-surface-container-lowest border border-surface-container p-3">
          <p class="text-caption uppercase text-secondary">Aprobadas</p>
          <p class="text-headline-md font-bold text-exito">${aprobadas}</p>
        </div>
        <div class="rounded-xl bg-surface-container-lowest border border-surface-container p-3">
          <p class="text-caption uppercase text-secondary">Rechazadas</p>
          <p class="text-headline-md font-bold text-error">${rechazadas}</p>
        </div>
      </section>

      <%-- Búsqueda y filtros (GET: la URL se puede compartir o recargar) --%>
      <form method="get" action="${ctx}/app/solicitudes" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 space-y-3">
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-secondary text-[20px] pointer-events-none">search</span>
          <input type="search" name="q" value="${fn:escapeXml(param.q)}" maxlength="60"
                 placeholder="Buscar por folio (EXP-...) o nombre del trámite" class="campo pl-10">
        </div>
        <div class="flex flex-col sm:flex-row gap-3">
          <select name="area" class="campo sm:flex-1" onchange="this.form.submit()" aria-label="Área">
            <option value="">Todas las áreas</option>
            <c:forEach var="a" items="${areas}">
              <option value="${a.key}" ${param.area == (''+=a.key) ? 'selected' : ''}><c:out value="${a.value}"/></option>
            </c:forEach>
          </select>
          <select name="estado" class="campo sm:flex-1" onchange="this.form.submit()" aria-label="Estado">
            <option value="">Todos los estados (${total})</option>
            <option value="PROCESO" ${param.estado == 'PROCESO' ? 'selected' : ''}>En proceso (${enProceso})</option>
            <option value="APROBADA" ${param.estado == 'APROBADA' ? 'selected' : ''}>Aprobadas (${aprobadas})</option>
            <option value="RECHAZADA" ${param.estado == 'RECHAZADA' ? 'selected' : ''}>Rechazadas (${rechazadas})</option>
          </select>
          <button type="submit" class="boton-borde">Buscar</button>
        </div>
      </form>

      <%-- Listado --%>
      <c:choose>
        <c:when test="${empty solicitudes}">
          <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-8 text-center">
            <span class="material-symbols-outlined text-[40px] text-outline">${hayFiltros ? 'search_off' : 'folder_off'}</span>
            <c:choose>
              <c:when test="${hayFiltros}">
                <p class="text-title-sm text-primary mt-2">No se encontraron trámites</p>
                <p class="text-body-sm text-on-surface-variant mt-1">Revisa el folio o cambia los filtros.</p>
                <a href="${ctx}/app/solicitudes" class="boton-borde mt-4">
                  <span class="material-symbols-outlined text-[18px]">refresh</span> Quitar filtros
                </a>
              </c:when>
              <c:otherwise>
                <p class="text-title-sm text-primary mt-2">Aún no tienes solicitudes</p>
                <p class="text-body-sm text-on-surface-variant mt-1">Elige un servicio del catálogo para empezar.</p>
                <a href="${ctx}/inicio#servicios" class="boton-primario mt-4">
                  <span class="material-symbols-outlined text-[18px]">apps</span> Ver servicios
                </a>
              </c:otherwise>
            </c:choose>
          </div>
        </c:when>
        <c:otherwise>
          <ul class="space-y-3">
            <c:forEach var="s" items="${solicitudes}">
              <c:url var="urlDetalle" value="/app/solicitud"><c:param name="folio" value="${s.folio}"/></c:url>
              <li>
                <a href="${urlDetalle}" class="tarjeta-area block rounded-2xl bg-surface-container-lowest border border-surface-container p-4 sm:p-5">
                  <div class="flex items-start gap-3">
                    <span class="w-10 h-10 rounded-xl bg-primary-fixed text-primary flex items-center justify-center shrink-0">
                      <span class="material-symbols-outlined">${empty s.areaIcono ? 'description' : s.areaIcono}</span>
                    </span>
                    <div class="min-w-0 flex-1">
                      <div class="flex flex-wrap items-center justify-between gap-2">
                        <h2 class="text-title-sm text-primary"><c:out value="${s.tramiteNombre}"/></h2>
                        <c:set var="est" value="${s.estado}"/><c:set var="estTexto" value="${s.estadoTexto}"/>
                        <%@include file="/WEB-INF/vistas/includes/badge_estado.jspf"%>
                      </div>
                      <p class="text-body-sm text-on-surface-variant mt-0.5">
                        Folio <strong class="text-on-surface"><c:out value="${s.folio}"/></strong> &bull; <c:out value="${s.institucionSiglas}"/>
                      </p>
                      <p class="text-caption text-secondary mt-1 flex flex-wrap gap-x-3 gap-y-1">
                        <span class="inline-flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">calendar_today</span>${s.fechaCreacionTexto}</span>
                        <c:if test="${not empty s.sedeNombre}">
                          <span class="inline-flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">location_on</span><c:out value="${s.sedeNombre}"/></span>
                        </c:if>
                      </p>
                      <c:if test="${s.grupoEstado == 'PROCESO'}">
                        <div class="mt-3 h-1.5 rounded-full bg-surface-container overflow-hidden" aria-label="Avance ${s.progreso}%">
                          <div class="h-full bg-primary rounded-full" style="width: ${s.progreso}%"></div>
                        </div>
                      </c:if>
                      <c:if test="${not empty s.observacionCiudadano}">
                        <p class="mt-3 text-body-sm rounded-lg bg-surface-container-low p-2.5 text-on-surface-variant">
                          <span class="material-symbols-outlined text-[16px] align-[-3px] text-primary">rate_review</span>
                          <c:out value="${s.observacionCiudadano}"/>
                        </p>
                      </c:if>
                    </div>
                    <span class="material-symbols-outlined text-outline self-center">chevron_right</span>
                  </div>
                </a>
              </li>
            </c:forEach>
          </ul>
          <p class="text-caption text-secondary text-center">Mostrando ${fn:length(solicitudes)} de ${total} solicitudes</p>
        </c:otherwise>
      </c:choose>
    </c:if>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
