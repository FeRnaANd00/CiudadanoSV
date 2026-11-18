<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | <c:out value="${solicitud.folio}"/></title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="solicitudes"/>
<c:set var="yo" value="${sessionScope.usuario}"/>
<c:set var="puedeResolver" value="${yo.puedeResolver}"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-6xl px-4 sm:px-6 py-6 space-y-5">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>

    <a href="${ctx}/admin/solicitudes" class="inline-flex items-center gap-1 text-label-md text-primary hover:underline">
      <span class="material-symbols-outlined text-[18px]">arrow_back</span> Bandeja de expedientes
    </a>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>

    <%-- Encabezado --%>
    <section class="relative overflow-hidden rounded-2xl bg-primary text-on-primary p-6">
      <span class="material-symbols-outlined absolute -right-6 -bottom-8 text-[150px] opacity-10 pointer-events-none select-none" aria-hidden="true">gavel</span>
      <div class="relative flex flex-wrap items-center justify-between gap-3">
        <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold">
          <c:out value="${solicitud.institucionSiglas}"/> &bull; <c:out value="${solicitud.tramiteCodigo}"/>
        </p>
        <c:set var="est" value="${solicitud.estado}"/><c:set var="estTexto" value="${solicitud.estadoTexto}"/>
        <span class="bg-white rounded-full"><%@include file="/WEB-INF/vistas/includes/badge_estado.jspf"%></span>
      </div>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold mt-2 relative"><c:out value="${solicitud.tramiteNombre}"/></h1>
      <div class="mt-4 relative flex flex-wrap items-end gap-x-6 gap-y-3">
        <div>
          <p class="text-caption uppercase text-primary-fixed">Folio</p>
          <p class="text-title-lg font-bold tracking-wide select-all"><c:out value="${solicitud.folio}"/></p>
        </div>
        <div>
          <p class="text-caption uppercase text-primary-fixed">Ingresó</p>
          <p class="text-label-md">${solicitud.fechaCreacionTexto}<c:if test="${solicitud.abierta}"> &bull; ${solicitud.tiempoEnColaTexto} en cola</c:if></p>
        </div>
        <div>
          <p class="text-caption uppercase text-primary-fixed">Sede</p>
          <p class="text-label-md"><c:out value="${empty solicitud.sedeNombre ? 'En línea' : solicitud.sedeNombre}"/></p>
        </div>
        <div>
          <p class="text-caption uppercase text-primary-fixed">Revisor</p>
          <p class="text-label-md"><c:out value="${empty solicitud.revisorNombre ? 'Sin asignar' : solicitud.revisorNombre}"/></p>
        </div>
        <c:if test="${puedeResolver and solicitud.abierta and solicitud.idRevisor != yo.idUsuario}">
          <form method="post" action="${ctx}/admin/solicitud" class="ml-auto">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="folio" value="${solicitud.folio}">
            <input type="hidden" name="accion" value="asignarme">
            <button type="submit" class="boton-borde"><span class="material-symbols-outlined text-[18px]">assignment_ind</span> Asignármela</button>
          </form>
        </c:if>
      </div>
    </section>

    <div class="grid gap-5 lg:grid-cols-[1.3fr_1fr]">
      <%-- ===== Columna izquierda: información ===== --%>
      <div class="space-y-5">
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
          <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">person</span> Ciudadano</h2>
          <dl class="mt-3 grid gap-3 sm:grid-cols-2">
            <div><dt class="text-caption uppercase text-secondary">Nombre</dt><dd class="text-label-md"><c:out value="${solicitud.ciudadanoNombre}"/></dd></div>
            <div><dt class="text-caption uppercase text-secondary">DUI</dt><dd class="text-label-md"><c:out value="${solicitud.ciudadanoDui}"/></dd></div>
            <div><dt class="text-caption uppercase text-secondary">Correo</dt><dd class="text-label-md break-all"><c:out value="${solicitud.ciudadanoCorreo}"/></dd></div>
            <div><dt class="text-caption uppercase text-secondary">Teléfono</dt><dd class="text-label-md"><c:out value="${empty solicitud.ciudadanoTelefono ? '—' : solicitud.ciudadanoTelefono}"/></dd></div>
            <div><dt class="text-caption uppercase text-secondary">Fecha de nacimiento</dt><dd class="text-label-md">${empty solicitud.ciudadanoFechaNacimiento ? '—' : solicitud.ciudadanoFechaNacimiento}</dd></div>
            <div class="sm:col-span-2"><dt class="text-caption uppercase text-secondary">Residencia</dt><dd class="text-label-md"><c:out value="${empty solicitud.ciudadanoResidencia ? '—' : solicitud.ciudadanoResidencia}"/></dd></div>
          </dl>
        </section>

        <c:if test="${not empty partida or not empty municipal or not empty dui}">
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">assignment</span> Datos de la solicitud</h2>
            <dl class="mt-3 grid gap-3 sm:grid-cols-2">
              <c:if test="${not empty partida}">
                <div><dt class="text-caption uppercase text-secondary">Persona inscrita</dt><dd class="text-label-md"><c:out value="${partida.nombreInscrito}"/></dd></div>
                <div><dt class="text-caption uppercase text-secondary">Relación</dt><dd class="text-label-md">${partida.relacionTexto}</dd></div>
                <div><dt class="text-caption uppercase text-secondary">Fecha de nacimiento</dt><dd class="text-label-md">${partida.fechaNacimientoTexto}</dd></div>
                <div><dt class="text-caption uppercase text-secondary">Municipio de inscripción</dt><dd class="text-label-md"><c:out value="${partida.municipioNombre}"/></dd></div>
                <div><dt class="text-caption uppercase text-secondary">Uso</dt><dd class="text-label-md">${partida.usoTexto}</dd></div>
              </c:if>
              <c:if test="${not empty municipal}">
                <div><dt class="text-caption uppercase text-secondary">Municipio</dt><dd class="text-label-md"><c:out value="${municipal.municipioNombre}"/></dd></div>
                <div><dt class="text-caption uppercase text-secondary">Dirección declarada</dt><dd class="text-label-md"><c:out value="${municipal.domicilio}"/></dd></div>
              </c:if>
              <c:if test="${not empty dui}">
                <div><dt class="text-caption uppercase text-secondary">Motivo</dt><dd class="text-label-md">${dui.motivoTexto}</dd></div>
                <div><dt class="text-caption uppercase text-secondary">Residencia</dt><dd class="text-label-md"><c:out value="${dui.municipioNombre}"/>, <c:out value="${dui.departamentoNombre}"/></dd></div>
              </c:if>
            </dl>
          </section>
        </c:if>

        <%-- Documentos y cotejo --%>
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
          <div class="flex items-center justify-between gap-2">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">fact_check</span> Documentos para cotejo</h2>
            <span class="text-caption text-secondary">${fn:length(documentos)} archivos</span>
          </div>
          <c:choose>
            <c:when test="${empty documentos}">
              <p class="text-body-sm text-on-surface-variant mt-2">Este trámite no tiene documentos adjuntos.</p>
            </c:when>
            <c:otherwise>
              <ul class="mt-3 space-y-2">
                <c:forEach var="d" items="${documentos}">
                  <li class="rounded-xl bg-surface-container-low p-3">
                    <div class="flex items-center gap-3">
                      <span class="material-symbols-outlined ${d.pdf ? 'text-error' : 'text-primary'}">${d.pdf ? 'picture_as_pdf' : 'image'}</span>
                      <div class="min-w-0 flex-1">
                        <p class="text-label-md text-on-surface truncate"><c:out value="${d.nombreArchivo}"/></p>
                        <p class="text-caption text-secondary">${d.tamanoTexto}<c:if test="${not empty d.requisitoNombre}"> &bull; <c:out value="${d.requisitoNombre}"/></c:if></p>
                      </div>
                      <span class="text-caption font-bold shrink-0 ${d.estadoCotejo == 'VALIDO' ? 'text-exito' : (d.estadoCotejo == 'RECHAZADO' ? 'text-error' : 'text-dorado')}">${d.estadoCotejoTexto}</span>
                    </div>
                    <div class="mt-2 flex flex-wrap items-center gap-2">
                      <a href="${ctx}/app/documento?id=${d.idDocumento}" target="_blank" rel="noopener" class="boton-borde !min-h-[34px] !py-1">
                        <span class="material-symbols-outlined text-[16px]">visibility</span> Ver
                      </a>
                      <c:if test="${puedeResolver and solicitud.abierta}">
                        <c:forTokens var="op" delims="," items="VALIDO,RECHAZADO,PENDIENTE">
                          <c:if test="${d.estadoCotejo != op}">
                            <form method="post" action="${ctx}/admin/solicitud">
                              <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                              <input type="hidden" name="folio" value="${solicitud.folio}">
                              <input type="hidden" name="accion" value="cotejo">
                              <input type="hidden" name="idDocumento" value="${d.idDocumento}">
                              <input type="hidden" name="cotejo" value="${op}">
                              <button type="submit" class="boton-borde !min-h-[34px] !py-1 ${op == 'VALIDO' ? '!text-exito' : (op == 'RECHAZADO' ? '!text-error' : '')}">
                                <span class="material-symbols-outlined text-[16px]">${op == 'VALIDO' ? 'check_circle' : (op == 'RECHAZADO' ? 'cancel' : 'undo')}</span>
                                ${op == 'VALIDO' ? 'Válido' : (op == 'RECHAZADO' ? 'Rechazar' : 'Pendiente')}
                              </button>
                            </form>
                          </c:if>
                        </c:forTokens>
                      </c:if>
                    </div>
                  </li>
                </c:forEach>
              </ul>
            </c:otherwise>
          </c:choose>
        </section>

        <%-- Historial completo --%>
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
          <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">timeline</span> Historial del expediente</h2>
          <ol class="mt-4 relative ml-3 border-l-2 border-surface-container space-y-5">
            <c:forEach var="h" items="${historial}">
              <li class="pl-6 relative">
                <span class="absolute -left-[13px] top-0 w-6 h-6 rounded-full flex items-center justify-center ring-4 ring-surface-container-lowest
                      ${h.interno ? 'bg-dorado text-white' : (h.estadoNuevo == 'RECHAZADA' ? 'bg-error text-white' : 'bg-primary text-white')}">
                  <span class="material-symbols-outlined text-[14px]">${h.interno ? 'lock' : 'check'}</span>
                </span>
                <div class="flex flex-wrap items-center gap-2">
                  <p class="text-label-md text-on-surface">
                    <c:choose>
                      <c:when test="${h.interno}">Observación interna</c:when>
                      <c:otherwise>${h.estadoTexto}</c:otherwise>
                    </c:choose>
                  </p>
                  <c:if test="${h.interno}"><span class="text-caption font-bold uppercase text-dorado">Solo funcionarios</span></c:if>
                </div>
                <p class="text-caption text-secondary">${h.fechaTexto}<c:if test="${not empty h.usuarioNombre}"> &bull; <c:out value="${h.usuarioNombre}"/></c:if></p>
                <c:if test="${not empty h.comentario}">
                  <p class="text-body-sm text-on-surface-variant mt-1"><c:out value="${h.comentario}"/></p>
                </c:if>
              </li>
            </c:forEach>
          </ol>
        </section>
      </div>

      <%-- ===== Columna derecha: acciones ===== --%>
      <div class="space-y-5">
        <%-- Resolución / cambio de estado --%>
        <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
          <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">gavel</span> Resolución</h2>
          <c:choose>
            <c:when test="${not puedeResolver}">
              <p class="text-body-sm text-on-surface-variant mt-2">Tu rol (<c:out value="${yo.rolTexto}"/>) puede consultar el expediente, pero no cambiar su estado.</p>
            </c:when>
            <c:when test="${empty solicitud.estadosSiguientes}">
              <p class="text-body-sm text-on-surface-variant mt-2">
                El expediente está cerrado<c:if test="${not empty solicitud.fechaResolucionTexto}"> (resuelto el ${solicitud.fechaResolucionTexto})</c:if>.
              </p>
            </c:when>
            <c:otherwise>
              <form method="post" action="${ctx}/admin/solicitud" class="mt-3 space-y-3">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="folio" value="${solicitud.folio}">
                <input type="hidden" name="accion" value="estado">
                <fieldset class="space-y-2">
                  <legend class="text-label-sm text-on-surface-variant mb-1">Nuevo estado</legend>
                  <c:forEach var="e" items="${solicitud.estadosSiguientes}" varStatus="vs">
                    <label class="opcion-radio flex items-center gap-3 rounded-xl border border-surface-container p-3 cursor-pointer">
                      <input type="radio" name="estado" value="${e}" ${vs.first ? 'checked' : ''} class="accent-[#002869]">
                      <span class="material-symbols-outlined ${e == 'RECHAZADA' ? 'text-error' : 'text-primary'}">
                        ${e == 'EN_REVISION' ? 'manage_search' : e == 'APROBADA' ? 'verified' : e == 'RECHAZADA' ? 'block' : 'task_alt'}
                      </span>
                      <span class="text-label-md">
                        ${e == 'EN_REVISION' ? 'Pasar a revisión' : e == 'APROBADA' ? 'Aprobar' : e == 'RECHAZADA' ? 'Rechazar' : 'Finalizar (documento entregado)'}
                      </span>
                    </label>
                  </c:forEach>
                </fieldset>
                <label class="block">
                  <span class="text-label-sm text-on-surface-variant">Observación para el ciudadano <span class="text-secondary">(obligatoria al rechazar)</span></span>
                  <textarea class="campo mt-1 min-h-[90px]" name="comentario" maxlength="500" placeholder="Se notificará al ciudadano junto con el cambio de estado."></textarea>
                </label>
                <button type="submit" class="boton-primario w-full" onclick="return confirm('¿Confirmas el cambio de estado? Se notificará al ciudadano.');">
                  <span class="material-symbols-outlined text-[18px]">send</span> Guardar y notificar
                </button>
                <c:if test="${solicitud.requiereBiometria and solicitud.estado == 'EN_REVISION'}">
                  <p class="text-caption text-secondary">Este trámite requiere biometría: para aprobarlo primero programa la cita biométrica.</p>
                </c:if>
              </form>
            </c:otherwise>
          </c:choose>
        </section>

        <%-- Cita biométrica --%>
        <c:if test="${solicitud.puedeProgramarCita and (puedeResolver or yo.puedeGestionarCupos)}">
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">fingerprint</span> Programar cita biométrica</h2>
            <p class="text-caption text-secondary mt-1"><c:out value="${solicitud.sedeNombre}"/></p>
            <form method="get" action="${ctx}/admin/solicitud" class="mt-3 flex items-end gap-2">
              <input type="hidden" name="folio" value="${solicitud.folio}">
              <label class="block flex-1">
                <span class="text-label-sm text-on-surface-variant">Fecha</span>
                <input class="campo mt-1" type="text" name="fecha" value="${fechaCita}" placeholder="DD/MM/AAAA" inputmode="numeric" maxlength="10" pattern="\d{2}/\d{2}/\d{4}">
              </label>
              <button type="submit" class="boton-borde">Ver horarios</button>
            </form>
            <c:choose>
              <c:when test="${empty cupos}">
                <p class="text-body-sm text-on-surface-variant mt-3">No hay horarios ese día.</p>
              </c:when>
              <c:otherwise>
                <form method="post" action="${ctx}/admin/solicitud" class="mt-3 space-y-2">
                  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                  <input type="hidden" name="folio" value="${solicitud.folio}">
                  <input type="hidden" name="accion" value="cita">
                  <c:forEach var="cu" items="${cupos}">
                    <label class="opcion-radio flex items-center justify-between gap-3 rounded-xl border border-surface-container p-3 ${cu.lleno or not cu.habilitado ? 'opacity-50' : 'cursor-pointer'}">
                      <span class="flex items-center gap-3">
                        <input type="radio" name="idCupo" value="${cu.idCupo}" required ${cu.lleno or not cu.habilitado ? 'disabled' : ''} class="accent-[#002869]">
                        <span class="text-label-md">${cu.horarioTexto}</span>
                      </span>
                      <span class="text-caption text-secondary">${cu.habilitado ? (cu.lleno ? 'Lleno' : (cu.disponibles += ' libres')) : 'Deshabilitado'}</span>
                    </label>
                  </c:forEach>
                  <button type="submit" class="boton-primario w-full"><span class="material-symbols-outlined text-[18px]">event_available</span> Programar y notificar</button>
                </form>
              </c:otherwise>
            </c:choose>
          </section>
        </c:if>

        <c:if test="${not empty citas}">
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">event</span> Citas del trámite</h2>
            <ul class="mt-3 space-y-2">
              <c:forEach var="ci" items="${citas}">
                <li class="rounded-xl bg-surface-container-low p-3">
                  <div class="flex items-center justify-between gap-2">
                    <span class="text-label-md"><c:out value="${ci.codigo}"/></span>
                    <span class="text-caption font-bold uppercase text-secondary">${ci.estadoTexto}</span>
                  </div>
                  <p class="text-caption text-secondary mt-0.5 first-letter:uppercase">${ci.fechaTexto} &bull; ${ci.horaTexto} &bull; Turno #${ci.numeroTurno}</p>
                </li>
              </c:forEach>
            </ul>
          </section>
        </c:if>

        <%-- Observación interna --%>
        <section class="rounded-2xl bg-[#FFF8EC] border border-[#F1DDB8] p-5">
          <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">lock</span> Observación interna</h2>
          <p class="text-caption text-secondary mt-1">Solo la ven los funcionarios. No se notifica al ciudadano.</p>
          <c:if test="${not empty solicitud.observacionInterna}">
            <p class="text-body-sm text-on-surface mt-2 rounded-lg bg-white/70 p-3"><c:out value="${solicitud.observacionInterna}"/></p>
          </c:if>
          <form method="post" action="${ctx}/admin/solicitud" class="mt-3 space-y-2">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="folio" value="${solicitud.folio}">
            <input type="hidden" name="accion" value="interna">
            <textarea class="campo min-h-[80px]" name="texto" maxlength="500" required placeholder="Ej.: verificar dirección con la alcaldía."></textarea>
            <button type="submit" class="boton-borde w-full"><span class="material-symbols-outlined text-[18px]">note_add</span> Guardar observación</button>
          </form>
        </section>

        <c:if test="${not empty solicitud.observacionCiudadano}">
          <section class="rounded-2xl bg-surface-container-low p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">rate_review</span> Última observación enviada al ciudadano</h2>
            <p class="text-body-sm text-on-surface mt-2">“<c:out value="${solicitud.observacionCiudadano}"/>”</p>
          </section>
        </c:if>
      </div>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
