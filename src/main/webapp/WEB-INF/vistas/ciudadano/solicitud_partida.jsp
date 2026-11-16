<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Partida de nacimiento</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-3xl px-4 sm:px-6 py-6 space-y-5">

    <c:url var="urlFicha" value="/app/tramite"><c:param name="codigo" value="${tramite.codigo}"/></c:url>
    <a href="${urlFicha}" class="inline-flex items-center gap-1 text-label-md text-primary hover:underline">
      <span class="material-symbols-outlined text-[18px]">arrow_back</span> Volver a la ficha del trámite
    </a>

    <section>
      <p class="text-label-sm uppercase text-secondary"><c:out value="${tramite.areaNombre}"/> &bull; <c:out value="${tramite.institucionSiglas}"/></p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Solicitar certificación de partida</h1>
      <p class="text-body-sm text-on-surface-variant mt-1">
        Completa los datos para identificar el asiento de nacimiento.
        Arancel: <strong>$<fmt:formatNumber value="${tramite.arancel}" minFractionDigits="2" maxFractionDigits="2"/></strong>
        &bull; Plazo: <strong>${tramite.plazoHoras} h hábiles</strong>.
      </p>
    </section>

    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span>
        <p class="text-label-md"><c:out value="${error}"/></p>
      </div>
    </c:if>

    <form method="post" action="${ctx}/app/solicitud/partida"
          class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 sm:p-6 space-y-5">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}">

      <%-- Solicitante (usuario en sesión, no editable) --%>
      <div class="rounded-xl bg-surface-container-low p-4 flex items-start gap-3">
        <span class="material-symbols-outlined text-primary">person_pin</span>
        <div class="min-w-0">
          <p class="text-caption uppercase text-secondary font-semibold">Solicitante</p>
          <p class="text-label-md text-on-surface"><c:out value="${sessionScope.usuario.nombreCompleto}"/></p>
          <p class="text-body-sm text-on-surface-variant">DUI <c:out value="${sessionScope.usuario.dui}"/></p>
        </div>
      </div>

      <div class="grid gap-4 sm:grid-cols-2">
        <div class="sm:col-span-2">
          <label for="relacion" class="block text-label-md text-on-surface mb-1">Relación con la persona inscrita <span class="text-error">*</span></label>
          <select id="relacion" name="relacion" required class="campo">
            <option value="">Selecciona una opción</option>
            <c:forEach var="op" items="${relaciones}">
              <option value="${op[0]}" ${param.relacion == op[0] ? 'selected' : ''}>${op[1]}</option>
            </c:forEach>
          </select>
        </div>

        <div class="sm:col-span-2">
          <label for="nombreInscrito" class="block text-label-md text-on-surface mb-1">Nombre completo de la persona inscrita <span class="text-error">*</span></label>
          <input id="nombreInscrito" name="nombreInscrito" type="text" required maxlength="120" class="campo"
                 value="${fn:escapeXml(param.nombreInscrito)}" placeholder="Como aparece en la partida"
                 data-mi-nombre="${fn:escapeXml(sessionScope.usuario.nombreCompleto)}">
        </div>

        <div>
          <div class="flex justify-between items-center mb-1">
            <label for="fechaNacimiento" class="block text-label-md text-on-surface">Fecha de nacimiento <span class="text-error">*</span></label>
            <span class="text-caption text-secondary">DD/MM/AAAA</span>
          </div>
          <%-- Texto con máscara: se escribe a mano. El botón abre un calendario opcional. --%>
          <div class="relative">
            <input id="fechaNacimiento" name="fechaNacimiento" type="text" required inputmode="numeric"
                   maxlength="10" placeholder="18/03/1991" autocomplete="bday" class="campo pr-11"
                   pattern="\d{2}/\d{2}/\d{4}" title="Formato DD/MM/AAAA"
                   value="${fn:escapeXml(param.fechaNacimiento)}">
            <%-- Icono + selector de fecha real transparente encima: al tocar el icono se toca el selector --%>
            <span class="absolute right-1.5 top-1/2 -translate-y-1/2 w-9 h-9 rounded-md flex items-center justify-center text-primary pointer-events-none">
              <span class="material-symbols-outlined text-[20px]">calendar_month</span>
            </span>
            <input type="date" id="calendarioOculto" tabindex="-1" aria-label="Elegir en calendario" min="1900-01-01" max="${hoy}"
                   class="selector-fecha absolute right-1.5 top-1/2 -translate-y-1/2 w-9 h-9 opacity-0 cursor-pointer">
          </div>
        </div>

        <div>
          <label for="idMunicipio" class="block text-label-md text-on-surface mb-1">Municipio de inscripción <span class="text-error">*</span></label>
          <select id="idMunicipio" name="idMunicipio" required class="campo">
            <option value="">Selecciona un municipio</option>
            <c:set var="depActual" value=""/>
            <c:forEach var="m" items="${municipios}">
              <c:if test="${m.departamento != depActual}">
                <c:if test="${not empty depActual}"></optgroup></c:if>
                <optgroup label="${fn:escapeXml(m.departamento)}">
                <c:set var="depActual" value="${m.departamento}"/>
              </c:if>
              <option value="${m.idMunicipio}" ${param.idMunicipio == (''+=m.idMunicipio) ? 'selected' : ''}><c:out value="${m.nombre}"/></option>
            </c:forEach>
            <c:if test="${not empty depActual}"></optgroup></c:if>
          </select>
        </div>

        <div class="sm:col-span-2">
          <label for="uso" class="block text-label-md text-on-surface mb-1">Uso de la certificación <span class="text-error">*</span></label>
          <select id="uso" name="uso" required class="campo">
            <option value="">Selecciona el uso</option>
            <c:forEach var="op" items="${usos}">
              <option value="${op[0]}" ${param.uso == op[0] ? 'selected' : ''}>${op[1]}</option>
            </c:forEach>
          </select>
        </div>
      </div>

      <label class="flex items-start gap-3 rounded-xl bg-surface-container-low p-3 cursor-pointer">
        <input type="checkbox" name="declaracion" required class="w-4 h-4 mt-0.5 accent-primary" ${not empty param.declaracion ? 'checked' : ''}>
        <span class="text-body-sm text-on-surface-variant">
          Declaro que la información proporcionada es verdadera y que estoy autorizado(a) para solicitar esta certificación.
        </span>
      </label>

      <div class="flex flex-col-reverse sm:flex-row sm:justify-end gap-3">
        <a href="${urlFicha}" class="boton-borde">Cancelar</a>
        <button type="submit" class="boton-primario">
          <span class="material-symbols-outlined text-[18px]">send</span> Enviar solicitud
        </button>
      </div>
      <p class="text-caption text-secondary flex items-center gap-1">
        <span class="material-symbols-outlined text-[16px]">info</span>
        Al enviar recibirás un número de folio para dar seguimiento a tu trámite.
      </p>
    </form>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
<script>
  // Si la persona inscrita es el mismo usuario, rellenamos su nombre
  (function () {
    var relacion = document.getElementById('relacion');
    var nombre = document.getElementById('nombreInscrito');
    relacion.addEventListener('change', function () {
      if (relacion.value === 'PERSONA_INSCRITA') {
        nombre.value = nombre.dataset.miNombre;
      } else if (nombre.value === nombre.dataset.miNombre) {
        nombre.value = '';
      }
    });
  })();

  // Fecha DD/MM/AAAA: pone las "/" solas mientras se escribe
  (function () {
    var fecha = document.getElementById('fechaNacimiento');
    var calendario = document.getElementById('calendarioOculto');

    fecha.addEventListener('input', function () {
      var d = fecha.value.replace(/\D/g, '').substring(0, 8);
      if (d.length > 4) { d = d.substring(0, 2) + '/' + d.substring(2, 4) + '/' + d.substring(4); }
      else if (d.length > 2) { d = d.substring(0, 2) + '/' + d.substring(2); }
      fecha.value = d;
    });

    // Calendario opcional: el selector transparente está sobre el icono.
    // En móvil el toque lo abre directo; en PC usamos showPicker() si existe.
    calendario.addEventListener('pointerdown', function () {
      var p = fecha.value.split('/');
      if (p.length === 3 && p[2].length === 4) { calendario.value = p[2] + '-' + p[1] + '-' + p[0]; }
    });
    calendario.addEventListener('click', function () {
      if (calendario.showPicker) { try { calendario.showPicker(); } catch (e) { /* el navegador lo abre solo */ } }
    });
    calendario.addEventListener('change', function () {
      var p = calendario.value.split('-');           // AAAA-MM-DD
      if (p.length === 3) { fecha.value = p[2] + '/' + p[1] + '/' + p[0]; }
    });
  })();
</script>
</body>
</html>
