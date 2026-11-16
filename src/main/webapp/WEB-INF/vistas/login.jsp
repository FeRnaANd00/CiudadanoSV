<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%-- Pestaña activa: la que indique el controlador o ?tab=registro en la URL --%>
<c:set var="tabActiva" value="${not empty tab ? tab : (param.tab == 'registro' ? 'registro' : 'login')}"/>
<c:set var="codigoServicio" value="${not empty servicio ? servicio.codigo : ''}"/>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Iniciar sesión</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full px-4 py-6">
  <div class="w-full max-w-md mx-auto space-y-4">

    <%-- Tarjeta de marca --%>
    <section class="relative overflow-hidden rounded-xl bg-primary p-5 flex items-center justify-between text-on-primary">
      <span class="material-symbols-outlined absolute -right-8 -bottom-8 text-[140px] opacity-10 pointer-events-none select-none" aria-hidden="true">shield</span>
      <div class="flex items-center gap-3.5 relative">
        <span class="w-12 h-12 rounded-full bg-primary-container border border-white/20 flex items-center justify-center shrink-0">
          <span class="material-symbols-outlined icono-relleno text-[26px] text-tertiary-fixed">verified</span>
        </span>
        <div>
          <p class="font-display text-2xl font-bold leading-none">Ciudadano<span class="text-tertiary-fixed">SV</span></p>
          <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold mt-1">Portal de Servicios Digitales</p>
        </div>
      </div>
      <div class="hidden sm:flex flex-col items-end relative">
        <span class="px-2 py-0.5 rounded bg-white/10 text-white/90 text-label-sm">El Salvador</span>
        <span class="text-[10px] text-white/70 tracking-wider mt-1">GOBIERNO DIGITAL</span>
      </div>
    </section>

    <%-- Servicio que eligió en la landing --%>
    <c:if test="${not empty servicio}">
      <div class="flex items-start gap-3 rounded-xl bg-surface-container-low p-4">
        <span class="material-symbols-outlined text-primary">assignment</span>
        <p class="text-body-sm text-on-surface">
          Para continuar con <strong class="text-primary"><c:out value="${servicio.nombre}"/></strong>
          inicia sesión o crea tu cuenta.
        </p>
      </div>
    </c:if>

    <%-- Llegó desde una página protegida --%>
    <c:if test="${empty servicio and not empty destino}">
      <div class="flex items-start gap-3 rounded-xl bg-surface-container-low p-4">
        <span class="material-symbols-outlined text-primary">lock</span>
        <p class="text-body-sm text-on-surface">Esa página es privada. Inicia sesión para continuar.</p>
      </div>
    </c:if>

    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span>
        <p class="text-label-md"><c:out value="${error}"/></p>
      </div>
    </c:if>
    <c:if test="${not empty exito}">
      <div class="flex items-start gap-3 rounded-xl bg-[#EAF5EE] text-exito p-4" role="status">
        <span class="material-symbols-outlined icono-relleno">check_circle</span>
        <p class="text-label-md"><c:out value="${exito}"/></p>
      </div>
    </c:if>

    <div class="bg-surface-container-lowest rounded-xl border border-surface-container p-5 space-y-5">

      <%-- Pestañas --%>
      <div class="flex w-full bg-surface-container-high rounded-lg p-1" role="tablist">
        <button type="button" id="tab-login" role="tab" onclick="cambiarTab('login')"
                class="flex-1 py-2.5 rounded-md text-label-md flex items-center justify-center gap-1.5 transition-all">
          <span class="material-symbols-outlined text-[18px]">login</span> Iniciar Sesión
        </button>
        <button type="button" id="tab-registro" role="tab" onclick="cambiarTab('registro')"
                class="flex-1 py-2.5 rounded-md text-label-md flex items-center justify-center gap-1.5 transition-all">
          <span class="material-symbols-outlined text-[18px]">person_add</span> Registrarse
        </button>
      </div>

      <%-- ===== INICIAR SESIÓN ===== --%>
      <div id="vista-login" class="flex flex-col space-y-4">
        <div class="flex flex-col space-y-1.5">
          <span class="text-label-sm text-on-surface-variant">Método de autenticación</span>
          <div class="grid grid-cols-2 gap-2">
            <button type="button" id="metodo-dui" onclick="elegirMetodo('dui')"
                    class="py-2 px-3 rounded-md text-label-sm flex items-center justify-center gap-1.5 transition-colors">
              <span class="material-symbols-outlined text-[16px]">badge</span> DUI
            </button>
            <button type="button" id="metodo-correo" onclick="elegirMetodo('correo')"
                    class="py-2 px-3 rounded-md text-label-sm flex items-center justify-center gap-1.5 transition-colors">
              <span class="material-symbols-outlined text-[16px]">mail</span> Correo
            </button>
          </div>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/login" class="flex flex-col space-y-4">
          <input type="hidden" name="accion" value="ingresar">
          <input type="hidden" name="servicio" value="${fn:escapeXml(codigoServicio)}">
          <input type="hidden" name="destino" value="${fn:escapeXml(destino)}">

          <div class="flex flex-col space-y-1">
            <div class="flex justify-between items-center">
              <label for="identificador" id="etiqueta-identificador" class="text-label-md text-on-surface">
                Número de DUI <span class="text-error">*</span>
              </label>
              <span id="pista-identificador" class="text-caption text-secondary">Formato: 00000000-0</span>
            </div>
            <div class="relative flex items-center">
              <span id="icono-identificador" class="material-symbols-outlined absolute left-3 text-secondary text-[20px] pointer-events-none">pin</span>
              <input id="identificador" name="identificador" type="text" maxlength="10" required
                     value="${fn:escapeXml(param.identificador)}" placeholder="02345678-9" autocomplete="username"
                     class="w-full h-11 pl-10 pr-3 rounded-md bg-surface-container-low text-body-md focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-outline">
            </div>
          </div>

          <div class="flex flex-col space-y-1">
            <div class="flex justify-between items-center">
              <label for="clave" class="text-label-md text-on-surface">Contraseña <span class="text-error">*</span></label>
              <button type="button" onclick="verClave('clave', this)" class="text-label-sm text-primary hover:underline flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px]">visibility</span><span>Ver</span>
              </button>
            </div>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-secondary text-[20px] pointer-events-none">lock</span>
              <input id="clave" name="clave" type="password" required placeholder="••••••••••••" autocomplete="current-password"
                     class="w-full h-11 pl-10 pr-3 rounded-md bg-surface-container-low text-body-md focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-outline">
            </div>
          </div>

          <button type="submit" class="w-full h-11 bg-primary hover:bg-[#071F4D] text-on-primary rounded-md text-label-md font-bold transition-colors flex items-center justify-center gap-2 mt-2">
            <span>Ingresar a CiudadanoSV</span>
            <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
          </button>
        </form>
      </div>

      <%-- ===== REGISTRO ===== --%>
      <div id="vista-registro" class="hidden flex-col space-y-4">
        <div class="bg-surface-container-low p-3 rounded-lg flex items-start gap-2.5">
          <span class="material-symbols-outlined text-primary text-[20px] mt-0.5">how_to_reg</span>
          <p class="text-body-sm text-on-surface">Crea tu perfil ciudadano para realizar trámites simulados.</p>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/login" class="flex flex-col space-y-3.5">
          <input type="hidden" name="accion" value="registrar">
          <input type="hidden" name="servicio" value="${fn:escapeXml(codigoServicio)}">
          <input type="hidden" name="destino" value="${fn:escapeXml(destino)}">

          <div class="flex flex-col space-y-1">
            <label for="nombre" class="text-label-md text-on-surface">Nombre completo <span class="text-error">*</span></label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-secondary text-[20px] pointer-events-none">person</span>
              <input id="nombre" name="nombre" type="text" required maxlength="120" value="${fn:escapeXml(param.nombre)}"
                     placeholder="Ej. Ana Beatriz Gómez Flores" autocomplete="name"
                     class="w-full h-11 pl-10 pr-3 rounded-md bg-surface-container-low text-body-md focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-outline">
            </div>
          </div>

          <div class="flex flex-col space-y-1">
            <div class="flex justify-between items-center">
              <label for="dui" class="text-label-md text-on-surface">Número de DUI <span class="text-error">*</span></label>
              <span class="text-caption text-secondary">00000000-0</span>
            </div>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-secondary text-[20px] pointer-events-none">badge</span>
              <input id="dui" name="dui" type="text" required maxlength="10" oninput="formatoDui(this)"
                     value="${fn:escapeXml(param.dui)}" placeholder="02345678-9"
                     class="w-full h-11 pl-10 pr-3 rounded-md bg-surface-container-low text-body-md focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-outline">
            </div>
          </div>

          <div class="flex flex-col space-y-1">
            <label for="telefono" class="text-label-md text-on-surface">Teléfono de contacto <span class="text-error">*</span></label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-secondary text-[20px] pointer-events-none">call</span>
              <input id="telefono" name="telefono" type="tel" required maxlength="9" oninput="formatoTelefono(this)"
                     value="${fn:escapeXml(param.telefono)}" placeholder="7000-0000" autocomplete="tel"
                     class="w-full h-11 pl-10 pr-3 rounded-md bg-surface-container-low text-body-md focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-outline">
            </div>
          </div>

          <div class="flex flex-col space-y-1">
            <label for="correo" class="text-label-md text-on-surface">Correo electrónico <span class="text-error">*</span></label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-secondary text-[20px] pointer-events-none">mail</span>
              <input id="correo" name="correo" type="email" required maxlength="120" value="${fn:escapeXml(param.correo)}"
                     placeholder="usuario.demo@ciudadano.sv" autocomplete="email"
                     class="w-full h-11 pl-10 pr-3 rounded-md bg-surface-container-low text-body-md focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-outline">
            </div>
          </div>

          <div class="flex flex-col space-y-1">
            <label for="claveNueva" class="text-label-md text-on-surface">Contraseña nueva <span class="text-error">*</span></label>
            <div class="relative flex items-center">
              <span class="material-symbols-outlined absolute left-3 text-secondary text-[20px] pointer-events-none">lock_reset</span>
              <input id="claveNueva" name="claveNueva" type="password" required oninput="revisarClave(this.value)"
                     placeholder="Cree una contraseña segura" autocomplete="new-password"
                     class="w-full h-11 pl-10 pr-3 rounded-md bg-surface-container-low text-body-md focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-outline">
            </div>
            <div class="pt-2 px-1 grid grid-cols-2 gap-1.5 text-secondary">
              <div id="regla-largo" class="flex items-center gap-1 text-caption"><span class="material-symbols-outlined text-[14px]">check_circle</span>Mín. 8 caracteres</div>
              <div id="regla-numero" class="flex items-center gap-1 text-caption"><span class="material-symbols-outlined text-[14px]">check_circle</span>Al menos 1 número</div>
              <div id="regla-mayus" class="flex items-center gap-1 text-caption"><span class="material-symbols-outlined text-[14px]">check_circle</span>1 letra mayúscula</div>
              <div id="regla-simbolo" class="flex items-center gap-1 text-caption"><span class="material-symbols-outlined text-[14px]">check_circle</span>1 símbolo (#,$,@)</div>
            </div>
          </div>

          <label class="flex items-start gap-2.5 cursor-pointer pt-1">
            <input type="checkbox" name="acepto" required class="w-4 h-4 mt-0.5 accent-primary">
            <span class="text-body-sm text-on-surface-variant">
              Reconozco que este es un entorno de demostración académica y acepto generar una credencial simulada.
            </span>
          </label>

          <button type="submit" class="w-full h-11 bg-primary hover:bg-[#071F4D] text-on-primary rounded-md text-label-md font-bold transition-colors flex items-center justify-center gap-2 mt-2">
            <span>Crear mi cuenta</span>
            <span class="material-symbols-outlined text-[18px]">verified_user</span>
          </button>
        </form>
      </div>
    </div>

    <p class="text-center">
      <a href="${pageContext.request.contextPath}/inicio" class="text-label-md text-primary hover:underline inline-flex items-center gap-1">
        <span class="material-symbols-outlined text-[18px]">arrow_back</span> Volver al catálogo de servicios
      </a>
    </p>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>

<script>
  var ACTIVA = 'bg-surface-container-lowest text-primary font-bold shadow-sm';
  var INACTIVA = 'text-on-surface-variant font-medium';

  function clases(el, quitar, poner) {
    quitar.split(' ').forEach(function (c) { el.classList.remove(c); });
    poner.split(' ').forEach(function (c) { el.classList.add(c); });
  }

  function cambiarTab(tab) {
    var esLogin = tab === 'login';
    clases(document.getElementById('tab-login'), esLogin ? INACTIVA : ACTIVA, esLogin ? ACTIVA : INACTIVA);
    clases(document.getElementById('tab-registro'), esLogin ? ACTIVA : INACTIVA, esLogin ? INACTIVA : ACTIVA);
    document.getElementById('tab-login').setAttribute('aria-selected', esLogin);
    document.getElementById('tab-registro').setAttribute('aria-selected', !esLogin);
    var vLogin = document.getElementById('vista-login');
    var vRegistro = document.getElementById('vista-registro');
    vLogin.classList.toggle('hidden', !esLogin);
    vLogin.classList.toggle('flex', esLogin);
    vRegistro.classList.toggle('hidden', esLogin);
    vRegistro.classList.toggle('flex', !esLogin);
  }

  var M_ACTIVO = 'bg-primary-fixed text-on-primary-fixed';
  var M_INACTIVO = 'bg-surface-container text-on-surface-variant';

  function elegirMetodo(metodo, conservarValor) {
    var esDui = metodo === 'dui';
    clases(document.getElementById('metodo-dui'), esDui ? M_INACTIVO : M_ACTIVO, esDui ? M_ACTIVO : M_INACTIVO);
    clases(document.getElementById('metodo-correo'), esDui ? M_ACTIVO : M_INACTIVO, esDui ? M_INACTIVO : M_ACTIVO);
    var input = document.getElementById('identificador');
    document.getElementById('etiqueta-identificador').innerHTML =
        (esDui ? 'Número de DUI' : 'Correo electrónico') + ' <span class="text-error">*</span>';
    document.getElementById('pista-identificador').textContent = esDui ? 'Formato: 00000000-0' : 'Formato: correo@dominio.sv';
    document.getElementById('icono-identificador').textContent = esDui ? 'pin' : 'mail';
    input.placeholder = esDui ? '02345678-9' : 'usuario@ejemplo.gob.sv';
    input.type = esDui ? 'text' : 'email';
    input.autocomplete = 'username';
    if (esDui) { input.maxLength = 10; input.oninput = function () { formatoDui(input); }; }
    else { input.removeAttribute('maxlength'); input.oninput = null; }
    if (!conservarValor) { input.value = ''; }
  }

  function formatoDui(input) {
    var v = input.value.replace(/\D/g, '');
    if (v.length > 8) { v = v.substring(0, 8) + '-' + v.substring(8, 9); }
    input.value = v.substring(0, 10);
  }

  function formatoTelefono(input) {
    var v = input.value.replace(/\D/g, '');
    if (v.length > 4) { v = v.substring(0, 4) + '-' + v.substring(4, 8); }
    input.value = v.substring(0, 9);
  }

  function verClave(id, boton) {
    var input = document.getElementById(id);
    var mostrar = input.type === 'password';
    input.type = mostrar ? 'text' : 'password';
    boton.children[0].textContent = mostrar ? 'visibility_off' : 'visibility';
    boton.children[1].textContent = mostrar ? 'Ocultar' : 'Ver';
  }

  function revisarClave(c) {
    marcar('regla-largo', c.length >= 8);
    marcar('regla-numero', /\d/.test(c));
    marcar('regla-mayus', /[A-Z]/.test(c));
    marcar('regla-simbolo', /[^A-Za-z0-9]/.test(c));
  }

  function marcar(id, ok) {
    var el = document.getElementById(id);
    el.classList.toggle('text-exito', ok);
    el.classList.toggle('font-semibold', ok);
  }

  // Estado inicial (pestaña indicada por el servidor y método según lo escrito)
  cambiarTab('${tabActiva == "registro" ? "registro" : "login"}');
  elegirMetodo(document.getElementById('identificador').value.indexOf('@') >= 0 ? 'correo' : 'dui', true);
</script>
</body>
</html>
