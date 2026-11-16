/* Tema de colores y tipografía de CiudadanoSV (copiado de las pantallas de diseño).
   Se carga justo después de https://cdn.tailwindcss.com en todas las vistas. */
tailwind.config = {
  theme: {
    extend: {
      colors: {
        "primary": "#002869",
        "primary-container": "#0b3d91",
        "primary-fixed": "#dae2ff",
        "primary-fixed-dim": "#b1c5ff",
        "on-primary": "#ffffff",
        "on-primary-fixed": "#001947",
        "on-primary-container": "#8dadff",
        "secondary": "#4a5d8e",
        "secondary-container": "#b5c8ff",
        "secondary-fixed": "#dae2ff",
        "on-secondary": "#ffffff",
        "on-secondary-fixed-variant": "#324575",
        "tertiary": "#003138",
        "tertiary-container": "#004953",
        "tertiary-fixed": "#9defff",
        "tertiary-fixed-dim": "#68d6e9",
        "on-tertiary": "#ffffff",
        "on-tertiary-container": "#4cbdcf",
        "background": "#f8f9ff",
        "surface": "#f8f9ff",
        "surface-container-lowest": "#ffffff",
        "surface-container-low": "#eff4ff",
        "surface-container": "#e6eeff",
        "surface-container-high": "#dfe9fc",
        "surface-container-highest": "#d9e3f6",
        "on-surface": "#121c2a",
        "on-surface-variant": "#434652",
        "outline": "#747783",
        "outline-variant": "#c4c6d3",
        "error": "#ba1a1a",
        "error-container": "#ffdad6",
        "dorado": "#C68A2E",
        "dorado-claro": "#E8AE52",
        "exito": "#107C41"
      },
      fontFamily: {
        sans: ["Public Sans", "sans-serif"],
        display: ["\"Source Serif 4\"", "serif"]
      },
      fontSize: {
        "caption": ["11px", { lineHeight: "14px", letterSpacing: "0.05em", fontWeight: "500" }],
        "label-sm": ["12px", { lineHeight: "16px", letterSpacing: "0.04em", fontWeight: "600" }],
        "label-md": ["14px", { lineHeight: "20px", letterSpacing: "0.02em", fontWeight: "600" }],
        "body-sm": ["13px", { lineHeight: "18px", letterSpacing: "0.01em" }],
        "body-md": ["15px", { lineHeight: "24px" }],
        "body-lg": ["18px", { lineHeight: "28px" }],
        "title-sm": ["16px", { lineHeight: "24px", fontWeight: "600" }],
        "title-lg": ["18px", { lineHeight: "26px", fontWeight: "600" }],
        "headline-md": ["22px", { lineHeight: "30px", letterSpacing: "-0.01em", fontWeight: "600" }],
        "headline-xl": ["32px", { lineHeight: "40px", letterSpacing: "-0.01em", fontWeight: "600" }],
        "display-lg": ["40px", { lineHeight: "48px", letterSpacing: "-0.02em", fontWeight: "700" }]
      }
    }
  }
};
