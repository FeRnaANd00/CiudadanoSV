<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | Panel</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="panel"/>
<c:set var="yo" value="${sessionScope.usuario}"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-6xl px-4 sm:px-6 py-6 space-y-6">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>
    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>

    <section class="relative overflow-hidden rounded-2xl bg-primary text-on-primary p-6">
      <span class="material-symbols-outlined absolute -right-6 -bottom-8 text-[150px] opacity-10 pointer-events-none select-none" aria-hidden="true">monitoring</span>
      <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold relative">
        Panel de control &bull; ${yo.superAdmin ? 'Todo el portal' : yo.institucionSiglas}
      </p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold mt-2 relative">Hola, <c:out value="${yo.primerNombre}"/></h1>
      <p class="text-body-sm text-primary-fixed mt-1 relative"><c:out value="${yo.rolTexto}"/></p>
      <div class="relative mt-4 flex flex-wrap gap-2">
        <a href="${ctx}/admin/solicitudes" class="inline-flex items-center gap-1.5 rounded-lg bg-white text-primary px-3 py-2 text-label-md">
          <span class="material-symbols-outlined text-[18px]">inbox</span> ${kpi.ABIERTAS + 0} por atender
        </a>
        <c:if test="${yo.puedeGestionarCupos}">
          <a href="${ctx}/admin/cupos" class="inline-flex items-center gap-1.5 rounded-lg bg-white/15 text-white px-3 py-2 text-label-md">
            <span class="material-symbols-outlined text-[18px]">event</span> ${kpi.CITAS_HOY + 0} citas hoy
          </a>
        </c:if>
      </div>
    </section>

    <%-- Indicadores --%>
    <section class="grid grid-cols-2 lg:grid-cols-4 gap-3" aria-label="Indicadores">
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary">Solicitudes</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${kpi.TOTAL + 0}</p>
        <p class="text-caption text-secondary">${kpi.ULTIMOS30 + 0} en los últimos 30 días</p>
      </div>
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary">Tasa de aprobación</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${kpi.TASA_APROBACION < 0 ? '—' : kpi.TASA_APROBACION += '%'}</p>
        <p class="text-caption text-secondary">Aprobadas: ${kpi.APROBADAS + 0} &bull; Rechazadas: ${kpi.RECHAZADAS + 0}</p>
      </div>
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary">Tiempo de resolución</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${kpi.TIEMPO_RESOLUCION}</p>
        <p class="text-caption text-secondary">Promedio desde que ingresa</p>
      </div>
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary">Citas próximos 7 días</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${kpi.CITAS_SEMANA + 0}</p>
        <p class="text-caption text-secondary">${yo.superAdmin ? kpi.CIUDADANOS += ' ciudadanos registrados' : 'En las sedes de tu institución'}</p>
      </div>
    </section>

    <%-- Tendencia diaria (columnas, una sola serie) --%>
    <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
      <h2 class="text-title-lg font-bold text-primary">Solicitudes recibidas por día</h2>
      <p class="text-caption text-secondary">Últimos ${diasTendencia} días</p>
      <div class="mt-5 flex items-end gap-1 sm:gap-2 h-40 border-b border-surface-container" role="img"
           aria-label="Gráfico de columnas de solicitudes por día; los valores están en la tabla de abajo.">
        <c:forEach var="b" items="${porDia}">
          <div class="group relative flex-1 h-full flex items-end justify-center">
            <div class="w-full max-w-[28px] rounded-t bg-primary group-hover:bg-[#345baf] transition-colors"
                 style="height:${b.valor == 0 ? 0 : (b.porcentaje < 3 ? 3 : b.porcentaje)}%"></div>
            <span class="pointer-events-none absolute bottom-full mb-1 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-md bg-[#121c2a] text-white text-caption px-2 py-1 opacity-0 group-hover:opacity-100 z-10 first-letter:uppercase">
              ${b.detalle}: ${b.valor}
            </span>
          </div>
        </c:forEach>
      </div>
      <div class="flex gap-1 sm:gap-2 mt-1">
        <c:forEach var="b" items="${porDia}" varStatus="vs">
          <span class="flex-1 text-center text-[10px] text-secondary">${vs.index % 2 == 0 or vs.last ? fn:escapeXml(b.etiqueta) : ''}</span>
        </c:forEach>
      </div>
      <details class="mt-3">
        <summary class="text-label-sm text-primary cursor-pointer">Ver tabla</summary>
        <table class="mt-2 text-body-sm">
          <c:forEach var="b" items="${porDia}">
            <tr><td class="pr-6 py-0.5 first-letter:uppercase">${b.detalle}</td><td class="text-right font-semibold">${b.valor}</td></tr>
          </c:forEach>
        </table>
      </details>
    </section>

    <div class="grid gap-5 lg:grid-cols-3">
      <%-- Barras horizontales: demanda por área, estado y top trámites --%>
      <c:forTokens var="g" delims="|" items="porArea;Demanda por área|porEstado;Solicitudes por estado|topTramites;Trámites más solicitados">
        <c:set var="partes" value="${g.split(';')}"/>
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
          <h2 class="text-title-sm font-bold text-primary">${partes[1]}</h2>
          <c:set var="barras" value="${requestScope[partes[0]]}"/>
          <c:choose>
            <c:when test="${empty barras}">
              <p class="text-body-sm text-on-surface-variant mt-3">Sin datos todavía.</p>
            </c:when>
            <c:otherwise>
              <ul class="mt-4 space-y-3">
                <c:forEach var="b" items="${barras}">
                  <li title="${fn:escapeXml(b.etiqueta)}: ${b.valor}">
                    <div class="flex items-baseline justify-between gap-2 text-body-sm">
                      <span class="text-on-surface truncate"><c:out value="${b.etiqueta}"/></span>
                      <span class="font-semibold text-on-surface">${b.valor}</span>
                    </div>
                    <div class="mt-1 h-2 rounded-full bg-surface-container">
                      <div class="h-full rounded-full bg-primary" style="width:${b.porcentaje}%"></div>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:otherwise>
          </c:choose>
        </section>
      </c:forTokens>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
