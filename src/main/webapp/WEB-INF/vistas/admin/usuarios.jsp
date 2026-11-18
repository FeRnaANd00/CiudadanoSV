<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | Gestión de usuarios</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="usuarios"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-6xl px-4 sm:px-6 py-6 space-y-5">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>

    <section class="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
      <div>
        <p class="text-label-sm uppercase text-secondary">Administración y seguridad</p>
        <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Gestión de usuarios y roles</h1>
        <p class="text-body-sm text-on-surface-variant mt-1">Ciudadanos registrados y funcionarios de cada institución.</p>
      </div>
      <a href="${ctx}/admin/usuario" class="boton-primario self-start sm:self-auto"><span class="material-symbols-outlined text-[18px]">person_add</span> Nuevo funcionario</a>
    </section>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>

    <section class="grid grid-cols-2 lg:grid-cols-4 gap-3">
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary">Usuarios activos</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${resumen.ACTIVO + 0}</p>
      </div>
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary">Ciudadanos</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${resumen.CIUDADANO + 0}</p>
      </div>
      <div class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4">
        <p class="text-caption uppercase text-secondary">Funcionarios</p>
        <p class="text-headline-lg font-bold text-primary mt-1">${resumen.REVISOR + resumen.OPERADOR + resumen.SUPER_ADMIN}</p>
      </div>
      <div class="rounded-2xl bg-error-container/60 border border-error-container p-4">
        <p class="text-caption uppercase text-[#93000a]">Suspendidos</p>
        <p class="text-headline-lg font-bold text-[#93000a] mt-1">${resumen.SUSPENDIDO + 0}</p>
      </div>
    </section>

    <form method="get" action="${ctx}/admin/usuarios" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-[1.5fr_1fr_1fr_1fr_auto] items-end">
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Nombre, DUI o correo</span>
        <input class="campo mt-1" type="search" name="q" value="<c:out value='${q}'/>">
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Rol</span>
        <select class="campo mt-1" name="rol">
          <option value="">Todos (${resumen.TOTAL + 0})</option>
          <option value="CIUDADANO" ${rol == 'CIUDADANO' ? 'selected' : ''}>Ciudadanos (${resumen.CIUDADANO + 0})</option>
          <option value="REVISOR" ${rol == 'REVISOR' ? 'selected' : ''}>Revisores (${resumen.REVISOR + 0})</option>
          <option value="OPERADOR" ${rol == 'OPERADOR' ? 'selected' : ''}>Operadores (${resumen.OPERADOR + 0})</option>
          <option value="SUPER_ADMIN" ${rol == 'SUPER_ADMIN' ? 'selected' : ''}>Super admin (${resumen.SUPER_ADMIN + 0})</option>
        </select>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Estado</span>
        <select class="campo mt-1" name="estado">
          <option value="">Todos</option>
          <option value="ACTIVO" ${estado == 'ACTIVO' ? 'selected' : ''}>Activo</option>
          <option value="SUSPENDIDO" ${estado == 'SUSPENDIDO' ? 'selected' : ''}>Suspendido</option>
          <option value="EN_REVISION" ${estado == 'EN_REVISION' ? 'selected' : ''}>En revisión</option>
        </select>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Institución</span>
        <select class="campo mt-1" name="institucion">
          <option value="">Todas</option>
          <c:forEach var="i" items="${instituciones}">
            <option value="${i.idInstitucion}" ${institucion == i.idInstitucion ? 'selected' : ''}><c:out value="${i.siglas}"/></option>
          </c:forEach>
        </select>
      </label>
      <button type="submit" class="boton-primario"><span class="material-symbols-outlined text-[18px]">search</span> Filtrar</button>
    </form>

    <section>
      <p class="text-label-md text-secondary mb-3">${total + 0} cuentas</p>
      <ul class="grid gap-3 md:grid-cols-2">
        <c:forEach var="u" items="${cuentas}">
          <li class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 flex gap-3">
            <span class="w-11 h-11 shrink-0 rounded-full flex items-center justify-center text-label-md font-bold ${u.funcionario ? 'bg-primary text-white' : 'bg-primary-fixed text-primary'}">${u.iniciales}</span>
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <p class="text-label-md text-on-surface truncate"><c:out value="${u.nombreCompleto}"/></p>
                <span class="text-caption font-bold uppercase rounded-full px-2 py-0.5 ${u.estado == 'ACTIVO' ? 'bg-[#EAF5EE] text-exito' : 'bg-error-container text-[#93000a]'}">${u.estadoTexto}</span>
              </div>
              <p class="text-caption uppercase font-bold text-dorado mt-0.5">${u.rolTexto}<c:if test="${not empty u.institucionSiglas}"> &bull; <c:out value="${u.institucionSiglas}"/></c:if></p>
              <p class="text-caption text-secondary mt-1">DUI <c:out value="${u.dui}"/> &bull; <span class="break-all"><c:out value="${u.correo}"/></span></p>
              <c:if test="${not empty u.residenciaTexto}">
                <p class="text-caption text-secondary flex items-center gap-0.5"><span class="material-symbols-outlined text-[14px]">location_on</span><c:out value="${u.residenciaTexto}"/></p>
              </c:if>
              <p class="text-caption text-secondary">Último acceso: ${u.ultimoAccesoTexto}</p>
              <a href="${ctx}/admin/usuario?id=${u.idUsuario}" class="inline-flex items-center gap-1 text-label-sm text-primary hover:underline mt-2">
                <span class="material-symbols-outlined text-[16px]">manage_accounts</span> Ver / administrar
              </a>
            </div>
          </li>
        </c:forEach>
      </ul>
      <c:if test="${paginas > 1}">
        <c:url var="base" value="/admin/usuarios">
          <c:param name="q" value="${q}"/><c:param name="rol" value="${rol}"/>
          <c:param name="estado" value="${estado}"/><c:param name="institucion" value="${institucion}"/>
        </c:url>
        <nav class="flex items-center justify-center gap-2 mt-5" aria-label="Páginas">
          <c:if test="${pagina > 1}"><a class="boton-borde" href="${base}&pagina=${pagina - 1}">Anterior</a></c:if>
          <span class="text-label-md text-secondary px-2">Página ${pagina} de ${paginas}</span>
          <c:if test="${pagina < paginas}"><a class="boton-borde" href="${base}&pagina=${pagina + 1}">Siguiente</a></c:if>
        </nav>
      </c:if>
    </section>

    <%-- Matriz de roles y permisos --%>
    <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
      <h2 class="text-title-lg font-bold text-primary flex items-center gap-2"><span class="material-symbols-outlined">admin_panel_settings</span> Matriz de roles y privilegios</h2>
      <div class="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <c:forEach var="entrada" items="${matriz}">
          <div class="rounded-xl bg-surface-container-low p-3">
            <p class="text-label-md text-primary">${entrada.key == 'CIUDADANO' ? 'Ciudadano' : entrada.key == 'REVISOR' ? 'Revisor técnico' : entrada.key == 'OPERADOR' ? 'Operador' : 'Super administrador'}</p>
            <ul class="mt-2 space-y-1">
              <c:forEach var="p" items="${entrada.value}">
                <li class="text-caption text-on-surface-variant flex gap-1"><span class="material-symbols-outlined text-[14px] text-exito">check</span><c:out value="${p}"/></li>
              </c:forEach>
            </ul>
          </div>
        </c:forEach>
      </div>
    </section>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
