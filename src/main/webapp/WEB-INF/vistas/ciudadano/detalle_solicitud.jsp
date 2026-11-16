<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | <c:out value="${solicitud.folio}"/></title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-4xl px-4 sm:px-6 py-6 space-y-5">

    <a href="${ctx}/app/solicitudes" class="inline-flex items-center gap-1 text-label-md text-primary hover:underline">
      <span class="material-symbols-outlined text-[18px]">arrow_back</span> Mis solicitudes
    </a>

    <%-- Encabezado --%>
    <section class="relative overflow-hidden rounded-2xl bg-primary text-on-primary p-6">
      <span class="material-symbols-outlined absolute -right-6 -bottom-8 text-[150px] opacity-10 pointer-events-none select-none" aria-hidden="true">${empty solicitud.areaIcono ? 'description' : solicitud.areaIcono}</span>
      <div class="relative flex flex-wrap items-center justify-between gap-3">
        <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold">
          <c:out value="${solicitud.areaNombre}"/> &bull; <c:out value="${solicitud.institucionSiglas}"/>
        </p>
        <c:set var="est" value="${solicitud.estado}"/><c:set var="estTexto" value="${solicitud.estadoTexto}"/>
        <span class="bg-white rounded-full"><%@include file="/WEB-INF/vistas/includes/badge_estado.jspf"%></span>
      </div>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold mt-2 relative"><c:out value="${solicitud.tramiteNombre}"/></h1>
      <div class="mt-4 relative flex flex-wrap items-center gap-x-6 gap-y-2">
        <div>
          <p class="text-caption uppercase text-primary-fixed">Folio</p>
          <p class="text-title-lg font-bold tracking-wide select-all"><c:out value="${solicitud.folio}"/></p>
        </div>
        <div>
          <p class="text-caption uppercase text-primary-fixed">Radicación</p>
          <p class="text-label-md">${solicitud.fechaCreacionTexto}</p>
        </div>
        <c:if test="${not empty solicitud.sedeNombre}">
          <div>
            <p class="text-caption uppercase text-primary-fixed">Sede</p>
            <p class="text-label-md"><c:out value="${solicitud.sedeNombre}"/></p>
          </div>
        </c:if>
      </div>
    </section>

    <div class="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
      <%-- Línea de tiempo --%>
      <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
        <div class="flex items-center justify-between gap-2 mb-4">
          <h2 class="text-title-lg font-bold text-primary flex items-center gap-2">
            <span class="material-symbols-outlined">timeline</span> Línea de tiempo
          </h2>
          <span class="text-caption text-secondary">${fn:length(historial)} de ${fn:length(historial) + fn:length(pendientes)} pasos</span>
        </div>
        <ol class="relative ml-4 border-l-2 border-surface-container">
          <c:forEach var="h" items="${historial}" varStatus="vs">
            <c:set var="esActual" value="${vs.last and solicitud.grupoEstado == 'PROCESO'}"/>
            <c:set var="esRechazo" value="${h.estadoNuevo == 'RECHAZADA'}"/>
            <li class="relative ml-6 pb-5">
              <span class="absolute -left-[37px] top-0 w-6 h-6 rounded-full flex items-center justify-center ring-4 ring-surface-container-lowest
                           ${esRechazo ? 'bg-error text-white' : (esActual ? 'bg-dorado text-white' : 'bg-primary text-white')}">
                <span class="material-symbols-outlined text-[14px]">${esRechazo ? 'close' : (esActual ? 'autorenew' : 'check')}</span>
              </span>
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="text-label-md text-on-surface">${h.estadoTexto}</h3>
                <c:if test="${esActual}"><span class="text-caption font-bold uppercase text-dorado">En curso</span></c:if>
              </div>
              <p class="text-caption text-secondary mt-0.5">${h.fechaTexto}</p>
              <c:if test="${not empty h.comentario}">
                <p class="text-body-sm text-on-surface-variant mt-1"><c:out value="${h.comentario}"/></p>
              </c:if>
              <c:if test="${h.hechoPorFuncionario and not empty h.usuarioNombre}">
                <p class="text-caption text-secondary mt-1">Por <c:out value="${h.usuarioNombre}"/></p>
              </c:if>
            </li>
          </c:forEach>
          <c:forEach var="p" items="${pendientes}" varStatus="vs">
            <li class="relative ml-6 ${vs.last ? '' : 'pb-5'}">
              <span class="absolute -left-[37px] top-0 w-6 h-6 rounded-full bg-surface-container text-outline flex items-center justify-center ring-4 ring-surface-container-lowest">
                <span class="material-symbols-outlined text-[14px]">schedule</span>
              </span>
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="text-label-md text-on-surface-variant">${p[1]}</h3>
                <span class="text-caption font-bold uppercase text-secondary">${vs.first ? 'Próximo paso' : 'Pendiente'}</span>
              </div>
              <p class="text-body-sm text-on-surface-variant mt-1">${p[2]}</p>
            </li>
          </c:forEach>
        </ol>
      </section>

      <div class="space-y-5">
        <%-- Observaciones del revisor --%>
        <c:if test="${not empty solicitud.observacionCiudadano}">
          <section class="rounded-2xl ${solicitud.estado == 'RECHAZADA' ? 'bg-error-container' : 'bg-surface-container-low'} p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2">
              <span class="material-symbols-outlined">rate_review</span> Observaciones del revisor
            </h2>
            <p class="text-body-sm text-on-surface mt-2">“<c:out value="${solicitud.observacionCiudadano}"/>”</p>
            <c:if test="${not empty solicitud.revisorNombre}">
              <p class="text-caption text-secondary mt-2"><c:out value="${solicitud.revisorNombre}"/> &bull; <c:out value="${solicitud.institucionSiglas}"/></p>
            </c:if>
          </section>
        </c:if>

        <%-- Datos de la solicitud (según el tipo de trámite) --%>
        <c:if test="${not empty partida}">
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2">
              <span class="material-symbols-outlined">assignment</span> Datos de la solicitud
            </h2>
            <dl class="mt-3 divide-y divide-surface-container">
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Persona inscrita</dt><dd class="text-label-md"><c:out value="${partida.nombreInscrito}"/></dd></div>
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Relación</dt><dd class="text-label-md">${partida.relacionTexto}</dd></div>
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Fecha de nacimiento</dt><dd class="text-label-md">${partida.fechaNacimientoTexto}</dd></div>
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Municipio de inscripción</dt><dd class="text-label-md"><c:out value="${partida.municipioNombre}"/></dd></div>
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Uso</dt><dd class="text-label-md">${partida.usoTexto}</dd></div>
            </dl>
          </section>
        </c:if>
        <c:if test="${not empty municipal}">
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2">
              <span class="material-symbols-outlined">assignment</span> Datos de la solicitud
            </h2>
            <dl class="mt-3 divide-y divide-surface-container">
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Municipio</dt><dd class="text-label-md"><c:out value="${municipal.municipioNombre}"/></dd></div>
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Dirección declarada</dt><dd class="text-label-md"><c:out value="${municipal.domicilio}"/></dd></div>
            </dl>
          </section>
        </c:if>
        <c:if test="${not empty dui}">
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2">
              <span class="material-symbols-outlined">assignment</span> Datos de la solicitud
            </h2>
            <dl class="mt-3 divide-y divide-surface-container">
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Motivo</dt><dd class="text-label-md">${dui.motivoTexto}</dd></div>
              <div class="py-2"><dt class="text-caption uppercase text-secondary">Residencia</dt><dd class="text-label-md"><c:out value="${dui.municipioNombre}"/>, <c:out value="${dui.departamentoNombre}"/></dd></div>
            </dl>
          </section>
        </c:if>

        <%-- Documentos --%>
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
          <div class="flex items-center justify-between gap-2">
            <h2 class="text-title-sm text-primary flex items-center gap-2">
              <span class="material-symbols-outlined">folder_open</span> Documentos
            </h2>
            <span class="text-caption text-secondary">${fn:length(documentos)} archivos</span>
          </div>
          <c:choose>
            <c:when test="${empty documentos}">
              <p class="text-body-sm text-on-surface-variant mt-2">Este trámite no tiene documentos adjuntos.</p>
            </c:when>
            <c:otherwise>
              <ul class="mt-3 space-y-2">
                <c:forEach var="d" items="${documentos}">
                  <li class="flex items-center gap-3 rounded-xl bg-surface-container-low p-3">
                    <span class="material-symbols-outlined ${d.pdf ? 'text-error' : 'text-primary'}">${d.pdf ? 'picture_as_pdf' : 'image'}</span>
                    <div class="min-w-0 flex-1">
                      <p class="text-label-md text-on-surface truncate"><c:out value="${d.nombreArchivo}"/></p>
                      <p class="text-caption text-secondary">${d.tamanoTexto}<c:if test="${not empty d.requisitoNombre}"> &bull; <c:out value="${d.requisitoNombre}"/></c:if></p>
                    </div>
                    <div class="flex flex-col items-end gap-1 shrink-0">
                      <span class="text-caption font-bold ${d.estadoCotejo == 'VALIDO' ? 'text-exito' : (d.estadoCotejo == 'RECHAZADO' ? 'text-error' : 'text-dorado')}">${d.estadoCotejoTexto}</span>
                      <a href="${ctx}/app/documento?id=${d.idDocumento}" target="_blank" rel="noopener"
                         class="text-label-sm text-primary hover:underline inline-flex items-center gap-0.5">
                        <span class="material-symbols-outlined text-[16px]">visibility</span> Ver
                      </a>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:otherwise>
          </c:choose>
        </section>

        <p class="text-caption text-secondary text-center">Última actualización: ${solicitud.fechaActualizacionTexto}</p>
      </div>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
