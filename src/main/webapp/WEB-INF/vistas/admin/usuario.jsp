<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | ${cuenta.idUsuario == 0 ? 'Nuevo funcionario' : 'Cuenta'}</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="usuarios"/>
<c:set var="nueva" value="${cuenta.idUsuario == 0}"/>
<c:set var="editable" value="${nueva or (cuenta.funcionario and not cuenta.superAdmin)}"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-5xl px-4 sm:px-6 py-6 space-y-5">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>

    <a href="${ctx}/admin/usuarios" class="inline-flex items-center gap-1 text-label-md text-primary hover:underline">
      <span class="material-symbols-outlined text-[18px]">arrow_back</span> Gestión de usuarios
    </a>

    <section class="flex items-center gap-4">
      <span class="w-14 h-14 shrink-0 rounded-full bg-primary text-white flex items-center justify-center text-title-lg font-bold">
        <c:choose><c:when test="${nueva}"><span class="material-symbols-outlined">person_add</span></c:when><c:otherwise>${cuenta.iniciales}</c:otherwise></c:choose>
      </span>
      <div>
        <p class="text-label-sm uppercase text-secondary">${nueva ? 'Alta de funcionario' : cuenta.rolTexto}</p>
        <h1 class="font-display text-[24px] leading-8 sm:text-headline-lg font-bold text-primary"><c:out value="${nueva ? 'Nuevo funcionario' : cuenta.nombreCompleto}"/></h1>
        <c:if test="${not nueva}">
          <p class="text-caption text-secondary">Registrado: ${cuenta.fechaRegistroTexto} &bull; Último acceso: ${cuenta.ultimoAccesoTexto}</p>
        </c:if>
      </div>
    </section>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>
    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span><p class="text-label-md"><c:out value="${error}"/></p>
      </div>
    </c:if>

    <div class="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
      <%-- Datos --%>
      <c:choose>
        <c:when test="${editable}">
          <form method="post" action="${ctx}/admin/usuario" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 grid gap-4 sm:grid-cols-2 self-start">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${cuenta.idUsuario}">
            <label class="block sm:col-span-2">
              <span class="text-label-sm text-on-surface-variant">Nombre completo</span>
              <input class="campo mt-1" name="nombre" required minlength="5" maxlength="120" value="<c:out value='${cuenta.nombreCompleto}'/>">
            </label>
            <label class="block">
              <span class="text-label-sm text-on-surface-variant">DUI</span>
              <input class="campo mt-1 ${nueva ? '' : 'bg-surface-container-low'}" name="dui" required pattern="\d{8}-\d" maxlength="10" placeholder="00000000-0" inputmode="numeric"
                     value="<c:out value='${cuenta.dui}'/>" ${nueva ? '' : 'readonly'}>
            </label>
            <label class="block">
              <span class="text-label-sm text-on-surface-variant">Teléfono</span>
              <input class="campo mt-1" name="telefono" pattern="\d{4}-\d{4}" maxlength="9" placeholder="7000-0000" value="<c:out value='${cuenta.telefono}'/>">
            </label>
            <label class="block sm:col-span-2">
              <span class="text-label-sm text-on-surface-variant">Correo institucional</span>
              <input class="campo mt-1" type="email" name="correo" required maxlength="120" value="<c:out value='${cuenta.correo}'/>">
            </label>
            <label class="block">
              <span class="text-label-sm text-on-surface-variant">Rol</span>
              <select class="campo mt-1" name="rol">
                <option value="REVISOR" ${cuenta.rol == 'REVISOR' ? 'selected' : ''}>Revisor técnico (resuelve solicitudes)</option>
                <option value="OPERADOR" ${cuenta.rol == 'OPERADOR' ? 'selected' : ''}>Operador (cupos y agenda)</option>
              </select>
            </label>
            <label class="block">
              <span class="text-label-sm text-on-surface-variant">Institución</span>
              <select class="campo mt-1" name="institucion" required>
                <option value="">Elige…</option>
                <c:forEach var="i" items="${instituciones}">
                  <option value="${i.idInstitucion}" ${cuenta.idInstitucion == i.idInstitucion ? 'selected' : ''}><c:out value="${i.siglas}"/></option>
                </c:forEach>
              </select>
            </label>
            <label class="block">
              <span class="text-label-sm text-on-surface-variant">Sede (opcional)</span>
              <select class="campo mt-1" name="sede">
                <option value="">Sin sede fija</option>
                <c:forEach var="s" items="${sedes}">
                  <option value="${s.idSede}" ${cuenta.idSede == s.idSede ? 'selected' : ''}>${s.institucionSiglas} · <c:out value="${s.nombre}"/></option>
                </c:forEach>
              </select>
            </label>
            <label class="block">
              <span class="text-label-sm text-on-surface-variant">Cargo</span>
              <input class="campo mt-1" name="cargo" maxlength="100" placeholder="Oficial Registral" value="<c:out value='${cuenta.cargo}'/>">
            </label>
            <c:if test="${nueva}">
              <label class="block sm:col-span-2">
                <span class="text-label-sm text-on-surface-variant">Contraseña inicial</span>
                <input class="campo mt-1" type="text" name="clave" required minlength="8" autocomplete="new-password" placeholder="Mínimo 8, una mayúscula, un número y un símbolo">
              </label>
            </c:if>
            <c:if test="${not nueva}">
              <div class="sm:col-span-2 border-t border-surface-container pt-1"><%@include file="/WEB-INF/vistas/includes/datos_personales.jspf"%></div>
            </c:if>
            <div class="sm:col-span-2">
              <button class="boton-primario"><span class="material-symbols-outlined text-[18px]">save</span> ${nueva ? 'Crear funcionario' : 'Guardar cambios'}</button>
              <p class="text-caption text-secondary mt-2">El revisor y el operador solo ven los trámites, sedes y cupos de su institución.</p>
            </div>
          </form>
        </c:when>
        <c:otherwise>
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 self-start">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">badge</span> Datos de la cuenta</h2>
            <dl class="mt-3 grid gap-3 sm:grid-cols-2">
              <div><dt class="text-caption uppercase text-secondary">DUI</dt><dd class="text-label-md"><c:out value="${cuenta.dui}"/></dd></div>
              <div><dt class="text-caption uppercase text-secondary">Teléfono</dt><dd class="text-label-md"><c:out value="${empty cuenta.telefono ? '—' : cuenta.telefono}"/></dd></div>
              <div class="sm:col-span-2"><dt class="text-caption uppercase text-secondary">Correo</dt><dd class="text-label-md break-all"><c:out value="${cuenta.correo}"/></dd></div>
              <c:if test="${not cuenta.funcionario}">
                <div><dt class="text-caption uppercase text-secondary">Solicitudes</dt><dd class="text-label-md">${cuenta.numSolicitudes}</dd></div>
              </c:if>
            </dl>
            <%@include file="/WEB-INF/vistas/includes/datos_personales.jspf"%>
            <p class="text-caption text-secondary mt-3">
              ${cuenta.superAdmin ? 'La cuenta del super administrador no se edita desde aquí.' : 'Los datos del ciudadano los mantiene él mismo desde su perfil.'}
            </p>
          </section>
        </c:otherwise>
      </c:choose>

      <%-- Seguridad de la cuenta --%>
      <c:if test="${not nueva and not cuenta.superAdmin}">
        <div class="space-y-5">
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">shield_person</span> Estado de la cuenta</h2>
            <p class="text-body-sm mt-2">Actualmente: <strong>${cuenta.estadoTexto}</strong></p>
            <form method="post" action="${ctx}/admin/usuario" class="mt-3"
                  onsubmit="return confirm('${cuenta.estado == 'ACTIVO' ? '¿Suspender la cuenta? No podrá iniciar sesión.' : '¿Reactivar la cuenta?'}');">
              <input type="hidden" name="csrf" value="${sessionScope.csrf}">
              <input type="hidden" name="id" value="${cuenta.idUsuario}">
              <input type="hidden" name="accion" value="${cuenta.estado == 'ACTIVO' ? 'suspender' : 'reactivar'}">
              <button class="boton-borde w-full ${cuenta.estado == 'ACTIVO' ? '!text-error' : '!text-exito'}">
                <span class="material-symbols-outlined text-[18px]">${cuenta.estado == 'ACTIVO' ? 'block' : 'check_circle'}</span>
                ${cuenta.estado == 'ACTIVO' ? 'Suspender cuenta' : 'Reactivar cuenta'}
              </button>
            </form>
          </section>
          <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
            <h2 class="text-title-sm text-primary flex items-center gap-2"><span class="material-symbols-outlined">key</span> Restablecer contraseña</h2>
            <p class="text-caption text-secondary mt-1">Asigna una contraseña temporal y compártela por un medio seguro.</p>
            <form method="post" action="${ctx}/admin/usuario" class="mt-3 space-y-2">
              <input type="hidden" name="csrf" value="${sessionScope.csrf}">
              <input type="hidden" name="id" value="${cuenta.idUsuario}">
              <input type="hidden" name="accion" value="clave">
              <input class="campo" type="text" name="claveTemporal" required minlength="8" autocomplete="new-password" placeholder="Nueva contraseña temporal">
              <button class="boton-borde w-full"><span class="material-symbols-outlined text-[18px]">lock_reset</span> Restablecer</button>
            </form>
          </section>
          <section class="rounded-2xl border border-error-container bg-surface-container-lowest p-5">
            <h2 class="text-title-sm text-error flex items-center gap-2"><span class="material-symbols-outlined">delete_forever</span> Eliminar cuenta</h2>
            <c:choose>
              <c:when test="${usos == 0}">
                <p class="text-caption text-secondary mt-1">Esta cuenta nunca se usó (sin solicitudes, citas ni historial), así que se puede eliminar. No se puede deshacer.</p>
                <form method="post" action="${ctx}/admin/usuario" class="mt-3"
                      onsubmit="return confirm('¿Eliminar definitivamente esta cuenta? Esta acción no se puede deshacer.');">
                  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                  <input type="hidden" name="id" value="${cuenta.idUsuario}">
                  <input type="hidden" name="accion" value="eliminar">
                  <button class="boton-borde w-full !text-error"><span class="material-symbols-outlined text-[18px]">delete</span> Eliminar cuenta</button>
                </form>
              </c:when>
              <c:otherwise>
                <p class="text-caption text-secondary mt-1">No se puede eliminar: la cuenta tiene ${usos} registros (solicitudes, citas o historial) que deben conservarse. Usa "Suspender cuenta".</p>
              </c:otherwise>
            </c:choose>
          </section>
        </div>
      </c:if>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
