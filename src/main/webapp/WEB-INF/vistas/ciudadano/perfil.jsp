<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Mi perfil</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-4xl px-4 sm:px-6 py-6 space-y-5">

    <%-- Tarjeta de identidad --%>
    <section class="relative overflow-hidden rounded-2xl bg-primary text-on-primary p-6">
      <span class="material-symbols-outlined absolute -right-6 -bottom-8 text-[150px] opacity-10 pointer-events-none select-none" aria-hidden="true">badge</span>
      <div class="relative flex items-center gap-4">
        <span class="w-16 h-16 shrink-0 rounded-full bg-white/15 flex items-center justify-center">
          <span class="material-symbols-outlined icono-relleno text-[36px] text-tertiary-fixed">account_circle</span>
        </span>
        <div class="min-w-0">
          <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold">Identidad digital &bull; <c:out value="${sessionScope.usuario.rolTexto}"/></p>
          <h1 class="font-display text-[24px] leading-8 sm:text-headline-lg font-bold"><c:out value="${perfil.nombreCompleto}"/></h1>
          <p class="text-body-sm text-primary-fixed mt-0.5">
            DUI <c:out value="${perfil.dui}"/> &bull; Nivel de verificación ${perfil.nivelVerificacion} &bull; Miembro desde ${fn:substring(perfil.fechaRegistroTexto, 0, 10)}
          </p>
          <c:if test="${not empty perfil.residenciaTexto}">
            <p class="text-body-sm text-primary-fixed mt-0.5 flex items-center gap-1">
              <span class="material-symbols-outlined text-[16px]">location_on</span><c:out value="${perfil.residenciaTexto}"/>
            </p>
          </c:if>
        </div>
      </div>
    </section>

    <%@include file="/WEB-INF/vistas/includes/avisos.jspf"%>

    <div class="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
      <%-- ===== Datos personales ===== --%>
      <form method="post" action="${ctx}/app/perfil" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 grid gap-4 sm:grid-cols-2 self-start">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="accion" value="datos">
        <div class="sm:col-span-2">
          <h2 class="text-title-lg font-bold text-primary flex items-center gap-2"><span class="material-symbols-outlined">contact_page</span> Mis datos</h2>
          <p class="text-caption text-secondary mt-1">El nombre y el DUI son datos registrales y no se cambian desde aquí.</p>
        </div>

        <label class="block sm:col-span-2">
          <span class="text-label-sm text-on-surface-variant">Correo electrónico</span>
          <input class="campo mt-1" type="email" name="correo" required maxlength="120" autocomplete="email" value="<c:out value='${perfil.correo}'/>">
        </label>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Teléfono</span>
          <input class="campo mt-1" id="telefono" name="telefono" inputmode="numeric" maxlength="9" pattern="\d{4}-\d{4}" placeholder="7000-0000" title="Formato 7000-0000" autocomplete="tel"
                 value="<c:out value='${perfil.telefono}'/>">
        </label>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Fecha de nacimiento <span class="text-secondary">(DD/MM/AAAA)</span></span>
          <input class="campo mt-1" id="fechaNacimiento" name="fechaNacimiento" inputmode="numeric" maxlength="10" pattern="\d{2}/\d{2}/\d{4}" placeholder="18/03/1991" title="Formato DD/MM/AAAA" autocomplete="bday"
                 value="${perfil.fechaNacimientoTexto}">
        </label>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Estado civil</span>
          <select class="campo mt-1" name="estadoCivil">
            <option value="">Sin especificar</option>
            <c:forTokens var="e" delims="," items="SOLTERO,CASADO,DIVORCIADO,VIUDO,ACOMPANADO">
              <option value="${e}" ${perfil.estadoCivil == e ? 'selected' : ''}>
                ${e == 'SOLTERO' ? 'Soltero(a)' : e == 'CASADO' ? 'Casado(a)' : e == 'DIVORCIADO' ? 'Divorciado(a)' : e == 'VIUDO' ? 'Viudo(a)' : 'Acompañado(a)'}
              </option>
            </c:forTokens>
          </select>
        </label>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Municipio de residencia</span>
          <select class="campo mt-1" id="idMunicipio" name="idMunicipio">
            <option value="">Sin especificar</option>
            <c:set var="depActual" value=""/>
            <c:forEach var="m" items="${municipios}">
              <c:if test="${m.departamento != depActual}">
                <c:if test="${not empty depActual}"></optgroup></c:if>
                <optgroup label="${fn:escapeXml(m.departamento)}">
                <c:set var="depActual" value="${m.departamento}"/>
              </c:if>
              <option value="${m.idMunicipio}" ${perfil.idMunicipio == m.idMunicipio ? 'selected' : ''}><c:out value="${m.nombre}"/></option>
            </c:forEach>
            <c:if test="${not empty depActual}"></optgroup></c:if>
          </select>
        </label>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Distrito</span>
          <select class="campo mt-1" id="idDistrito" name="idDistrito">
            <option value="" data-municipio="">Sin especificar</option>
            <c:forEach var="d" items="${distritos}">
              <option value="${d.idDistrito}" data-municipio="${d.idMunicipio}" ${perfil.idDistrito == d.idDistrito ? 'selected' : ''}><c:out value="${d.nombre}"/></option>
            </c:forEach>
          </select>
        </label>
        <label class="block sm:col-span-2">
          <span class="text-label-sm text-on-surface-variant">Dirección de residencia</span>
          <input class="campo mt-1" name="direccion" maxlength="200" autocomplete="street-address" placeholder="Colonia, calle, número"
                 value="<c:out value='${perfil.direccion}'/>">
        </label>
        <div class="sm:col-span-2">
          <button type="submit" class="boton-primario"><span class="material-symbols-outlined text-[18px]">save</span> Guardar datos</button>
        </div>
      </form>

      <%-- ===== Cambiar contraseña ===== --%>
      <form id="clave" method="post" action="${ctx}/app/perfil" class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 space-y-3 self-start">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="accion" value="clave">
        <div>
          <h2 class="text-title-lg font-bold text-primary flex items-center gap-2"><span class="material-symbols-outlined">key</span> Cambiar contraseña</h2>
          <p class="text-caption text-secondary mt-1">Último cambio: ${perfil.ultimoCambioClaveTexto}</p>
        </div>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Contraseña actual</span>
          <input class="campo mt-1" type="password" name="claveActual" required autocomplete="current-password">
        </label>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Nueva contraseña</span>
          <input class="campo mt-1" type="password" name="claveNueva" required minlength="8" autocomplete="new-password">
          <span class="text-caption text-secondary">Mínimo 8 caracteres, una mayúscula, un número y un símbolo.</span>
        </label>
        <label class="block">
          <span class="text-label-sm text-on-surface-variant">Confirmar nueva contraseña</span>
          <input class="campo mt-1" type="password" name="claveConfirmacion" required minlength="8" autocomplete="new-password">
        </label>
        <button type="submit" class="boton-primario w-full"><span class="material-symbols-outlined text-[18px]">check_circle</span> Actualizar contraseña</button>
      </form>
    </div>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
<script>
  // Máscaras: pone los separadores solos mientras se escribe
  (function () {
    function mascara(id, partes, separador) {
      var campo = document.getElementById(id);
      campo.addEventListener('input', function () {
        var d = campo.value.replace(/\D/g, '');
        var r = '', i = 0;
        for (var p = 0; p < partes.length && i < d.length; p++) {
          if (p > 0) { r += separador; }
          r += d.substring(i, i + partes[p]);
          i += partes[p];
        }
        campo.value = r;
      });
    }
    mascara('fechaNacimiento', [2, 2, 4], '/');
    mascara('telefono', [4, 4], '-');
  })();

  // Distrito: solo se muestran los del municipio elegido
  (function () {
    var municipio = document.getElementById('idMunicipio');
    var distrito = document.getElementById('idDistrito');
    function filtrar() {
      var opciones = distrito.options;
      for (var i = 1; i < opciones.length; i++) {
        var visible = opciones[i].dataset.municipio === municipio.value;
        opciones[i].hidden = !visible;
        opciones[i].disabled = !visible;
        if (!visible && opciones[i].selected) { distrito.value = ''; }
      }
      distrito.disabled = municipio.value === '';
    }
    municipio.addEventListener('change', filtrar);
    filtrar();
  })();
</script>
</body>
</html>
