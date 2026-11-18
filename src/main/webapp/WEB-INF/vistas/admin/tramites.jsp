<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | Servicios y trámites</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="tramites"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-6xl px-4 sm:px-6 py-6 space-y-5">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>

    <section class="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
      <div>
        <p class="text-label-sm uppercase text-secondary">Catálogo de servicios</p>
        <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Servicios y trámites</h1>
        <p class="text-body-sm text-on-surface-variant mt-1">Aranceles, plazos, modalidad y requisitos que ve el ciudadano.</p>
      </div>
      <a href="${ctx}/admin/tramite" class="boton-primario self-start sm:self-auto"><span class="material-symbols-outlined text-[18px]">add_circle</span> Nuevo trámite</a>
    </section>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>

    <form method="get" action="${ctx}/admin/tramites" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-4 grid gap-3 sm:grid-cols-[1.5fr_1fr_1fr_auto] items-end">
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Código o nombre</span>
        <input class="campo mt-1" type="search" name="q" value="<c:out value='${q}'/>" placeholder="RNPN-DUI-01, licencia...">
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Área</span>
        <select class="campo mt-1" name="area">
          <option value="">Todas</option>
          <c:forEach var="a" items="${areas}">
            <option value="${a.idArea}" ${area == a.idArea ? 'selected' : ''}><c:out value="${a.nombre}"/></option>
          </c:forEach>
        </select>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Estado</span>
        <select class="campo mt-1" name="estado">
          <option value="">Todos</option>
          <option value="ACTIVO" ${estado == 'ACTIVO' ? 'selected' : ''}>Activo</option>
          <option value="EN_REVISION" ${estado == 'EN_REVISION' ? 'selected' : ''}>En revisión</option>
          <option value="PROXIMAMENTE" ${estado == 'PROXIMAMENTE' ? 'selected' : ''}>Próximamente</option>
          <option value="INACTIVO" ${estado == 'INACTIVO' ? 'selected' : ''}>Inactivo</option>
        </select>
      </label>
      <button type="submit" class="boton-primario"><span class="material-symbols-outlined text-[18px]">search</span> Filtrar</button>
    </form>

    <p class="text-label-md text-secondary">${fn:length(tramites)} trámites</p>
    <div class="overflow-x-auto rounded-2xl bg-surface-container-lowest border border-surface-container">
      <table class="w-full text-left text-body-sm min-w-[720px]">
        <thead class="bg-surface-container-low text-caption uppercase text-secondary">
          <tr>
            <th class="px-4 py-3">Trámite</th>
            <th class="px-4 py-3">Institución</th>
            <th class="px-4 py-3">Arancel</th>
            <th class="px-4 py-3">Modalidad</th>
            <th class="px-4 py-3">Requisitos</th>
            <th class="px-4 py-3">Estado</th>
            <th class="px-4 py-3"><span class="sr-only">Acciones</span></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-surface-container">
          <c:forEach var="t" items="${tramites}">
            <tr class="hover:bg-surface-container-low">
              <td class="px-4 py-3">
                <p class="text-label-md text-on-surface"><c:out value="${t.nombre}"/></p>
                <p class="text-caption text-secondary"><c:out value="${t.codigo}"/> &bull; <c:out value="${t.areaNombre}"/></p>
              </td>
              <td class="px-4 py-3"><c:out value="${t.institucionSiglas}"/></td>
              <td class="px-4 py-3">${t.gratuito ? 'Gratis' : ('$' += t.arancel)}</td>
              <td class="px-4 py-3">
                <span class="text-caption font-bold uppercase">${t.modalidad}</span>
                <c:if test="${t.requiereCita}"><span class="material-symbols-outlined text-[16px] text-secondary align-middle" title="Requiere cita">event</span></c:if>
                <c:if test="${t.requiereBiometria}"><span class="material-symbols-outlined text-[16px] text-secondary align-middle" title="Requiere biometría">fingerprint</span></c:if>
              </td>
              <td class="px-4 py-3">${t.numRequisitos}</td>
              <td class="px-4 py-3">
                <span class="text-caption font-bold uppercase rounded-full px-2 py-0.5
                      ${t.estado == 'ACTIVO' ? 'bg-[#EAF5EE] text-exito' : t.estado == 'INACTIVO' ? 'bg-error-container text-[#93000a]' : 'bg-dorado/15 text-dorado'}">${t.estadoTexto}</span>
              </td>
              <td class="px-4 py-3 text-right">
                <a href="${ctx}/admin/tramite?id=${t.idTramite}" class="boton-borde !min-h-[34px] !py-1"><span class="material-symbols-outlined text-[16px]">edit</span> Editar</a>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
