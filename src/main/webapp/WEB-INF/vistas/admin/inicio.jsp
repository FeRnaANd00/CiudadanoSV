<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <%@include file="/WEB-INF/vistas/includes/head.jspf"%>
  <title>CiudadanoSV Admin | Inicio</title>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
<%@include file="/WEB-INF/vistas/includes/header.jspf"%>

<main class="flex-1 w-full">
  <div class="mx-auto max-w-6xl px-4 sm:px-6 py-6 space-y-6">

    <c:if test="${not empty sessionScope.aviso}">
      <div class="flex items-start gap-3 rounded-xl bg-[#EAF5EE] text-exito p-4" role="status">
        <span class="material-symbols-outlined icono-relleno">check_circle</span>
        <p class="text-label-md"><c:out value="${sessionScope.aviso}"/></p>
      </div>
      <c:remove var="aviso" scope="session"/>
    </c:if>

    <section class="relative overflow-hidden rounded-2xl bg-primary text-on-primary p-6">
      <span class="material-symbols-outlined absolute -right-6 -bottom-8 text-[150px] opacity-10 pointer-events-none select-none" aria-hidden="true">admin_panel_settings</span>
      <p class="text-caption uppercase tracking-wider text-dorado-claro font-semibold relative">Portal administrativo &bull; Gestión pública digital</p>
      <h1 class="font-display text-[26px] leading-8 sm:text-headline-xl font-bold mt-2 relative">
        Hola, <c:out value="${sessionScope.usuario.primerNombre}"/>
      </h1>
      <p class="text-body-sm text-primary-fixed mt-1 relative"><c:out value="${sessionScope.usuario.rolTexto}"/></p>
    </section>

    <section>
      <h2 class="text-headline-md font-bold text-primary mb-4">Módulos de administración</h2>
      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <c:forTokens var="m" delims="|" items="inbox;Bandeja de solicitudes;Revisar, cotejar y resolver expedientes;Paso 10|calendar_month;Citas y cupos;Capacidad por sede y agenda del día;Paso 11|category;Servicios y trámites;Catálogo, aranceles y requisitos;Paso 12|group;Gestión de usuarios;Funcionarios, roles y suspensiones;Paso 13|monitoring;Panel estadístico;Demanda por área y estado de los trámites;Paso 14">
          <c:set var="partes" value="${m.split(';')}"/>
          <article class="rounded-2xl bg-surface-container-lowest border border-surface-container p-5 opacity-80">
            <span class="w-11 h-11 rounded-xl bg-primary-fixed text-primary flex items-center justify-center mb-3">
              <span class="material-symbols-outlined">${partes[0]}</span>
            </span>
            <h3 class="text-title-lg font-bold text-primary">${partes[1]}</h3>
            <p class="text-body-sm text-on-surface-variant mt-1">${partes[2]}</p>
            <span class="inline-block mt-3 text-caption font-bold uppercase rounded-full bg-dorado/15 text-dorado px-2 py-0.5">Próximamente &bull; ${partes[3]}</span>
          </article>
        </c:forTokens>
      </div>
    </section>
  </div>
</main>

<%@include file="/WEB-INF/vistas/includes/footer.jspf"%>
</body>
</html>
