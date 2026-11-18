<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Notificaciones</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-3xl px-4 sm:px-6 py-6 space-y-5">

    <%-- Encabezado --%>
    <section class="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
      <div class="flex items-center gap-3">
        <span class="w-12 h-12 rounded-xl bg-primary flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined icono-relleno text-tertiary-fixed">notifications_active</span>
        </span>
        <div>
          <p class="text-label-sm uppercase text-secondary">Bandeja oficial ciudadana</p>
          <h1 class="font-display text-[24px] leading-8 sm:text-headline-lg font-bold text-primary">Notificaciones y actividad</h1>
        </div>
      </div>
      <c:if test="${vista == 'bandeja' and conteo.NO_LEIDAS > 0}">
        <form method="post" action="${ctx}/app/notificaciones" class="self-start sm:self-auto">
          <input type="hidden" name="csrf" value="${sessionScope.csrf}">
          <input type="hidden" name="accion" value="todas">
          <c:if test="${not empty tipo}"><input type="hidden" name="tipo" value="${tipo}"></c:if>
          <button type="submit" class="boton-borde">
            <span class="material-symbols-outlined text-[18px]">done_all</span> Marcar leídas
          </button>
        </form>
      </c:if>
    </section>

    <%-- Pestañas: Notificaciones / Historial --%>
    <nav class="grid grid-cols-2 gap-1 rounded-full bg-surface-container p-1" aria-label="Secciones">
      <a href="${ctx}/app/notificaciones"
         class="py-2.5 rounded-full text-label-md text-center flex items-center justify-center gap-1.5 ${vista == 'bandeja' ? 'bg-primary text-white shadow-sm' : 'text-secondary hover:text-primary'}"
         ${vista == 'bandeja' ? 'aria-current="page"' : ''}>
        <span class="material-symbols-outlined text-[18px]">notifications</span> Notificaciones
        <c:if test="${conteo.NO_LEIDAS > 0}">
          <span class="rounded-full bg-error text-white text-[11px] font-bold px-1.5 leading-5">${conteo.NO_LEIDAS}</span>
        </c:if>
      </a>
      <a href="${ctx}/app/notificaciones?vista=historial"
         class="py-2.5 rounded-full text-label-md text-center flex items-center justify-center gap-1.5 ${vista == 'historial' ? 'bg-primary text-white shadow-sm' : 'text-secondary hover:text-primary'}"
         ${vista == 'historial' ? 'aria-current="page"' : ''}>
        <span class="material-symbols-outlined text-[18px]">history</span> Historial
      </a>
    </nav>

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

    <c:choose>
      <%-- ================= BANDEJA ================= --%>
      <c:when test="${vista == 'bandeja'}">
        <%-- Filtros --%>
        <div class="flex gap-2 overflow-x-auto pb-1 -mx-1 px-1" role="group" aria-label="Filtrar por tipo">
          <c:set var="chipBase" value="shrink-0 inline-flex items-center gap-1.5 rounded-full border px-3.5 py-1.5 text-label-sm"/>
          <c:set var="chipOn" value="bg-primary border-primary text-white"/>
          <c:set var="chipOff" value="bg-surface-container-lowest border-surface-container text-secondary hover:border-primary hover:text-primary"/>
          <a href="${ctx}/app/notificaciones" class="${chipBase += ' ' += (empty tipo ? chipOn : chipOff)}">Todas (${conteo.TODAS + 0})</a>
          <a href="${ctx}/app/notificaciones?tipo=TRAMITE" class="${chipBase += ' ' += (tipo == 'TRAMITE' ? chipOn : chipOff)}">
            <span class="material-symbols-outlined text-[16px]">folder</span> Trámites (${conteo.TRAMITE + 0})
          </a>
          <a href="${ctx}/app/notificaciones?tipo=CITA" class="${chipBase += ' ' += (tipo == 'CITA' ? chipOn : chipOff)}">
            <span class="material-symbols-outlined text-[16px]">calendar_today</span> Citas (${conteo.CITA + 0})
          </a>
          <a href="${ctx}/app/notificaciones?tipo=SEGURIDAD" class="${chipBase += ' ' += (tipo == 'SEGURIDAD' ? chipOn : chipOff)}">
            <span class="material-symbols-outlined text-[16px]">shield</span> Seguridad (${conteo.SEGURIDAD + 0})
          </a>
        </div>

        <c:choose>
          <c:when test="${empty notificaciones}">
            <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-8 text-center">
              <span class="material-symbols-outlined text-[40px] text-outline">${empty tipo ? 'notifications_off' : 'search_off'}</span>
              <p class="text-title-sm text-primary mt-2">${empty tipo ? 'No tienes notificaciones' : 'Sin notificaciones en esta categoría'}</p>
              <p class="text-body-sm text-on-surface-variant mt-1">
                ${empty tipo ? 'Aquí verás los avisos de tus trámites, citas y seguridad de tu cuenta.' : 'Selecciona otro filtro para ver los avisos registrados.'}
              </p>
            </div>
          </c:when>
          <c:otherwise>
            <ul class="space-y-3">
              <c:forEach var="n" items="${notificaciones}">
                <li class="rounded-2xl border p-4 sm:p-5 flex gap-4 ${n.leida ? 'bg-surface-container-lowest border-surface-container' : 'bg-[#F2F6FF] border-[#C9D8F5]'}">
                  <div class="relative shrink-0 self-start">
                    <span class="w-11 h-11 rounded-xl flex items-center justify-center
                          ${n.tipo == 'SEGURIDAD' ? 'bg-[#FFF4E5] text-[#8A5A00]' : (n.tipo == 'CITA' ? 'bg-tertiary-fixed-dim/25 text-[#00606F]' : 'bg-primary-fixed text-primary')}">
                      <span class="material-symbols-outlined">${n.icono}</span>
                    </span>
                    <c:if test="${not n.leida}">
                      <span class="absolute -top-1 -right-1 w-3 h-3 rounded-full bg-error ring-2 ring-white" title="Sin leer"></span>
                    </c:if>
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="flex flex-wrap items-start justify-between gap-x-3 gap-y-1">
                      <h2 class="text-title-sm ${n.leida ? 'text-on-surface' : 'text-primary font-bold'}"><c:out value="${n.titulo}"/></h2>
                      <span class="text-caption text-secondary whitespace-nowrap">${n.cuandoTexto}</span>
                    </div>
                    <p class="text-body-sm text-on-surface-variant mt-1"><c:out value="${n.mensaje}"/></p>

                    <div class="flex flex-wrap items-center gap-x-4 gap-y-2 mt-3">
                      <c:if test="${n.conEnlace}">
                        <form method="post" action="${ctx}/app/notificaciones">
                          <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                          <input type="hidden" name="accion" value="abrir">
                          <input type="hidden" name="id" value="${n.idNotificacion}">
                          <button type="submit" class="text-label-sm text-primary hover:underline inline-flex items-center gap-1">
                            ${n.textoEnlace} <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
                          </button>
                        </form>
                      </c:if>
                      <c:if test="${not n.leida}">
                        <form method="post" action="${ctx}/app/notificaciones">
                          <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                          <input type="hidden" name="accion" value="leer">
                          <input type="hidden" name="id" value="${n.idNotificacion}">
                          <c:if test="${not empty tipo}"><input type="hidden" name="tipo" value="${tipo}"></c:if>
                          <button type="submit" class="text-label-sm text-secondary hover:text-primary inline-flex items-center gap-1">
                            <span class="material-symbols-outlined text-[16px]">check_circle</span> Marcar leída
                          </button>
                        </form>
                      </c:if>
                      <form method="post" action="${ctx}/app/notificaciones" class="ml-auto">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="archivar">
                        <input type="hidden" name="id" value="${n.idNotificacion}">
                        <c:if test="${not empty tipo}"><input type="hidden" name="tipo" value="${tipo}"></c:if>
                        <button type="submit" class="text-label-sm text-secondary hover:text-error inline-flex items-center gap-1" title="Quitar de la bandeja">
                          <span class="material-symbols-outlined text-[16px]">archive</span> Archivar
                        </button>
                      </form>
                    </div>
                  </div>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </c:when>

      <%-- ================= HISTORIAL (BITÁCORA) ================= --%>
      <c:otherwise>
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 sm:p-6">
          <div class="flex flex-wrap items-center justify-between gap-2 mb-5">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">timeline</span>
              <h2 class="text-title-lg font-bold text-primary">Bitácora ciudadana</h2>
            </div>
            <span class="text-caption font-bold uppercase rounded-full bg-surface-container text-on-surface-variant px-2.5 py-1 inline-flex items-center gap-1">
              <span class="material-symbols-outlined text-[14px]">lock</span> Registro inmutable
            </span>
          </div>

          <c:choose>
            <c:when test="${empty bitacora}">
              <p class="text-body-sm text-on-surface-variant text-center py-6">Todavía no hay actividad registrada.</p>
            </c:when>
            <c:otherwise>
              <ol class="relative border-l-2 border-surface-container ml-3 space-y-6">
                <c:forEach var="b" items="${bitacora}">
                  <li class="pl-6 relative">
                    <span class="absolute -left-[13px] top-0 w-6 h-6 rounded-full bg-primary-fixed text-primary flex items-center justify-center ring-4 ring-surface-container-lowest">
                      <span class="material-symbols-outlined text-[14px]">${b.icono}</span>
                    </span>
                    <p class="text-caption text-secondary">${b.fechaTexto}</p>
                    <p class="text-label-md text-on-surface mt-0.5">${b.accionTexto}</p>
                    <c:if test="${not empty b.descripcion}">
                      <p class="text-body-sm text-on-surface-variant mt-0.5"><c:out value="${b.descripcion}"/></p>
                    </c:if>
                  </li>
                </c:forEach>
              </ol>
            </c:otherwise>
          </c:choose>
        </section>

        <div class="flex items-start gap-3 rounded-xl bg-primary text-white p-4">
          <span class="material-symbols-outlined text-tertiary-fixed">fingerprint</span>
          <div>
            <p class="text-label-md">Auditoría de actividad ciudadana</p>
            <p class="text-body-sm text-primary-fixed mt-0.5">
              Cada inicio de sesión, envío de solicitud, archivo adjunto y cita queda registrado con fecha y hora. Este registro no se puede modificar.
            </p>
          </div>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
