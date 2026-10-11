import { normalizarTextoDescriptivo } from './texto-descriptivo';

describe('normalizarTextoDescriptivo', () => {
  it('capitaliza y limpia espacios', () => {
    expect(normalizarTextoDescriptivo('  pollo  ')).toBe('Pollo');
    expect(normalizarTextoDescriptivo('POLLO ENTERO')).toBe('Pollo Entero');
    expect(normalizarTextoDescriptivo('papa amarilla')).toBe('Papa Amarilla');
  });
});
