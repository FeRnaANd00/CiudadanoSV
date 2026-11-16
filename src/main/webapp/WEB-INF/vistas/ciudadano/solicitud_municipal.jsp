<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV | Trámite municipal</title>
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
      <p class="text-label-sm uppercase text-secondary">Servicios Municipales &bull; Alcaldías</p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold text-primary mt-1">Trámite municipal</h1>
      <p class="text-body-sm text-on-surface-variant mt-1">Solicita tu solvencia o constancia de residencia sin ir a la alcaldía.</p>
    </section>

    <c:if test="${not empty error}">
      <div class="flex items-start gap-3 rounded-xl bg-error-container text-[#93000a] p-4" role="alert">
        <span class="material-symbols-outlined">error</span>
        <p class="text-label-md"><c:out value="${error}"/></p>
      </div>
    </c:if>

    <form method="post" action="${ctx}/app/solicitud/municipal"
          class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 sm:p-6 space-y-5">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}">

      <%-- Elegir trámite (tarjetas tipo radio) --%>
      <fieldset>
        <legend class="text-label-md text-on-surface mb-2">Trámite <span class="text-error">*</span></legend>
        <div class="grid gap-3 sm:grid-cols-2">
          <c:forEach var="op" items="${opciones}">
            <label class="opcion-radio flex items-start gap-3 rounded-xl border border-surface-container p-4 cursor-pointer">
              <input type="radio" name="codigo" value="${op.codigo}" class="mt-1 accent-primary" ${op.codigo == tramite.codigo ? 'checked' : ''}>
              <span>
                <span class="block text-label-md text-on-surface"><c:out value="${op.nombre}"/></span>
                <span class="block text-caption text-secondary mt-0.5">
                  $<fmt:formatNumber value="${op.arancel}" minFractionDigits="2" maxFractionDigits="2"/>
                  &bull; ${op.plazoHoras} h hábiles &bull; 100% digital
                </span>
              </span>
            </label>
          </c:forEach>
        </div>
      </fieldset>

      <div class="rounded-xl bg-surface-container-low p-4 flex items-start gap-3">
        <span class="material-symbols-outlined text-primary">person_pin</span>
        <div class="min-w-0">
          <p class="text-caption uppercase text-secondary font-semibold">Solicitante</p>
          <p class="text-label-md text-on-surface"><c:out value="${sessionScope.usuario.nombreCompleto}"/></p>
          <p class="text-body-sm text-on-surface-variant">DUI <c:out value="${sessionScope.usuario.dui}"/></p>
        </div>
      </div>

      <div>
        <label for="idMunicipio" class="block text-label-md text-on-surface mb-1">Municipio <span class="text-error">*</span></label>
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

      <div>
        <label for="domicilio" class="block text-label-md text-on-surface mb-1">Dirección de residencia <span class="text-error">*</span></label>
        <textarea id="domicilio" name="domicilio" required minlength="10" maxlength="200" rows="3" class="campo"
                  placeholder="Colonia, calle o pasaje, número de casa">${fn:escapeXml(param.domicilio)}</textarea>
      </div>

      <label class="flex items-start gap-3 rounded-xl bg-surface-container-low p-3 cursor-pointer">
        <input type="checkbox" name="declaracion" required class="w-4 h-4 mt-0.5 accent-primary" ${not empty param.declaracion ? 'checked' : ''}>
        <span class="text-body-sm text-on-surface-variant">Declaro que la información proporcionada es verdadera.</span>
      </label>

      <div class="flex flex-col-reverse sm:flex-row sm:justify-end gap-3">
        <a href="${urlFicha}" class="boton-borde">Cancelar</a>
        <button type="submit" class="boton-primario">
          <span class="material-symbols-outlined text-[18px]">send</span> Enviar solicitud
        </button>
      </div>
    </form>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
