/**
 * Proxy del servidor de desarrollo de Angular hacia la API Spring Boot.
 *
 * Por defecto el backend se asume en la misma PC que `ng serve`.
 * Si el backend corre en otra máquina de la red local, definir la variable
 * de entorno API_PROXY_TARGET antes de ejecutar `npm start`, por ejemplo:
 *
 *   PowerShell:  $env:API_PROXY_TARGET = "http://192.168.1.10:8080"
 *   CMD:         set API_PROXY_TARGET=http://192.168.1.10:8080
 */
const target = process.env['API_PROXY_TARGET'] || 'http://localhost:8080';

module.exports = {
  '/api': {
    target,
    secure: false,
    changeOrigin: true,
  },
};
