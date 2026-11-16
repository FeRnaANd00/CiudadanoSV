<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Mis citas</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-3xl px-4 sm:px-6 py-6 space-y-5">

    <section class="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
      <div>
        <p class="text-label-sm uppercase text-secondary">Atención presencial</p>
        <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Mis citas</h1>
      </div>
      <a href="${ctx}/app/citas/nueva" class="boton-primario self-start sm:self-auto">
        <span class="material-symbols-outlined text-[18px]">add_circle</span> Agendar cita
      </a>
    </section>

    <%-- Mensajes de una sola vez --%>
    <c:if test="${not empty sessionScope.aviso}">
      <div class="flex items-start gap-3 rounded-xl bg-[#EAF5EE] text-exito p-4" role="status">
        <span class="material-symbols-outlined icono-relleno">check_circle</span>
        <p class="text-label-md"><c:out value="${sessionScope.aviso}"/></p>
      </div>
      <c:remove var="aviso" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.avisoError}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span>
        <p class="text-label-md"><c:out value="${sessionScope.avisoError}"/></p>
      </div>
      <c:remove var="avisoError" scope="session"/>
    </c:if>
    <c:if test="${not empty errorBD}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">database</span>
        <p class="text-label-md"><c:out value="${errorBD}"/></p>
      </div>
    </c:if>

    <%-- Próximas --%>
    <section>
      <h2 class="text-title-lg font-bold text-primary mb-3">Próximas</h2>
      <c:choose>
        <c:when test="${empty proximas}">
          <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-6 text-center">
            <span class="material-symbols-outlined text-[40px] text-outline">event_busy</span>
            <p class="text-title-sm text-primary mt-2">No tienes citas programadas</p>
            <a href="${ctx}/app/citas/nueva" class="boton-primario mt-4">Agendar una cita</a>
          </div>
        </c:when>
        <c:otherwise>
          <ul class="space-y-3">
            <c:forEach var="ci" items="${proximas}">
              <li class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 sm:p-5 flex gap-4">
                <div class="w-16 shrink-0 rounded-xl bg-primary text-on-primary text-center py-2 self-start">
                  <span class="block text-headline-md font-bold leading-none">${ci.diaNumero}</span>
                  <span class="block text-caption uppercase text-primary-fixed mt-1">${ci.mesCorto}</span>
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center justify-between gap-2">
                    <h3 class="text-title-sm text-primary"><c:out value="${ci.tramiteNombre}"/></h3>
                    <span class="text-caption font-bold uppercase rounded-full bg-tertiary-fixed-dim/20 text-[#00606F] px-2 py-0.5">${ci.cuandoTexto}</span>
                  </div>
                  <p class="text-body-sm text-on-surface-variant mt-0.5 first-letter:uppercase">${ci.fechaTexto} &bull; ${ci.horaTexto}</p>
                  <p class="text-caption text-secondary mt-1 flex flex-wrap gap-x-3 gap-y-1">
                    <span class="inline-flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">location_on</span><c:out value="${ci.sedeNombre}"/></span>
                    <span class="inline-flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">confirmation_number</span>Turno #${ci.numeroTurno}</span>
                    <span class="inline-flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">tag</span><c:out value="${ci.codigo}"/></span>
                  </p>
                  <c:if test="${ci.cancelable}">
                    <form method="post" action="${ctx}/app/citas" class="mt-3"
                          onsubmit="return confirm('¿Cancelar la cita ${ci.codigo}? El turno quedará libre para otra persona.');">
                      <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                      <input type="hidden" name="accion" value="cancelar">
                      <input type="hidden" name="idCita" value="${ci.idCita}">
                      <button type="submit" class="text-label-sm text-error hover:underline inline-flex items-center gap-1">
                        <span class="material-symbols-outlined text-[16px]">event_busy</span> Cancelar cita
                      </button>
                    </form>
                  </c:if>
                </div>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>

    <%-- Historial --%>
    <c:if test="${not empty historial}">
      <section>
        <h2 class="text-title-lg font-bold text-primary mb-3">Historial</h2>
        <ul class="rounded-2xl bg-surface-container-lowest border border-surface-container divide-y divide-surface-container">
          <c:forEach var="ci" items="${historial}">
            <li class="p-4 flex flex-wrap items-center justify-between gap-2">
              <div class="min-w-0">
                <p class="text-label-md text-on-surface"><c:out value="${ci.tramiteNombre}"/></p>
                <p class="text-caption text-secondary first-letter:uppercase">${ci.fechaTexto} &bull; ${ci.horaTexto} &bull; <c:out value="${ci.sedeNombre}"/></p>
              </div>
              <span class="text-caption font-bold uppercase rounded-full px-2 py-0.5
                    ${ci.estado == 'ATENDIDA' ? 'bg-[#EAF5EE] text-exito' : (ci.estado == 'CANCELADA' ? 'bg-surface-container text-on-surface-variant' : 'bg-error-container text-[#93000a]')}">${ci.estadoTexto}</span>
            </li>
          </c:forEach>
        </ul>
      </section>
    </c:if>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
