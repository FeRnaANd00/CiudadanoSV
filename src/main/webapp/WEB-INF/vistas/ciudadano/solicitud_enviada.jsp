<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Solicitud enviada</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-xl px-4 sm:px-6 py-10 space-y-5">

    <section class="rounded-2xl bg-surface-container-lowest border border-surface-container p-6 text-center">
      <span class="w-14 h-14 rounded-full bg-[#EAF5EE] text-exito inline-flex items-center justify-center">
        <span class="material-symbols-outlined icono-relleno text-[32px]">check_circle</span>
      </span>
      <h1 class="text-headline-md font-bold text-primary mt-3">Solicitud enviada</h1>
      <p class="text-body-sm text-on-surface-variant mt-1">Guarda tu número de folio para dar seguimiento.</p>

      <div class="mt-5 rounded-xl bg-primary text-on-primary p-4">
        <p class="text-caption uppercase text-primary-fixed">Folio oficial</p>
        <p id="folio" class="font-display text-[28px] leading-9 font-bold tracking-wide select-all"><c:out value="${solicitud.folio}"/></p>
        <button type="button" onclick="copiarFolio(this)" class="mt-1 inline-flex items-center gap-1 text-label-sm text-primary-fixed hover:text-white">
          <span class="material-symbols-outlined text-[16px]">content_copy</span><span>Copiar folio</span>
        </button>
      </div>

      <dl class="mt-5 text-left divide-y divide-surface-container">
        <div class="flex justify-between gap-3 py-2.5">
          <dt class="text-body-sm text-on-surface-variant">Trámite</dt>
          <dd class="text-label-md text-on-surface text-right"><c:out value="${solicitud.tramiteNombre}"/></dd>
        </div>
        <div class="flex justify-between gap-3 py-2.5">
          <dt class="text-body-sm text-on-surface-variant">Institución</dt>
          <dd class="text-label-md text-on-surface"><c:out value="${solicitud.institucionSiglas}"/></dd>
        </div>
        <div class="flex justify-between gap-3 py-2.5">
          <dt class="text-body-sm text-on-surface-variant">Fecha de envío</dt>
          <dd class="text-label-md text-on-surface">${solicitud.fechaCreacionTexto}</dd>
        </div>
        <div class="flex justify-between gap-3 py-2.5">
          <dt class="text-body-sm text-on-surface-variant">Estado</dt>
          <dd><span class="text-caption font-bold uppercase rounded-full bg-primary-fixed text-primary px-2 py-0.5">${solicitud.estadoTexto}</span></dd>
        </div>
      </dl>
    </section>

    <section class="rounded-2xl bg-surface-container-low p-5">
      <h2 class="text-title-sm text-primary">¿Qué sigue?</h2>
      <ol class="mt-2 space-y-1.5 text-body-sm text-on-surface-variant list-decimal list-inside">
        <li>Un revisor de la institución validará tu solicitud.</li>
        <li>Recibirás una notificación cuando cambie el estado.</li>
        <li>Podrás consultar el avance en "Mis solicitudes".</li>
      </ol>
    </section>

    <div class="flex flex-col sm:flex-row gap-3 justify-center">
      <c:url var="urlDetalle" value="/app/solicitud"><c:param name="folio" value="${solicitud.folio}"/></c:url>
      <a href="${urlDetalle}" class="boton-primario">
        <span class="material-symbols-outlined text-[18px]">timeline</span> Ver seguimiento
      </a>
      <a href="${ctx}/inicio#servicios" class="boton-borde">
        <span class="material-symbols-outlined text-[18px]">apps</span> Solicitar otro servicio
      </a>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
<script>
  // navigator.clipboard solo funciona con HTTPS o en localhost.
  // Desde el teléfono (http://IP-de-la-PC:8080) se usa el método antiguo como respaldo.
  function copiarFolio(boton) {
    var folio = document.getElementById('folio').textContent.trim();
    var texto = boton.children[1];

    function avisar(mensaje) {
      texto.textContent = mensaje;
      setTimeout(function () { texto.textContent = 'Copiar folio'; }, 2500);
    }

    function copiaAntigua() {
      var area = document.createElement('textarea');
      area.value = folio;
      area.setAttribute('readonly', '');
      area.style.position = 'fixed';
      area.style.top = '-1000px';
      document.body.appendChild(area);
      area.focus();
      area.setSelectionRange(0, folio.length);   // iOS necesita selección explícita
      var ok = false;
      try { ok = document.execCommand('copy'); } catch (e) { ok = false; }
      document.body.removeChild(area);
      if (ok) {
        avisar('¡Copiado!');
      } else {
        // Último recurso: dejar el folio seleccionado para copiarlo a mano
        var rango = document.createRange();
        rango.selectNodeContents(document.getElementById('folio'));
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(rango);
        avisar('Mantén presionado para copiar');
      }
    }

    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(folio).then(function () { avisar('¡Copiado!'); }, copiaAntigua);
    } else {
      copiaAntigua();
    }
  }
</script>
</body>
</html>
