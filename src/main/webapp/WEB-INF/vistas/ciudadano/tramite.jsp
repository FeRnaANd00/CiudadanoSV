<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | <c:out value="${empty tramite ? 'Trámite' : tramite.nombre}"/></title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-4xl px-4 sm:px-6 py-6 space-y-5">

    <a href="${ctx}/inicio#servicios" class="inline-flex items-center gap-1 text-label-md text-primary hover:underline">
      <span class="material-symbols-outlined text-[18px]">arrow_back</span> Volver al catálogo
    </a>

    <%-- Mensaje de una sola vez (ej. "Sesión iniciada"): se muestra y se borra --%>
    <c:if test="${not empty sessionScope.aviso}">
      <div class="flex items-start gap-3 rounded-xl bg-[#EAF5EE] text-exito p-4" role="status">
        <span class="material-symbols-outlined icono-relleno">check_circle</span>
        <p class="text-label-md"><c:out value="${sessionScope.aviso}"/></p>
      </div>
      <c:remove var="aviso" scope="session"/>
    </c:if>

    <c:choose>
      <c:when test="${not empty errorBD}">
        <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
          <span class="material-symbols-outlined">database</span>
          <p class="text-label-md"><c:out value="${errorBD}"/></p>
        </div>
      </c:when>
      <c:otherwise>
        <%-- Encabezado del trámite --%>
        <section class="relative overflow-hidden rounded-2xl bg-primary text-on-primary p-6">
          <span class="material-symbols-outlined absolute -right-6 -bottom-8 text-[150px] opacity-10 pointer-events-none select-none" aria-hidden="true">assured_workload</span>
          <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold relative">
            <c:out value="${tramite.areaNombre}"/> &bull; <c:out value="${tramite.institucionSiglas}"/>
          </p>
          <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold mt-2 relative"><c:out value="${tramite.nombre}"/></h1>
          <p class="text-body-sm text-primary-fixed mt-1 relative"><c:out value="${tramite.institucionNombre}"/> &bull; Ref. <c:out value="${tramite.codigo}"/></p>

          <dl class="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-6 relative">
            <div class="rounded-xl bg-white/10 p-3">
              <dt class="text-caption uppercase text-primary-fixed">Arancel</dt>
              <dd class="text-title-lg font-bold mt-1">
                <c:choose>
                  <c:when test="${tramite.gratuito}">Gratuito</c:when>
                  <c:otherwise>$<fmt:formatNumber value="${tramite.arancel}" minFractionDigits="2" maxFractionDigits="2"/></c:otherwise>
                </c:choose>
              </dd>
            </div>
            <div class="rounded-xl bg-white/10 p-3">
              <dt class="text-caption uppercase text-primary-fixed">Plazo</dt>
              <dd class="text-title-lg font-bold mt-1">${empty tramite.plazoHoras ? 'Inmediato' : tramite.plazoHoras.toString().concat(' h hábiles')}</dd>
            </div>
            <div class="rounded-xl bg-white/10 p-3">
              <dt class="text-caption uppercase text-primary-fixed">Modalidad</dt>
              <dd class="text-title-lg font-bold mt-1">
                <c:choose>
                  <c:when test="${tramite.modalidad == 'DIGITAL'}">100% digital</c:when>
                  <c:when test="${tramite.modalidad == 'PRESENCIAL'}">Presencial</c:when>
                  <c:otherwise>Mixta</c:otherwise>
                </c:choose>
              </dd>
            </div>
            <div class="rounded-xl bg-white/10 p-3">
              <dt class="text-caption uppercase text-primary-fixed">Cita</dt>
              <dd class="text-title-lg font-bold mt-1">${tramite.requiereCita ? 'Requerida' : 'No requiere'}</dd>
            </div>
          </dl>
        </section>

        <%-- Requisitos --%>
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
          <div class="flex items-center gap-2 mb-4">
            <span class="material-symbols-outlined text-primary">fact_check</span>
            <h2 class="text-title-lg font-bold text-primary">Requisitos</h2>
          </div>
          <c:choose>
            <c:when test="${empty requisitos}">
              <p class="text-body-sm text-on-surface-variant">Este trámite no tiene requisitos adicionales: solo necesitas tu cuenta verificada.</p>
            </c:when>
            <c:otherwise>
              <ul class="space-y-3">
                <c:forEach var="r" items="${requisitos}">
                  <li class="flex items-start gap-3 rounded-xl bg-surface-container-low p-3">
                    <span class="material-symbols-outlined ${r.obligatorio ? 'text-primary' : 'text-outline'}">${r.requiereArchivo ? 'upload_file' : (r.tipoValidacion == 'PRESENCIAL' ? 'storefront' : 'check_circle')}</span>
                    <div class="min-w-0">
                      <p class="text-label-md text-on-surface">
                        <c:out value="${r.nombre}"/>
                        <c:if test="${not r.obligatorio}"><span class="text-caption text-secondary font-semibold">(opcional)</span></c:if>
                      </p>
                      <c:if test="${not empty r.descripcion}">
                        <p class="text-body-sm text-on-surface-variant mt-0.5"><c:out value="${r.descripcion}"/></p>
                      </c:if>
                      <c:if test="${r.requiereArchivo}">
                        <p class="text-caption text-secondary mt-1">Adjuntar en línea: <c:out value="${r.formatos}"/> &bull; máx. ${r.tamanoMaxMb} MB</p>
                      </c:if>
                      <c:if test="${r.tipoValidacion == 'PRESENCIAL'}">
                        <p class="text-caption text-secondary mt-1">Se presenta en ventanilla el día de la cita.</p>
                      </c:if>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:otherwise>
          </c:choose>
        </section>

        <%-- Acción --%>
        <section class="rounded-2xl bg-surface-container-low p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <p class="text-title-sm text-primary">¿Tienes todo listo?</p>
            <p class="text-body-sm text-on-surface-variant">Al enviar la solicitud recibirás un folio para darle seguimiento.</p>
          </div>
          <c:choose>
            <c:when test="${tramite.disponible and not empty urlFormulario}">
              <a href="${urlFormulario}" class="boton-primario">
                <span class="material-symbols-outlined text-[18px]">${empty iconoBoton ? 'edit_document' : iconoBoton}</span>
                ${empty textoBoton ? 'Iniciar solicitud' : textoBoton}
              </a>
            </c:when>
            <c:when test="${tramite.disponible}">
              <span class="boton-primario opacity-60 cursor-not-allowed" title="El formulario se construye en los siguientes pasos">
                <span class="material-symbols-outlined text-[18px]">edit_document</span> Iniciar solicitud (próximamente)
              </span>
            </c:when>
            <c:otherwise>
              <span class="text-caption font-bold uppercase rounded-full bg-dorado/15 text-dorado px-3 py-1.5">Trámite próximamente disponible</span>
            </c:otherwise>
          </c:choose>
        </section>
      </c:otherwise>
    </c:choose>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
