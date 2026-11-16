<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Multas e infracciones</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-3xl px-4 sm:px-6 py-6 space-y-5">

    <section>
      <p class="text-label-sm uppercase text-secondary">Transporte y movilidad &bull; VMT</p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Consulta de multas e infracciones</h1>
    </section>

    <%-- Criterio de búsqueda --%>
    <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 space-y-4">
      <div class="grid grid-cols-2 gap-2 bg-surface-container-high rounded-lg p-1" role="tablist">
        <a href="${ctx}/app/multas?tipo=dui" role="tab" aria-selected="${tipo == 'dui'}"
           class="py-2.5 rounded-md text-label-md flex items-center justify-center gap-1.5 ${tipo == 'dui' ? 'bg-surface-container-lowest text-primary font-bold shadow-sm' : 'text-on-surface-variant'}">
          <span class="material-symbols-outlined text-[18px]">badge</span> Por licencia
        </a>
        <a href="${ctx}/app/multas?tipo=placa" role="tab" aria-selected="${tipo == 'placa'}"
           class="py-2.5 rounded-md text-label-md flex items-center justify-center gap-1.5 ${tipo == 'placa' ? 'bg-surface-container-lowest text-primary font-bold shadow-sm' : 'text-on-surface-variant'}">
          <span class="material-symbols-outlined text-[18px]">directions_car</span> Por placa
        </a>
      </div>

      <c:choose>
        <c:when test="${tipo == 'dui'}">
          <div class="rounded-xl bg-surface-container-low p-4 flex items-center gap-3">
            <span class="material-symbols-outlined text-primary">id_card</span>
            <div>
              <p class="text-caption uppercase text-secondary font-semibold">Licencia vinculada a tu DUI</p>
              <p class="text-label-md text-on-surface"><c:out value="${sessionScope.usuario.dui}"/></p>
            </div>
          </div>
        </c:when>
        <c:otherwise>
          <form method="get" action="${ctx}/app/multas" class="flex flex-col sm:flex-row gap-3">
            <input type="hidden" name="tipo" value="placa">
            <div class="relative flex-1">
              <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-secondary text-[20px] pointer-events-none">pin</span>
              <input type="text" name="placa" value="${fn:escapeXml(placa)}" required maxlength="10" placeholder="P123-456"
                     autocapitalize="characters" class="campo pl-10 uppercase" aria-label="Número de placa">
            </div>
            <button type="submit" class="boton-primario">
              <span class="material-symbols-outlined text-[18px]">search</span> Buscar
            </button>
          </form>
        </c:otherwise>
      </c:choose>
    </section>

    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span>
        <p class="text-label-md"><c:out value="${error}"/></p>
      </div>
    </c:if>

    <c:if test="${consultado}">
      <%-- Estado de solvencia --%>
      <section class="rounded-2xl p-5 flex flex-wrap items-center justify-between gap-4 ${solvente ? 'bg-[#EAF5EE]' : 'bg-primary text-on-primary'}">
        <div class="flex items-center gap-3">
          <span class="material-symbols-outlined icono-relleno text-[36px] ${solvente ? 'text-exito' : 'text-dorado-claro'}">${solvente ? 'verified' : 'warning'}</span>
          <div>
            <p class="text-caption uppercase font-semibold ${solvente ? 'text-exito' : 'text-primary-fixed'}">Estado de solvencia</p>
            <p class="text-title-lg font-bold ${solvente ? 'text-exito' : ''}">${solvente ? 'Solvente' : pendientes.toString().concat(pendientes == 1 ? ' multa pendiente' : ' multas pendientes')}</p>
          </div>
        </div>
        <c:if test="${not solvente}">
          <div class="text-right">
            <p class="text-caption uppercase text-primary-fixed">Monto pendiente</p>
            <p class="font-display text-[28px] leading-9 font-bold">$<fmt:formatNumber value="${montoPendiente}" minFractionDigits="2" maxFractionDigits="2"/> <span class="text-label-sm">USD</span></p>
          </div>
        </c:if>
      </section>

      <%-- Filtros --%>
      <c:url var="base" value="/app/multas"><c:param name="tipo" value="${tipo}"/><c:if test="${tipo == 'placa'}"><c:param name="placa" value="${placa}"/></c:if></c:url>
      <nav class="flex gap-2 overflow-x-auto pb-1" aria-label="Filtrar por estado">
        <a href="${base}" class="shrink-0 boton-borde ${empty param.estado ? 'bg-primary-fixed' : ''}">Todas (${total})</a>
        <a href="${base}&estado=PENDIENTE" class="shrink-0 boton-borde ${param.estado == 'PENDIENTE' ? 'bg-primary-fixed' : ''}">Pendientes (${pendientes})</a>
        <a href="${base}&estado=PAGADA" class="shrink-0 boton-borde ${param.estado == 'PAGADA' ? 'bg-primary-fixed' : ''}">Pagadas (${pagadas})</a>
        <a href="${base}&estado=EN_APELACION" class="shrink-0 boton-borde ${param.estado == 'EN_APELACION' ? 'bg-primary-fixed' : ''}">En apelación (${apelacion})</a>
      </nav>

      <%-- Infracciones --%>
      <c:choose>
        <c:when test="${empty multas}">
          <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-8 text-center">
            <span class="material-symbols-outlined text-[40px] text-outline">task_alt</span>
            <p class="text-title-sm text-primary mt-2">No se encontraron registros</p>
            <p class="text-body-sm text-on-surface-variant mt-1">No hay infracciones con este criterio.</p>
          </div>
        </c:when>
        <c:otherwise>
          <ul class="space-y-3">
            <c:forEach var="m" items="${multas}">
              <li class="rounded-2xl bg-surface-container-lowest border p-4 sm:p-5 ${m.pendiente ? 'border-dorado/40' : 'border-surface-container'}">
                <div class="flex flex-wrap items-center justify-between gap-2">
                  <p class="text-caption uppercase text-secondary font-semibold">Folio <c:out value="${m.folio}"/> &bull; Código <c:out value="${m.codigoInfraccion}"/></p>
                  <span class="text-caption font-bold uppercase rounded-full px-2 py-0.5
                        ${m.vencida ? 'bg-error-container text-[#93000a]' : (m.pendiente ? 'bg-dorado/15 text-dorado' : (m.estado == 'PAGADA' ? 'bg-[#EAF5EE] text-exito' : 'bg-primary-fixed text-primary'))}">${m.estadoTexto}</span>
                </div>
                <h2 class="text-title-sm text-primary mt-1">${m.gravedadTexto}</h2>
                <p class="text-body-sm text-on-surface mt-1"><c:out value="${m.descripcion}"/></p>
                <ul class="text-caption text-secondary mt-2 space-y-1">
                  <li class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">calendar_today</span>${m.fechaHoraTexto}</li>
                  <c:if test="${not empty m.lugar}"><li class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">location_on</span><c:out value="${m.lugar}"/></li></c:if>
                  <c:if test="${not empty m.agente}"><li class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">shield_person</span><c:out value="${m.agente}"/></li></c:if>
                  <c:if test="${not empty m.placa}"><li class="flex items-center gap-1"><span class="material-symbols-outlined text-[14px]">directions_car</span>Placa <c:out value="${m.placa}"/></li></c:if>
                </ul>
                <div class="mt-3 pt-3 border-t border-surface-container flex flex-wrap items-end justify-between gap-2">
                  <div>
                    <p class="text-caption uppercase text-secondary">${m.estado == 'PAGADA' ? 'Monto cancelado' : 'Monto de la sanción'}</p>
                    <p class="text-title-lg font-bold text-primary">$<fmt:formatNumber value="${m.monto}" minFractionDigits="2" maxFractionDigits="2"/></p>
                  </div>
                  <c:choose>
                    <c:when test="${m.pendiente}">
                      <p class="text-label-sm ${m.vencida ? 'text-error' : 'text-dorado'}">${m.vencimientoTexto} (${m.fechaVencimientoTexto})</p>
                    </c:when>
                    <c:when test="${m.estado == 'PAGADA'}">
                      <p class="text-caption text-secondary text-right">Recibo <c:out value="${m.reciboPago}"/><br>${m.fechaPagoTexto}</p>
                    </c:when>
                  </c:choose>
                </div>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </c:if>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
