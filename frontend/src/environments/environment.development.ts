/**
 * Configuración de desarrollo (`ng serve`).
 *
 * `apiUrl` sigue siendo relativa: el servidor de desarrollo de Angular reenvía
 * `/api` al backend según `proxy.conf.js`. De este modo la PC de desarrollo,
 * otra PC o una tablet de la red local usan la misma URL sin tocar código.
 *
 * Si se prefiere llamar al backend directamente, cambiar aquí a una URL
 * absoluta (por ejemplo `http://192.168.1.10:8080/api/v1`) y asegurarse de que
 * `CORS_ALLOWED_ORIGINS` del backend incluya el origen del frontend.
 */
export const environment = {
  production: false,
  apiUrl: '/api/v1',
};
