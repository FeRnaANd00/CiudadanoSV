<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | ${tramite.idTramite == 0 ? 'Nuevo trámite' : tramite.codigo}</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>
<c:set var="moduloActivo" value="tramites"/>
<c:set var="nuevo" value="${tramite.idTramite == 0}"/>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-5xl px-4 sm:px-6 py-6 space-y-5">
    <%@include file="/WEB-INF/vistas/includes/admin_nav.jspf"%>

    <a href="${ctx}/admin/tramites" class="inline-flex items-center gap-1 text-label-md text-primary hover:underline">
      <span class="material-symbols-outlined text-[18px]">arrow_back</span> Servicios y trámites
    </a>

    <section>
      <p class="text-label-sm uppercase text-secondary">${nuevo ? 'Alta en el catálogo' : 'Parámetros del trámite'}</p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">
        <c:out value="${nuevo ? 'Nuevo trámite' : tramite.nombre}"/>
      </h1>
      <c:if test="${not nuevo}">
        <p class="text-body-sm text-on-surface-variant mt-1">${tramite.numSolicitudes} solicitudes registradas con este trámite.</p>
      </c:if>
    </section>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>
    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span><p class="text-label-md"><c:out value="${error}"/></p>
      </div>
    </c:if>

    <%-- Datos del trámite --%>
    <form method="post" action="${ctx}/admin/tramite" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 grid gap-4 sm:grid-cols-2">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}">
      <input type="hidden" name="accion" value="guardar">
      <input type="hidden" name="id" value="${tramite.idTramite}">
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Código</span>
        <input class="campo mt-1 uppercase ${nuevo ? '' : 'bg-surface-container-low'}" name="codigo" required maxlength="20" pattern="[A-Za-z0-9]+(-[A-Za-z0-9]+)*"
               value="<c:out value='${tramite.codigo}'/>" placeholder="MINED-04" ${nuevo ? '' : 'readonly'}>
        <c:if test="${not nuevo}"><span class="text-caption text-secondary">El código no se cambia: lo usan los formularios y los enlaces.</span></c:if>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Nombre</span>
        <input class="campo mt-1" name="nombre" required minlength="5" maxlength="120" value="<c:out value='${tramite.nombre}'/>">
      </label>
      <label class="block sm:col-span-2">
        <span class="text-label-sm text-on-surface-variant">Descripción</span>
        <textarea class="campo mt-1 min-h-[70px]" name="descripcion" maxlength="300"><c:out value="${tramite.descripcion}"/></textarea>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Área</span>
        <select class="campo mt-1" name="area" required>
          <c:forEach var="a" items="${areas}">
            <option value="${a.idArea}" ${tramite.idArea == a.idArea ? 'selected' : ''}><c:out value="${a.nombre}"/></option>
          </c:forEach>
        </select>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Institución</span>
        <select class="campo mt-1" name="institucion" required>
          <c:forEach var="i" items="${instituciones}">
            <option value="${i.idInstitucion}" ${tramite.idInstitucion == i.idInstitucion ? 'selected' : ''}><c:out value="${i.siglas}"/> — <c:out value="${i.nombre}"/></option>
          </c:forEach>
        </select>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Arancel (USD)</span>
        <input class="campo mt-1" name="arancel" type="number" min="0" max="9999.99" step="0.01" required value="${tramite.arancel}">
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Plazo (horas hábiles, vacío si no aplica)</span>
        <input class="campo mt-1" name="plazoHoras" type="number" min="1" max="2000" value="${tramite.plazoHoras}">
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Modalidad</span>
        <select class="campo mt-1" name="modalidad">
          <c:forTokens var="m" delims="," items="DIGITAL,PRESENCIAL,MIXTA">
            <option value="${m}" ${tramite.modalidad == m ? 'selected' : ''}>${m == 'DIGITAL' ? 'Digital' : m == 'PRESENCIAL' ? 'Presencial' : 'Mixta'}</option>
          </c:forTokens>
        </select>
      </label>
      <label class="block">
        <span class="text-label-sm text-on-surface-variant">Estado en el catálogo</span>
        <select class="campo mt-1" name="estado">
          <c:forTokens var="e" delims="," items="ACTIVO,EN_REVISION,PROXIMAMENTE,INACTIVO">
            <option value="${e}" ${tramite.estado == e ? 'selected' : ''}>${e == 'ACTIVO' ? 'Activo (se puede solicitar)' : e == 'EN_REVISION' ? 'En revisión' : e == 'PROXIMAMENTE' ? 'Próximamente (visible, sin solicitar)' : 'Inactivo (oculto)'}</option>
          </c:forTokens>
        </select>
      </label>
      <div class="sm:col-span-2 flex flex-wrap gap-x-6 gap-y-2">
        <label class="flex items-center gap-2"><input type="checkbox" name="requiereCita" ${tramite.requiereCita ? 'checked' : ''} class="w-4 h-4 accent-[#002869]"> <span class="text-label-md">Requiere cita presencial</span></label>
        <label class="flex items-center gap-2"><input type="checkbox" name="requiereBiometria" ${tramite.requiereBiometria ? 'checked' : ''} class="w-4 h-4 accent-[#002869]"> <span class="text-label-md">Requiere biometría (cita programada por la institución)</span></label>
      </div>
      <div class="sm:col-span-2">
        <button type="submit" class="boton-primario"><span class="material-symbols-outlined text-[18px]">save</span> ${nuevo ? 'Crear trámite' : 'Guardar cambios'}</button>
        <c:if test="${nuevo}"><p class="text-caption text-secondary mt-2">Después de crearlo podrás agregar sus requisitos.</p></c:if>
      </div>
    </form>

    <%-- Requisitos --%>
    <c:if test="${not nuevo}">
      <section id="requisitos" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 space-y-4">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <h2 class="text-title-lg font-bold text-primary flex items-center gap-2"><span class="material-symbols-outlined">checklist</span> Matriz de requisitos</h2>
          <span class="text-caption text-secondary">${fn:length(requisitos)} requisitos</span>
        </div>

        <c:if test="${empty requisitos}">
          <p class="text-body-sm text-on-surface-variant">Este trámite todavía no tiene requisitos.</p>
        </c:if>

        <c:forEach var="r" items="${requisitos}">
          <details class="rounded-xl border border-surface-container">
            <summary class="cursor-pointer list-none p-3 flex flex-wrap items-center justify-between gap-2">
              <span class="text-label-md flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px] text-primary">${r.requiereArchivo ? 'upload_file' : 'description'}</span>
                <c:out value="${r.nombre}"/>
              </span>
              <span class="text-caption text-secondary">
                ${r.obligatorio ? 'Obligatorio' : 'Opcional'} &bull; ${r.tipoValidacion}<c:if test="${r.requiereArchivo}"> &bull; ${r.formatos}, ${r.tamanoMaxMb} MB</c:if>
              </span>
            </summary>
            <div class="border-t border-surface-container p-3">
              <c:set var="req" value="${r}"/>
              <%@include file="/WEB-INF/vistas/includes/form_requisito.jspf"%>
              <form method="post" action="${ctx}/admin/tramite" class="mt-2" onsubmit="return confirm('¿Eliminar este requisito? Los documentos ya subidos se conservan.');">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="accion" value="eliminarRequisito">
                <input type="hidden" name="id" value="${tramite.idTramite}">
                <input type="hidden" name="idRequisito" value="${r.idRequisito}">
                <button class="text-label-sm text-error hover:underline inline-flex items-center gap-1"><span class="material-symbols-outlined text-[16px]">delete</span> Eliminar requisito</button>
              </form>
            </div>
          </details>
        </c:forEach>

        <details class="rounded-xl border border-dashed border-primary/40" ${empty requisitos ? 'open' : ''}>
          <summary class="cursor-pointer list-none p-3 text-label-md text-primary flex items-center gap-2"><span class="material-symbols-outlined text-[18px]">add</span> Agregar requisito</summary>
          <div class="border-t border-surface-container p-3">
            <c:set var="req" value="${null}"/>
            <%@include file="/WEB-INF/vistas/includes/form_requisito.jspf"%>
          </div>
        </details>

        <c:if test="${not empty tramitesConRequisitos}">
          <form method="post" action="${ctx}/admin/tramite" class="flex flex-wrap items-end gap-2 rounded-xl bg-surface-container-low p-3">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="copiar">
            <input type="hidden" name="id" value="${tramite.idTramite}">
            <label class="block flex-1 min-w-[220px]">
              <span class="text-label-sm text-on-surface-variant">Copiar los requisitos de otro trámite</span>
              <select class="campo mt-1" name="origen">
                <c:forEach var="o" items="${tramitesConRequisitos}">
                  <c:if test="${o.idTramite != tramite.idTramite}">
                    <option value="${o.idTramite}"><c:out value="${o.codigo}"/> — <c:out value="${o.nombre}"/> (${o.numRequisitos})</option>
                  </c:if>
                </c:forEach>
              </select>
            </label>
            <button class="boton-borde"><span class="material-symbols-outlined text-[18px]">content_copy</span> Copiar</button>
          </form>
        </c:if>
      </section>

      <%-- Eliminar trámite --%>
      <section class="rounded-2xl border border-error-container bg-surface-container-lowest p-5">
        <h2 class="text-title-sm text-error flex items-center gap-2"><span class="material-symbols-outlined">delete_forever</span> Eliminar trámite</h2>
        <c:choose>
          <c:when test="${tieneFormulario}">
            <p class="text-caption text-secondary mt-1">Este trámite tiene un formulario programado en el portal, por eso no se elimina. Si ya no se ofrece, cambia su estado a "Inactivo".</p>
          </c:when>
          <c:when test="${usos > 0}">
            <p class="text-caption text-secondary mt-1">No se puede eliminar: tiene ${usos} solicitudes o citas registradas. Cambia su estado a "Inactivo" para ocultarlo.</p>
          </c:when>
          <c:otherwise>
            <p class="text-caption text-secondary mt-1">El trámite nunca se usó. Al eliminarlo también se borran sus requisitos y sus bloques de cupos vacíos. No se puede deshacer.</p>
            <form method="post" action="${ctx}/admin/tramite" class="mt-3"
                  onsubmit="return confirm('¿Eliminar definitivamente el trámite ${tramite.codigo}? Esta acción no se puede deshacer.');">
              <input type="hidden" name="csrf" value="${sessionScope.csrf}">
              <input type="hidden" name="id" value="${tramite.idTramite}">
              <input type="hidden" name="accion" value="eliminar">
              <button class="boton-borde !text-error"><span class="material-symbols-outlined text-[18px]">delete</span> Eliminar trámite</button>
            </form>
          </c:otherwise>
        </c:choose>
      </section>
    </c:if>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
