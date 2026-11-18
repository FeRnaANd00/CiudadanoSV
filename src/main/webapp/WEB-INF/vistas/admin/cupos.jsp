<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | Citas y cupos</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="cupos"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-6xl px-4 sm:px-6 py-6 space-y-5">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>

    <section>
      <p class="text-label-sm uppercase text-secondary">Gestión de agendas y capacidad</p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Citas y disponibilidad de cupos</h1>
    </section>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>

    <c:choose>
      <c:when test="${empty sedes}">
        <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-8 text-center">
          <span class="material-symbols-outlined text-[40px] text-outline">domain_disabled</span>
          <p class="text-title-sm text-primary mt-2">Tu institución no tiene sedes activas</p>
        </div>
      </c:when>
      <c:otherwise>
        <%-- Selección de sede, trámite y fecha --%>
        <form method="get" action="${ctx}/admin/cupos" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-[1.4fr_1.4fr_1fr_auto] items-end">
          <label class="block">
            <span class="text-label-sm text-on-surface-variant">Sede de atención</span>
            <select class="campo mt-1" name="sede" onchange="this.form.tramite.value='';this.form.submit()">
              <c:forEach var="s" items="${sedes}">
                <option value="${s.idSede}" ${s.idSede == sede.idSede ? 'selected' : ''}><c:if test="${sessionScope.usuario.superAdmin}">${s.institucionSiglas} · </c:if><c:out value="${s.nombre}"/></option>
              </c:forEach>
            </select>
          </label>
          <label class="block">
            <span class="text-label-sm text-on-surface-variant">Trámite o servicio</span>
            <select class="campo mt-1" name="tramite">
              <c:forEach var="t" items="${tramites}">
                <option value="${t.idTramite}" ${t.idTramite == tramite.idTramite ? 'selected' : ''}><c:out value="${t.nombre}"/></option>
              </c:forEach>
            </select>
          </label>
          <label class="block">
            <span class="text-label-sm text-on-surface-variant">Fecha</span>
            <input class="campo mt-1" type="text" name="fecha" value="${fechaTexto}" placeholder="DD/MM/AAAA" inputmode="numeric" maxlength="10" pattern="\d{2}/\d{2}/\d{4}">
          </label>
          <button type="submit" class="boton-primario"><span class="material-symbols-outlined text-[18px]">search</span> Ver</button>
        </form>

        <%-- Próximos días hábiles --%>
        <section class="grid grid-cols-5 gap-2">
          <c:forEach var="d" items="${semana}">
            <a href="${ctx}/admin/cupos?sede=${sede.idSede}&tramite=${tramite.idTramite}&fecha=${d.fechaTexto}"
               class="rounded-xl border p-2 sm:p-3 text-center ${d.fechaTexto == fechaTexto ? 'bg-primary border-primary text-white' : 'bg-surface-container-lowest border-surface-container hover:border-primary'}">
              <span class="block text-caption uppercase ${d.fechaTexto == fechaTexto ? 'text-primary-fixed' : 'text-secondary'}">${d.hoy ? 'Hoy' : d.diaSemana}</span>
              <span class="block text-title-lg font-bold">${d.diaNumero}</span>
              <span class="block text-caption ${d.fechaTexto == fechaTexto ? 'text-primary-fixed' : 'text-secondary'}">${d.citas} cit.</span>
            </a>
          </c:forEach>
        </section>

        <div class="grid gap-5 lg:grid-cols-[1.2fr_1fr]">
          <%-- ===== Bloques del día ===== --%>
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div>
                <h2 class="text-title-lg font-bold text-primary">Bloques del ${fechaTexto}</h2>
                <p class="text-caption text-secondary"><c:out value="${tramite.nombre}"/></p>
              </div>
              <div class="flex items-center gap-1">
                <a class="boton-borde !px-2" href="${ctx}/admin/cupos?sede=${sede.idSede}&tramite=${tramite.idTramite}&fecha=${fechaAnterior}" title="Día anterior"><span class="material-symbols-outlined text-[18px]">chevron_left</span></a>
                <a class="boton-borde !px-2" href="${ctx}/admin/cupos?sede=${sede.idSede}&tramite=${tramite.idTramite}&fecha=${fechaSiguiente}" title="Día siguiente"><span class="material-symbols-outlined text-[18px]">chevron_right</span></a>
              </div>
            </div>

            <c:choose>
              <c:when test="${empty tramite}">
                <p class="text-body-sm text-on-surface-variant mt-4">Esta sede no atiende trámites con cita.</p>
              </c:when>
              <c:otherwise>
                <c:if test="${not empty bloques}">
                  <div class="mt-4 grid grid-cols-3 gap-2 text-center">
                    <div class="rounded-xl bg-surface-container-low p-2"><p class="text-caption text-secondary">Capacidad</p><p class="text-title-lg font-bold text-primary">${capacidadDia}</p></div>
                    <div class="rounded-xl bg-surface-container-low p-2"><p class="text-caption text-secondary">Ocupados</p><p class="text-title-lg font-bold text-primary">${ocupadosDia}</p></div>
                    <div class="rounded-xl bg-surface-container-low p-2"><p class="text-caption text-secondary">Ocupación</p><p class="text-title-lg font-bold ${ocupacionDia >= 90 ? 'text-error' : 'text-primary'}">${ocupacionDia}%</p></div>
                  </div>
                </c:if>

                <c:choose>
                  <c:when test="${empty bloques}">
                    <div class="mt-4 rounded-xl bg-surface-container-low p-4 text-center">
                      <p class="text-body-sm text-on-surface-variant">Este día no tiene bloques horarios.</p>
                      <c:if test="${not esPasado and diaHabil}">
                        <form method="post" action="${ctx}/admin/cupos" class="mt-3">
                          <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                          <input type="hidden" name="accion" value="generar">
                          <input type="hidden" name="sede" value="${sede.idSede}"><input type="hidden" name="tramite" value="${tramite.idTramite}"><input type="hidden" name="fecha" value="${fechaTexto}">
                          <button class="boton-primario"><span class="material-symbols-outlined text-[18px]">auto_awesome</span> Generar 4 bloques de 10 turnos</button>
                        </form>
                      </c:if>
                    </div>
                  </c:when>
                  <c:otherwise>
                    <ul class="mt-4 space-y-3">
                      <c:forEach var="b" items="${bloques}">
                        <li class="rounded-xl border border-surface-container p-3 ${b.habilitado ? '' : 'bg-surface-container-low opacity-80'}">
                          <div class="flex flex-wrap items-center justify-between gap-2">
                            <span class="text-label-md flex items-center gap-1"><span class="material-symbols-outlined text-[18px] text-primary">schedule</span>${b.horarioTexto}</span>
                            <span class="text-caption font-bold ${not b.habilitado ? 'text-secondary' : (b.lleno ? 'text-error' : 'text-exito')}">
                              ${not b.habilitado ? 'Deshabilitado' : (b.lleno ? 'Lleno' : (b.disponibles += ' libres'))} &bull; ${b.ocupados}/${b.capacidad}
                            </span>
                          </div>
                          <div class="mt-2 h-2 rounded-full bg-surface-container overflow-hidden">
                            <div class="h-full ${b.porcentajeOcupado >= 90 ? 'bg-error' : 'bg-primary'}" style="width:${b.porcentajeOcupado}%"></div>
                          </div>
                          <c:if test="${not esPasado}">
                            <div class="mt-3 flex flex-wrap items-center gap-2">
                              <form method="post" action="${ctx}/admin/cupos" class="flex items-center gap-1">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="capacidad"><input type="hidden" name="idCupo" value="${b.idCupo}">
                                <input type="hidden" name="sede" value="${sede.idSede}"><input type="hidden" name="tramite" value="${tramite.idTramite}"><input type="hidden" name="fecha" value="${fechaTexto}">
                                <label class="sr-only" for="cap${b.idCupo}">Capacidad</label>
                                <input id="cap${b.idCupo}" class="campo !min-h-[34px] !py-1 w-20" type="number" name="capacidad" min="${b.ocupados > 0 ? b.ocupados : 1}" max="${capacidadMaxima}" value="${b.capacidad}">
                                <button class="boton-borde !min-h-[34px] !py-1" title="Guardar capacidad"><span class="material-symbols-outlined text-[16px]">save</span></button>
                              </form>
                              <form method="post" action="${ctx}/admin/cupos">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="${b.habilitado ? 'deshabilitar' : 'habilitar'}"><input type="hidden" name="idCupo" value="${b.idCupo}">
                                <input type="hidden" name="sede" value="${sede.idSede}"><input type="hidden" name="tramite" value="${tramite.idTramite}"><input type="hidden" name="fecha" value="${fechaTexto}">
                                <button class="boton-borde !min-h-[34px] !py-1"><span class="material-symbols-outlined text-[16px]">${b.habilitado ? 'block' : 'check_circle'}</span> ${b.habilitado ? 'Deshabilitar' : 'Habilitar'}</button>
                              </form>
                              <c:if test="${b.ocupados == 0}">
                                <form method="post" action="${ctx}/admin/cupos" onsubmit="return confirm('¿Eliminar este bloque?');">
                                  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                  <input type="hidden" name="accion" value="eliminar"><input type="hidden" name="idCupo" value="${b.idCupo}">
                                  <input type="hidden" name="sede" value="${sede.idSede}"><input type="hidden" name="tramite" value="${tramite.idTramite}"><input type="hidden" name="fecha" value="${fechaTexto}">
                                  <button class="boton-borde !min-h-[34px] !py-1 !text-error"><span class="material-symbols-outlined text-[16px]">delete</span> Eliminar</button>
                                </form>
                              </c:if>
                            </div>
                          </c:if>
                        </li>
                      </c:forEach>
                    </ul>
                    <c:if test="${not esPasado}">
                      <form method="post" action="${ctx}/admin/cupos" class="mt-3"
                            onsubmit="return confirm('${diaBloqueado ? '¿Habilitar todos los bloques del día?' : '¿Bloquear el día (feriado o cierre)? Las citas ya agendadas se mantienen.'}');">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="${diaBloqueado ? 'habilitarDia' : 'bloquearDia'}">
                        <input type="hidden" name="sede" value="${sede.idSede}"><input type="hidden" name="tramite" value="${tramite.idTramite}"><input type="hidden" name="fecha" value="${fechaTexto}">
                        <button class="boton-borde w-full ${diaBloqueado ? '' : '!text-error'}">
                          <span class="material-symbols-outlined text-[18px]">${diaBloqueado ? 'event_available' : 'event_busy'}</span>
                          ${diaBloqueado ? 'Habilitar el día' : 'Bloquear fecha / día festivo'}
                        </button>
                      </form>
                    </c:if>
                  </c:otherwise>
                </c:choose>

                <%-- Nuevo bloque --%>
                <c:if test="${not esPasado}">
                  <form method="post" action="${ctx}/admin/cupos" class="mt-4 rounded-xl bg-surface-container-low p-3 grid grid-cols-2 sm:grid-cols-[1fr_1fr_1fr_auto] gap-2 items-end">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="crear">
                    <input type="hidden" name="sede" value="${sede.idSede}"><input type="hidden" name="tramite" value="${tramite.idTramite}"><input type="hidden" name="fecha" value="${fechaTexto}">
                    <label class="block"><span class="text-caption text-on-surface-variant">Inicio</span><input class="campo mt-1" type="time" name="inicio" required value="15:00"></label>
                    <label class="block"><span class="text-caption text-on-surface-variant">Fin</span><input class="campo mt-1" type="time" name="fin" required value="16:00"></label>
                    <label class="block"><span class="text-caption text-on-surface-variant">Turnos</span><input class="campo mt-1" type="number" name="capacidad" min="1" max="${capacidadMaxima}" required value="10"></label>
                    <button class="boton-primario"><span class="material-symbols-outlined text-[18px]">add</span> Agregar</button>
                  </form>
                </c:if>
              </c:otherwise>
            </c:choose>
          </section>

          <%-- ===== Agenda del día ===== --%>
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <div class="flex items-center justify-between gap-2">
              <h2 class="text-title-lg font-bold text-primary flex items-center gap-2"><span class="material-symbols-outlined">view_timeline</span> Agenda de la sede</h2>
              <span class="text-caption text-secondary">${fn:length(agenda)} citas</span>
            </div>
            <c:choose>
              <c:when test="${empty agenda}">
                <p class="text-body-sm text-on-surface-variant mt-4">No hay citas el ${fechaTexto}.</p>
              </c:when>
              <c:otherwise>
                <ul class="mt-4 space-y-2">
                  <c:forEach var="ci" items="${agenda}">
                    <li class="rounded-xl bg-surface-container-low p-3">
                      <div class="flex flex-wrap items-center justify-between gap-2">
                        <span class="text-label-md">${ci.horaTexto} &bull; Turno #${ci.numeroTurno}</span>
                        <span class="text-caption font-bold uppercase rounded-full px-2 py-0.5
                              ${ci.estado == 'ATENDIDA' ? 'bg-[#EAF5EE] text-exito' : ci.estado == 'PROGRAMADA' ? 'bg-primary-fixed text-primary' : ci.estado == 'CANCELADA' ? 'bg-surface-container text-on-surface-variant' : 'bg-error-container text-[#93000a]'}">
                          ${ci.estado == 'PROGRAMADA' ? 'Programada' : ci.estadoTexto}
                        </span>
                      </div>
                      <p class="text-body-sm text-on-surface mt-1"><c:out value="${ci.ciudadanoNombre}"/> <span class="text-secondary">&bull; DUI <c:out value="${ci.ciudadanoDui}"/></span></p>
                      <p class="text-caption text-secondary"><c:out value="${ci.tramiteNombre}"/> &bull; <c:out value="${ci.codigo}"/>
                        <c:if test="${not empty ci.folioSolicitud}"> &bull; <a class="text-primary hover:underline" href="${ctx}/admin/solicitud?folio=${ci.folioSolicitud}"><c:out value="${ci.folioSolicitud}"/></a></c:if>
                      </p>
                      <c:if test="${ci.atendible}">
                        <div class="mt-2 flex gap-2">
                          <c:forTokens var="r" delims="," items="ATENDIDA,NO_ASISTIO">
                            <form method="post" action="${ctx}/admin/cupos">
                              <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                              <input type="hidden" name="accion" value="atender"><input type="hidden" name="idCita" value="${ci.idCita}"><input type="hidden" name="resultado" value="${r}">
                              <input type="hidden" name="sede" value="${sede.idSede}"><input type="hidden" name="tramite" value="${tramite.idTramite}"><input type="hidden" name="fecha" value="${fechaTexto}">
                              <button class="boton-borde !min-h-[32px] !py-1 ${r == 'ATENDIDA' ? '!text-exito' : '!text-error'}">
                                <span class="material-symbols-outlined text-[16px]">${r == 'ATENDIDA' ? 'how_to_reg' : 'person_off'}</span> ${r == 'ATENDIDA' ? 'Atendida' : 'No asistió'}
                              </button>
                            </form>
                          </c:forTokens>
                        </div>
                      </c:if>
                    </li>
                  </c:forEach>
                </ul>
              </c:otherwise>
            </c:choose>
          </section>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
