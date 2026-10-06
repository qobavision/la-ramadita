/**
 * Configuración de producción.
 *
 * `apiUrl` es relativa: el frontend asume que se sirve desde el mismo host
 * que expone la API (reverse proxy o backend sirviendo los estáticos).
 * Así una tablet que abre http://<ip-servidor>/ llama a http://<ip-servidor>/api/v1
 * sin que exista ninguna dirección fija dentro del código.
 */
export const environment = {
  production: true,
  apiUrl: '/api/v1',
};
