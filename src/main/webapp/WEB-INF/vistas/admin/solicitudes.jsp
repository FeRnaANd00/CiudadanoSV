<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | Bandeja de solicitudes</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="solicitudes"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-6xl px-4 sm:px-6 py-6 space-y-5">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>

    <section>
      <p class="text-label-sm uppercase text-secondary">
        ${sessionScope.usuario.superAdmin ? 'Todas las instituciones' : sessionScope.usuario.institucionSiglas}
      </p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Bandeja de expedientes</h1>
      <p class="text-body-sm text-on-surface-variant mt-1">Trámites ingresados en línea por los ciudadanos.</p>
    </section>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>

    <%-- Indicadores --%>
    <section class="grid grid-cols-2 lg:grid-cols-4 gap-3">
      <a href="${ctx}/admin/solicitudes?estado=ABIERTAS" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 hover:border-primary">
        <p class="text-caption uppercase text-secondary flex items-center gap-1"><span class="material-symbols-outlined text-[16px]">folder_shared</span> Por atender</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${indicadores.ABIERTAS + 0}</p>
      </a>
      <a href="${ctx}/admin/solicitudes?estado=RECIBIDA" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 hover:border-primary">
        <p class="text-caption uppercase text-secondary flex items-center gap-1"><span class="material-symbols-outlined text-[16px]">move_to_inbox</span> Recibidas</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${indicadores.RECIBIDA + 0}</p>
      </a>
      <div class="rounded-2xl bg-error-container/60 border border-error-container p-4">
        <p class="text-caption uppercase text-[#93000a] flex items-center gap-1"><span class="material-symbols-outlined text-[16px]">schedule_send</span> Más de 24 h</p>
        <p class="text-headline-lg font-bold text-[#93000a] mt-1">${indicadores.PRIORIDAD + 0}</p>
      </div>
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary flex items-center gap-1"><span class="material-symbols-outlined text-[16px]">assignment_ind</span> Sin asignar</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${indicadores.SIN_ASIGNAR + 0}</p>
      </div>
    </section>

    <%-- Filtros (GET) --%>
    <form method="get" action="${ctx}/admin/solicitudes" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-[1.4fr_1fr_1fr_1fr_auto] items-end">
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Folio o DUI</span>
        <input class="campo mt-1" type="search" name="q" value="<c:out value='${f.texto}'/>" placeholder="EXP-2026-00012 o 04829104-5">
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Estado</span>
        <select class="campo mt-1" name="estado">
          <option value="" ${empty f.estado ? 'selected' : ''}>Todos</option>
          <option value="ABIERTAS" ${f.estado == 'ABIERTAS' ? 'selected' : ''}>Por atender</option>
          <c:forTokens var="e" delims="," items="RECIBIDA,EN_REVISION,CITA_PROGRAMADA,APROBADA,RECHAZADA,FINALIZADA">
            <option value="${e}" ${f.estado == e ? 'selected' : ''}>
              ${e == 'RECIBIDA' ? 'Recibida' : e == 'EN_REVISION' ? 'En revisión' : e == 'CITA_PROGRAMADA' ? 'Cita programada' : e == 'APROBADA' ? 'Aprobada' : e == 'RECHAZADA' ? 'Rechazada' : 'Finalizada'}
              (${indicadores[e] + 0})
            </option>
          </c:forTokens>
        </select>
      </label>
      <c:choose>
        <c:when test="${sessionScope.usuario.superAdmin}">
          <label class="block">
            <span class="text-label-sm text-on-surface-variant">Institución</span>
            <select class="campo mt-1" name="institucion">
              <option value="">Todas</option>
              <c:forEach var="i" items="${instituciones}">
                <option value="${i.idInstitucion}" ${f.idInstitucion == i.idInstitucion ? 'selected' : ''}><c:out value="${i.siglas}"/></option>
              </c:forEach>
            </select>
          </label>
        </c:when>
        <c:otherwise>
          <label class="flex items-center gap-2 min-h-[44px]">
            <input type="checkbox" name="mias" value="1" ${f.soloMias ? 'checked' : ''} class="w-4 h-4 accent-[#002869]">
            <span class="text-label-md">Solo asignadas a mí</span>
          </label>
        </c:otherwise>
      </c:choose>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Trámite</span>
        <select class="campo mt-1" name="tramite">
          <option value="">Todos</option>
          <c:forEach var="t" items="${tramites}">
            <option value="${t.idTramite}" ${f.idTramite == t.idTramite ? 'selected' : ''}><c:out value="${t.nombre}"/><c:if test="${sessionScope.usuario.superAdmin}"> (${t.institucionSiglas})</c:if></option>
          </c:forEach>
        </select>
      </label>
      <div class="flex gap-2">
        <button type="submit" class="boton-primario"><span class="material-symbols-outlined text-[18px]">search</span> Filtrar</button>
        <a href="${ctx}/admin/solicitudes" class="boton-borde" title="Quitar filtros"><span class="material-symbols-outlined text-[18px]">refresh</span></a>
      </div>
    </form>

    <%-- Resultados --%>
    <section>
      <p class="text-label-md text-secondary mb-3">${total += (total == 1 ? ' expediente' : ' expedientes')}</p>
      <c:choose>
        <c:when test="${empty solicitudes}">
          <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-8 text-center">
            <span class="material-symbols-outlined text-[40px] text-outline">inbox</span>
            <p class="text-title-sm text-primary mt-2">No hay expedientes con estos filtros</p>
          </div>
        </c:when>
        <c:otherwise>
          <ul class="space-y-3">
            <c:forEach var="s" items="${solicitudes}">
              <li class="rounded-2xl bg-surface-container-lowest border ${s.prioridadAlta ? 'border-l-4 border-l-error border-surface-container' : 'border-surface-container'} p-4 sm:p-5">
                <div class="flex flex-wrap items-center justify-between gap-2">
                  <div class="flex flex-wrap items-center gap-2">
                    <span class="text-label-md font-bold text-primary tracking-wide"><c:out value="${s.folio}"/></span>
                    <c:set var="est" value="${s.estado}"/><c:set var="estTexto" value="${s.estadoTexto}"/>
                    <%@include file="/WEB-INF/vistas/includes/badge_estado.jspf"%>
                    <c:if test="${s.abierta}">
                      <span class="text-caption font-bold inline-flex items-center gap-0.5 ${s.prioridadAlta ? 'text-error' : 'text-secondary'}">
                        <span class="material-symbols-outlined text-[14px]">timer</span> ${s.tiempoEnColaTexto} en cola
                      </span>
                    </c:if>
                  </div>
                  <span class="text-caption text-secondary"><c:out value="${s.institucionSiglas}"/></span>
                </div>
                <h2 class="text-title-sm text-on-surface mt-1"><c:out value="${s.tramiteNombre}"/></h2>
                <dl class="mt-2 grid gap-x-6 gap-y-1 sm:grid-cols-2 lg:grid-cols-4 text-body-sm">
                  <div><dt class="inline text-secondary">Ciudadano:</dt> <dd class="inline"><c:out value="${s.ciudadanoNombre}"/></dd></div>
                  <div><dt class="inline text-secondary">DUI:</dt> <dd class="inline"><c:out value="${s.ciudadanoDui}"/></dd></div>
                  <div><dt class="inline text-secondary">Sede:</dt> <dd class="inline"><c:out value="${empty s.sedeNombre ? 'En línea' : s.sedeNombre}"/></dd></div>
                  <div><dt class="inline text-secondary">Ingresó:</dt> <dd class="inline">${s.fechaCreacionTexto}</dd></div>
                </dl>
                <div class="mt-3 flex flex-wrap items-center justify-between gap-2">
                  <span class="text-caption inline-flex items-center gap-1 ${empty s.revisorNombre ? 'text-dorado font-bold' : 'text-secondary'}">
                    <span class="material-symbols-outlined text-[16px]">assignment_ind</span>
                    <c:out value="${empty s.revisorNombre ? 'Sin revisor asignado' : s.revisorNombre}"/>
                  </span>
                  <a href="${ctx}/admin/solicitud?folio=${s.folio}" class="boton-primario">
                    <span class="material-symbols-outlined text-[18px]">gavel</span> ${s.abierta and sessionScope.usuario.puedeResolver ? 'Gestionar' : 'Ver ficha'}
                  </a>
                </div>
              </li>
            </c:forEach>
          </ul>

          <%-- Paginación --%>
          <c:if test="${paginas > 1}">
            <c:url var="base" value="/admin/solicitudes">
              <c:param name="q" value="${f.texto}"/>
              <c:param name="estado" value="${f.estado}"/>
              <c:param name="institucion" value="${f.idInstitucion}"/>
              <c:param name="tramite" value="${f.idTramite}"/>
              <c:if test="${f.soloMias}"><c:param name="mias" value="1"/></c:if>
            </c:url>
            <nav class="flex items-center justify-center gap-2 mt-5" aria-label="Páginas">
              <c:if test="${f.pagina > 1}">
                <a class="boton-borde" href="${base}&pagina=${f.pagina - 1}"><span class="material-symbols-outlined text-[18px]">chevron_left</span> Anterior</a>
              </c:if>
              <span class="text-label-md text-secondary px-2">Página ${f.pagina} de ${paginas}</span>
              <c:if test="${f.pagina < paginas}">
                <a class="boton-borde" href="${base}&pagina=${f.pagina + 1}">Siguiente <span class="material-symbols-outlined text-[18px]">chevron_right</span></a>
              </c:if>
            </nav>
          </c:if>
        </c:otherwise>
      </c:choose>
    </section>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
