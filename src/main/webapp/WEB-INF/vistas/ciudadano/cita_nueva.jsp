<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Agendar cita</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-3xl px-4 sm:px-6 py-6 space-y-5">

    <a href="${ctx}/app/citas" class="inline-flex items-center gap-1 text-label-md text-primary hover:underline">
      <span class="material-symbols-outlined text-[18px]">arrow_back</span> Mis citas
    </a>

    <section>
      <p class="text-label-sm uppercase text-secondary">Salud Pública &bull; MINSAL</p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Reserva de atención presencial</h1>
      <p class="text-body-sm text-on-surface-variant mt-1">Elige el tipo de atención, la unidad de salud y el día.</p>
    </section>

    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span>
        <p class="text-label-md"><c:out value="${error}"/></p>
      </div>
    </c:if>

    <%-- 1 y 2: tipo y sede (GET: cada cambio recarga la página) --%>
    <form method="get" action="${ctx}/app/citas/nueva" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 space-y-4">
      <input type="hidden" name="fecha" value="${fn:escapeXml(fecha)}">
      <div>
        <label for="codigo" class="block text-label-md text-on-surface mb-1">1. Tipo de atención</label>
        <select id="codigo" name="codigo" class="campo" onchange="this.form.submit()">
          <c:forEach var="t" items="${tipos}">
            <option value="${t.codigo}" ${t.codigo == tipo.codigo ? 'selected' : ''}><c:out value="${t.nombre}"/></option>
          </c:forEach>
        </select>
      </div>
      <div>
        <label for="idSede" class="block text-label-md text-on-surface mb-1">2. Unidad de salud</label>
        <select id="idSede" name="idSede" class="campo" onchange="this.form.submit()">
          <option value="">Selecciona una unidad</option>
          <c:forEach var="s" items="${sedes}">
            <option value="${s.idSede}" ${not empty sede and s.idSede == sede.idSede ? 'selected' : ''}><c:out value="${s.nombre}"/> &mdash; <c:out value="${s.municipioNombre}"/></option>
          </c:forEach>
        </select>
        <c:if test="${not empty sede and not empty sede.horario}">
          <p class="text-caption text-secondary mt-1 flex items-center gap-1">
            <span class="material-symbols-outlined text-[14px]">schedule</span> <c:out value="${sede.horario}"/>
          </p>
        </c:if>
      </div>
      <noscript><button type="submit" class="boton-borde">Actualizar</button></noscript>
    </form>

    <%-- 3: día --%>
    <c:if test="${not empty sede}">
      <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
        <h2 class="text-label-md text-on-surface mb-3">3. Selecciona el día</h2>
        <div class="flex gap-2 overflow-x-auto pb-1 -mx-1 px-1">
          <c:forEach var="d" items="${dias}">
            <c:url var="urlDia" value="/app/citas/nueva">
              <c:param name="codigo" value="${tipo.codigo}"/><c:param name="idSede" value="${sede.idSede}"/><c:param name="fecha" value="${d[0]}"/>
            </c:url>
            <a href="${urlDia}" class="shrink-0 w-16 rounded-xl border text-center py-2 transition-colors
                      ${d[0] == fecha ? 'bg-primary border-primary text-on-primary' : 'border-surface-container hover:bg-surface-container-low'}"
               ${d[0] == fecha ? 'aria-current="date"' : ''}>
              <span class="block text-caption uppercase ${d[0] == fecha ? 'text-primary-fixed' : 'text-secondary'}">${d[1]}</span>
              <span class="block text-title-lg font-bold">${d[2]}</span>
              <span class="block text-caption uppercase ${d[0] == fecha ? 'text-primary-fixed' : 'text-secondary'}">${d[3]}</span>
            </a>
          </c:forEach>
        </div>
      </section>
    </c:if>

    <%-- 4: horario --%>
    <c:if test="${not empty fecha and not empty sede}">
      <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
        <h2 class="text-label-md text-on-surface">4. Elige el horario</h2>
        <p class="text-body-sm text-on-surface-variant mb-3"><c:out value="${fechaTexto}"/> &bull; <c:out value="${sede.nombre}"/></p>
        <c:choose>
          <c:when test="${empty cupos}">
            <p class="text-body-sm text-on-surface-variant">No hay horarios para este día.</p>
          </c:when>
          <c:otherwise>
            <ul class="space-y-2">
              <c:forEach var="cu" items="${cupos}">
                <li class="rounded-xl border border-surface-container p-3 flex flex-wrap items-center justify-between gap-3">
                  <div class="min-w-0">
                    <p class="text-label-md text-on-surface">${cu.horarioTexto}</p>
                    <div class="flex items-center gap-2 mt-1">
                      <div class="w-24 h-1.5 rounded-full bg-surface-container overflow-hidden">
                        <div class="h-full rounded-full ${cu.lleno ? 'bg-error' : 'bg-primary'}" style="width: ${cu.porcentajeOcupado}%"></div>
                      </div>
                      <span class="text-caption ${cu.lleno ? 'text-error font-bold' : 'text-secondary'}">
                        ${cu.lleno ? 'Lleno' : cu.disponibles.toString().concat(' turnos libres')}
                      </span>
                    </div>
                  </div>
                  <c:choose>
                    <c:when test="${cu.habilitado and not cu.lleno}">
                      <form method="post" action="${ctx}/app/citas/nueva">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="idCupo" value="${cu.idCupo}">
                        <input type="hidden" name="codigo" value="${tipo.codigo}">
                        <input type="hidden" name="idSede" value="${sede.idSede}">
                        <input type="hidden" name="fecha" value="${fecha}">
                        <button type="submit" class="boton-primario">
                          <span class="material-symbols-outlined text-[18px]">event_available</span> Reservar
                        </button>
                      </form>
                    </c:when>
                    <c:otherwise>
                      <span class="boton-borde opacity-50 cursor-not-allowed">No disponible</span>
                    </c:otherwise>
                  </c:choose>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </section>
    </c:if>

    <aside class="rounded-2xl bg-surface-container-low p-4 flex items-start gap-3">
      <span class="material-symbols-outlined text-primary">info</span>
      <p class="text-body-sm text-on-surface-variant">
        Preséntate 15 minutos antes con tu DUI. Puedes cancelar la cita hasta 2 horas antes desde "Mis citas".
      </p>
    </aside>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
