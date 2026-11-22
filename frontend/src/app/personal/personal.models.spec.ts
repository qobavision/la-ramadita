import { formatearFechaHora } from './personal.models';

describe('formatearFechaHora', () => {
  it('interpreta como UTC las fechas del backend que llegan sin zona', () => {
    // LocalDateTime en UTC sin "Z" (empleados, usuarios, compras, movimientos).
    expect(formatearFechaHora('2026-11-23T01:00:00')).toBe(formatearFechaHora('2026-11-23T01:00:00Z'));
  });

  it('respeta los instantes que ya traen zona', () => {
    expect(formatearFechaHora('2026-11-23T01:00:00Z')).toBe(formatearFechaHora('2026-11-22T20:00:00-05:00'));
  });

  it('devuelve un guion si no hay fecha', () => {
    expect(formatearFechaHora(null)).toBe('—');
    expect(formatearFechaHora('no-es-fecha')).toBe('—');
  });
});
