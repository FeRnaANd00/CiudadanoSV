<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:set var="motivoActual" value="${not empty param.motivo ? param.motivo : motivoSugerido}"/>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | <c:out value="${tramite.nombre}"/></title>
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
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1"><c:out value="${tramite.nombre}"/></h1>
      <p class="text-body-sm text-on-surface-variant mt-1">
        Completa la información para programar la verificación y entrega.
        Arancel: <strong>$<fmt:formatNumber value="${tramite.arancel}" minFractionDigits="2" maxFractionDigits="2"/></strong>
        &bull; Plazo: <strong>${tramite.plazoHoras} h hábiles</strong>.
      </p>
    </section>

    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span>
        <div>
          <p class="text-label-md"><c:out value="${error}"/></p>
          <c:if test="${not empty requisitosArchivo}">
            <p class="text-body-sm mt-1">Por seguridad, el navegador no conserva los archivos: vuelve a seleccionarlos.</p>
          </c:if>
        </div>
      </div>
    </c:if>

    <%-- Requisitos --%>
    <c:if test="${not empty requisitos}">
      <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5">
        <h2 class="text-title-sm text-primary flex items-center gap-2">
          <span class="material-symbols-outlined">fact_check</span> Requisitos
        </h2>
        <ul class="mt-3 space-y-2">
          <c:forEach var="r" items="${requisitos}">
            <li class="flex items-start gap-2 text-body-sm">
              <span class="material-symbols-outlined text-[18px] ${r.requiereArchivo ? 'text-primary' : 'text-outline'}">${r.requiereArchivo ? 'upload_file' : 'storefront'}</span>
              <span>
                <strong class="text-on-surface"><c:out value="${r.nombre}"/></strong><c:if test="${not r.obligatorio}"> <span class="text-secondary">(opcional)</span></c:if>
                <span class="block text-on-surface-variant">${r.requiereArchivo ? 'Se adjunta en este formulario.' : 'Se presenta en ventanilla el día de la cita.'}</span>
              </span>
            </li>
          </c:forEach>
        </ul>
      </section>
    </c:if>

    <form method="post" action="${ctx}/app/solicitud/dui" enctype="multipart/form-data" id="formDui"
          class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 sm:p-6 space-y-5">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}">
      <input type="hidden" name="codigo" value="${fn:escapeXml(tramite.codigo)}">

      <%-- Datos del ciudadano (de la sesión) --%>
      <div class="rounded-xl bg-surface-container-low p-4 grid gap-3 sm:grid-cols-2">
        <div>
          <p class="text-caption uppercase text-secondary font-semibold">Nombre completo</p>
          <p class="text-label-md text-on-surface"><c:out value="${sessionScope.usuario.nombreCompleto}"/></p>
        </div>
        <div>
          <p class="text-caption uppercase text-secondary font-semibold">DUI actual</p>
          <p class="text-label-md text-on-surface"><c:out value="${sessionScope.usuario.dui}"/></p>
        </div>
      </div>

      <div>
        <label for="motivo" class="block text-label-md text-on-surface mb-1">Motivo <span class="text-error">*</span></label>
        <select id="motivo" name="motivo" required class="campo">
          <c:forEach var="op" items="${motivos}">
            <option value="${op[0]}" ${motivoActual == op[0] ? 'selected' : ''}>${op[1]}</option>
          </c:forEach>
        </select>
      </div>

      <%-- Departamento -> municipio (el municipio se filtra con JavaScript) --%>
      <div class="grid gap-4 sm:grid-cols-2">
        <div>
          <label for="departamento" class="block text-label-md text-on-surface mb-1">Departamento de residencia <span class="text-error">*</span></label>
          <select id="departamento" class="campo" required>
            <option value="">Selecciona</option>
            <c:set var="depActual" value=""/>
            <c:forEach var="m" items="${municipios}">
              <c:if test="${m.departamento != depActual}">
                <option value="${fn:escapeXml(m.departamento)}"><c:out value="${m.departamento}"/></option>
                <c:set var="depActual" value="${m.departamento}"/>
              </c:if>
            </c:forEach>
          </select>
        </div>
        <div>
          <label for="idMunicipio" class="block text-label-md text-on-surface mb-1">Municipio / distrito <span class="text-error">*</span></label>
          <select id="idMunicipio" name="idMunicipio" required class="campo">
            <option value="">Selecciona primero el departamento</option>
            <c:forEach var="m" items="${municipios}">
              <option value="${m.idMunicipio}" data-departamento="${fn:escapeXml(m.departamento)}"
                      ${param.idMunicipio == (''+=m.idMunicipio) ? 'selected' : ''}><c:out value="${m.nombre}"/></option>
            </c:forEach>
          </select>
        </div>
      </div>

      <div>
        <label for="idSede" class="block text-label-md text-on-surface mb-1">Sede para atención y retiro <span class="text-error">*</span></label>
        <select id="idSede" name="idSede" required class="campo">
          <option value="">Selecciona un Duicentro</option>
          <c:forEach var="s" items="${sedes}">
            <option value="${s.idSede}" ${param.idSede == (''+=s.idSede) ? 'selected' : ''}><c:out value="${s.nombre}"/> &mdash; <c:out value="${s.municipioNombre}"/></option>
          </c:forEach>
        </select>
        <p class="text-caption text-secondary mt-1 flex items-center gap-1">
          <span class="material-symbols-outlined text-[14px]">schedule</span> Lunes a viernes de 8:00 a 16:00 y sábados hasta las 12:00.
        </p>
      </div>

      <%-- Un campo de archivo por cada requisito que lo pide --%>
      <c:forEach var="r" items="${requisitosArchivo}">
        <div class="zona-archivo rounded-xl border-2 border-dashed border-surface-container-highest p-4">
          <label for="archivo_${r.idRequisito}" class="block text-label-md text-on-surface">
            <c:out value="${r.nombre}"/> <c:if test="${r.obligatorio}"><span class="text-error">*</span></c:if>
          </label>
          <c:if test="${not empty r.descripcion}"><p class="text-body-sm text-on-surface-variant mt-0.5"><c:out value="${r.descripcion}"/></p></c:if>
          <input type="file" id="archivo_${r.idRequisito}" name="archivo_${r.idRequisito}" ${r.obligatorio ? 'required' : ''}
                 accept="${fn:contains(r.formatos, 'PDF') ? 'application/pdf,' : ''}${fn:contains(r.formatos, 'JPG') ? 'image/jpeg,' : ''}${fn:contains(r.formatos, 'PNG') ? 'image/png' : ''}"
                 data-max-mb="${r.tamanoMaxMb}"
                 class="archivo mt-3 block w-full text-body-sm text-on-surface-variant file:mr-3 file:rounded-lg file:border-0 file:bg-primary file:px-4 file:py-2 file:text-label-md file:text-on-primary hover:file:bg-[#071F4D]">
          <p class="text-caption text-secondary mt-2">Formatos: <c:out value="${r.formatos}"/> &bull; Máximo ${r.tamanoMaxMb} MB</p>
          <p class="error-archivo text-caption text-error font-semibold mt-1 hidden"></p>
        </div>
      </c:forEach>

      <label class="flex items-start gap-3 rounded-xl bg-surface-container-low p-3 cursor-pointer">
        <input type="checkbox" name="declaracion" required class="w-4 h-4 mt-0.5 accent-primary" ${not empty param.declaracion ? 'checked' : ''}>
        <span class="text-body-sm text-on-surface-variant">
          Declaración jurada: los datos y documentos proporcionados son verdaderos.
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
        Al enviar recibirás un folio. La cita para la toma biométrica se te notificará cuando el revisor apruebe los documentos.
      </p>
    </form>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
<script>
  // Departamento -> solo muestra los municipios de ese departamento
  (function () {
    var dep = document.getElementById('departamento');
    var mun = document.getElementById('idMunicipio');
    var opciones = Array.prototype.slice.call(mun.querySelectorAll('option[data-departamento]'));

    function filtrar(conservar) {
      opciones.forEach(function (o) {
        var visible = o.dataset.departamento === dep.value;
        o.hidden = !visible;
        o.disabled = !visible;
      });
      if (!conservar || (mun.selectedOptions[0] && mun.selectedOptions[0].disabled)) { mun.value = ''; }
      mun.options[0].textContent = dep.value ? 'Selecciona un municipio' : 'Selecciona primero el departamento';
    }
    dep.addEventListener('change', function () { filtrar(false); });

    // Al volver con error: elegir el departamento del municipio que ya estaba seleccionado
    var elegido = mun.selectedOptions[0];
    if (elegido && elegido.dataset.departamento) { dep.value = elegido.dataset.departamento; }
    filtrar(true);
  })();

  // Revisa el tamaño del archivo antes de enviarlo (el servidor lo vuelve a validar)
  (function () {
    document.querySelectorAll('input.archivo').forEach(function (input) {
      input.addEventListener('change', function () {
        var aviso = input.parentElement.querySelector('.error-archivo');
        var maxBytes = parseInt(input.dataset.maxMb, 10) * 1024 * 1024;
        var archivo = input.files[0];
        if (archivo && archivo.size > maxBytes) {
          aviso.textContent = 'El archivo pesa ' + (archivo.size / 1048576).toFixed(1) + ' MB y el máximo es ' + input.dataset.maxMb + ' MB.';
          aviso.classList.remove('hidden');
          input.value = '';
        } else {
          aviso.classList.add('hidden');
        }
      });
    });
  })();
</script>
</body>
</html>
