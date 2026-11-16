<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Portal de Servicios Digitales</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">

  <%-- ===== Mensajes ===== --%>
  <c:if test="${not empty aviso or not empty errorBD}">
    <div class="mx-auto max-w-6xl px-4 sm:px-6 pt-6 space-y-3">
      <c:if test="${not empty aviso}">
        <div class="flex items-start gap-3 rounded-xl bg-[#EAF5EE] text-exito p-4" role="status">
          <span class="material-symbols-outlined icono-relleno">check_circle</span>
          <p class="text-label-md"><c:out value="${aviso}"/></p>
        </div>
      </c:if>
      <c:if test="${not empty errorBD}">
        <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
          <span class="material-symbols-outlined">database</span>
          <p class="text-label-md"><c:out value="${errorBD}"/></p>
        </div>
      </c:if>
    </div>
  </c:if>

  <%-- ===== Hero ===== --%>
  <section class="mx-auto max-w-6xl px-4 sm:px-6 pt-8 pb-6 grid gap-6 lg:grid-cols-[1.4fr_1fr] items-stretch">
    <div class="flex flex-col justify-center">

      <h1 class="font-display text-[30px] leading-[38px] sm:text-display-lg font-bold text-primary mt-4">
        Tus trámites del Estado, en un solo lugar
      </h1>
      <p class="text-body-lg text-on-surface-variant mt-3 max-w-xl">
        Solicita tu DUI, agenda citas médicas, consulta esquelas del VMT y obtén constancias
        municipales sin hacer filas. Elige un servicio para comenzar.
      </p>
      <div class="flex flex-wrap gap-3 mt-6">
        <a href="#servicios" class="boton-primario">
          <span class="material-symbols-outlined text-[18px]">apps</span>
          Ver servicios
        </a>
        <c:if test="${empty sessionScope.usuario}">
          <a href="${pageContext.request.contextPath}/login?tab=registro" class="boton-borde">
            <span class="material-symbols-outlined text-[18px]">person_add</span>
            Crear cuenta
          </a>
        </c:if>
      </div>
    </div>

    <%-- Tarjeta institucional --%>
    <div class="relative overflow-hidden rounded-2xl bg-primary text-on-primary p-6 flex flex-col justify-between min-h-[240px]">
      <span class="material-symbols-outlined absolute -right-6 -bottom-8 text-[160px] opacity-10 pointer-events-none select-none" aria-hidden="true">shield</span>
      <div class="flex items-center gap-3.5 relative">
        <span class="w-12 h-12 rounded-full bg-primary-container border border-white/20 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined icono-relleno text-[26px] text-tertiary-fixed">verified</span>
        </span>
        <div>
          <p class="font-display text-2xl font-bold leading-none">Ciudadano<span class="text-tertiary-fixed">SV</span></p>
          <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold mt-1.5">Portal de Servicios Digitales</p>
        </div>
      </div>
      <dl class="grid grid-cols-3 gap-3 mt-8 relative">
        <div class="rounded-xl bg-white/10 p-3">
          <dt class="text-caption uppercase text-primary-fixed">Áreas</dt>
          <dd class="text-headline-md font-bold mt-1">${empty totalAreas ? '—' : totalAreas}</dd>
        </div>
        <div class="rounded-xl bg-white/10 p-3">
          <dt class="text-caption uppercase text-primary-fixed">Trámites</dt>
          <dd class="text-headline-md font-bold mt-1">${empty totalTramites ? '—' : totalTramites}</dd>
        </div>
        <div class="rounded-xl bg-white/10 p-3">
          <dt class="text-caption uppercase text-primary-fixed">Sedes</dt>
          <dd class="text-headline-md font-bold mt-1">${empty totalSedes ? '—' : totalSedes}</dd>
        </div>
      </dl>
    </div>
  </section>

  <%-- ===== Catálogo de servicios ===== --%>
  <section id="servicios" class="mx-auto max-w-6xl px-4 sm:px-6 py-8 scroll-mt-20">
    <div class="flex items-end justify-between gap-3 mb-5">
      <div>
        <p class="text-label-sm uppercase text-secondary">Catálogo</p>
        <h2 class="text-headline-md font-bold text-primary mt-1">Servicios públicos disponibles</h2>
        <p class="text-body-sm text-on-surface-variant mt-1">
          <c:choose>
            <c:when test="${not empty sessionScope.usuario}">Selecciona un trámite para ver sus requisitos e iniciar tu solicitud.</c:when>
            <c:otherwise>Selecciona un trámite; te pediremos iniciar sesión para continuar.</c:otherwise>
          </c:choose>
        </p>
      </div>
    </div>

    <c:choose>
      <c:when test="${empty areas}">
        <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-8 text-center">
          <span class="material-symbols-outlined text-[40px] text-outline">inventory_2</span>
          <p class="text-title-sm text-primary mt-2">El catálogo no está disponible en este momento</p>
          <p class="text-body-sm text-on-surface-variant mt-1">Intenta de nuevo en unos minutos.</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <c:forEach var="area" items="${areas}">
            <%-- Color del icono según el área (mismos tonos que catalogo_Servicios.html) --%>
            <c:choose>
              <c:when test="${area.icono == 'local_hospital'}"><c:set var="tono" value="bg-tertiary-fixed-dim/20 text-[#00606F]"/></c:when>
              <c:when test="${area.icono == 'badge'}"><c:set var="tono" value="bg-primary-fixed text-primary"/></c:when>
              <c:when test="${area.icono == 'school'}"><c:set var="tono" value="bg-secondary-fixed text-secondary"/></c:when>
              <c:when test="${area.icono == 'commute'}"><c:set var="tono" value="bg-surface-container-high text-primary"/></c:when>
              <c:otherwise><c:set var="tono" value="bg-tertiary/10 text-tertiary"/></c:otherwise>
            </c:choose>

            <article class="tarjeta-area rounded-2xl bg-surface-container-lowest border border-surface-container p-5 flex flex-col">
              <div class="flex items-center gap-3 mb-4">
                <span class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0 ${tono}">
                  <span class="material-symbols-outlined">${empty area.icono ? 'account_balance' : area.icono}</span>
                </span>
                <div class="min-w-0">
                  <h3 class="text-title-lg font-bold text-primary leading-tight"><c:out value="${area.nombre}"/></h3>
                  <p class="text-caption text-on-surface-variant uppercase mt-0.5">${area.tramites.size()} trámites</p>
                </div>
              </div>

              <ul class="flex flex-col gap-1 -mx-2">
                <c:forEach var="t" items="${area.tramites}">
                  <li>
                    <c:choose>
                      <c:when test="${t.disponible}">
                        <%-- Con sesión va directo al trámite; sin sesión, al login --%>
                        <c:choose>
                          <c:when test="${not empty sessionScope.usuario}">
                            <c:url var="urlTramite" value="/app/tramite"><c:param name="codigo" value="${t.codigo}"/></c:url>
                          </c:when>
                          <c:otherwise>
                            <c:url var="urlTramite" value="/login"><c:param name="servicio" value="${t.codigo}"/></c:url>
                          </c:otherwise>
                        </c:choose>
                        <a href="${urlTramite}" class="tarjeta-tramite flex items-center justify-between gap-2 rounded-lg px-2 py-2">
                          <span class="text-label-md text-on-surface"><c:out value="${t.nombre}"/></span>
                          <span class="material-symbols-outlined flecha text-[18px] text-outline">arrow_forward</span>
                        </a>
                      </c:when>
                      <c:otherwise>
                        <div class="flex items-center justify-between gap-2 rounded-lg px-2 py-2 opacity-70" aria-disabled="true">
                          <span class="text-label-md text-on-surface-variant"><c:out value="${t.nombre}"/></span>
                          <span class="text-caption font-bold uppercase rounded-full bg-dorado/15 text-dorado px-2 py-0.5 whitespace-nowrap">Próximamente</span>
                        </div>
                      </c:otherwise>
                    </c:choose>
                  </li>
                </c:forEach>
                <c:if test="${empty area.tramites}">
                  <li class="px-2 py-2 text-body-sm text-on-surface-variant">Sin trámites disponibles por ahora.</li>
                </c:if>
              </ul>
            </article>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </section>

  <%-- ===== Cómo funciona ===== --%>
  <section class="mx-auto max-w-6xl px-4 sm:px-6 pb-12">
    <div class="rounded-2xl bg-surface-container-low p-6">
      <h2 class="text-headline-md font-bold text-primary">¿Cómo funciona?</h2>
      <ol class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mt-5">
        <li class="flex gap-3">
          <span class="w-9 h-9 rounded-full bg-primary text-on-primary flex items-center justify-center font-bold shrink-0">1</span>
          <div><p class="text-title-sm text-primary">Elige el servicio</p><p class="text-body-sm text-on-surface-variant">Busca el trámite en el catálogo.</p></div>
        </li>
        <li class="flex gap-3">
          <span class="w-9 h-9 rounded-full bg-primary text-on-primary flex items-center justify-center font-bold shrink-0">2</span>
          <div><p class="text-title-sm text-primary">Inicia sesión</p><p class="text-body-sm text-on-surface-variant">Con tu DUI o correo electrónico.</p></div>
        </li>
        <li class="flex gap-3">
          <span class="w-9 h-9 rounded-full bg-primary text-on-primary flex items-center justify-center font-bold shrink-0">3</span>
          <div><p class="text-title-sm text-primary">Completa la solicitud</p><p class="text-body-sm text-on-surface-variant">Adjunta tus documentos o agenda tu cita.</p></div>
        </li>
        <li class="flex gap-3">
          <span class="w-9 h-9 rounded-full bg-primary text-on-primary flex items-center justify-center font-bold shrink-0">4</span>
          <div><p class="text-title-sm text-primary">Da seguimiento</p><p class="text-body-sm text-on-surface-variant">Recibe notificaciones del estado de tu trámite.</p></div>
        </li>
      </ol>
    </div>
  </section>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
