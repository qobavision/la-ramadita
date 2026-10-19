import { ambitoApi, ambitoDesdeApi, categoriaDesdeApi, categoriaHaciaApi } from './carta.api';

describe('carta.api', () => {
  it('mapea categorías humanas al contrato del backend', () => {
    expect(categoriaHaciaApi('Plato a la brasa')).toBe('POLLO_A_LA_BRASA');
    expect(categoriaHaciaApi('Plato ejecutivo')).toBe('PLATO_EJECUTIVO');
    expect(categoriaHaciaApi('Plato marino')).toBe('PLATO_MARINO');
    expect(categoriaHaciaApi('Menú')).toBe('MENU');
    expect(categoriaHaciaApi('Ensalada')).toBe('ENSALADA');
    expect(categoriaDesdeApi('ENSALADA')).toBe('Ensalada');
    expect(categoriaDesdeApi('PLATO')).toBe('Plato ejecutivo');
    expect(categoriaDesdeApi('PLATO_EJECUTIVO')).toBe('Plato ejecutivo');
    expect(categoriaDesdeApi('GUARNICION')).toBe('Guarnición');
    expect(categoriaDesdeApi('COMBO')).toBe('Combo');
  });

  it('mapea la unidad de negocio al ámbito', () => {
    expect(ambitoApi('Pollería')).toBe('POLLERIA');
    expect(ambitoApi('Restaurante')).toBe('RESTAURANTE');
    expect(ambitoDesdeApi('AMBOS')).toBe('AMBOS');
  });
});
