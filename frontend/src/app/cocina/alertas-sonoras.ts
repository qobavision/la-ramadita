import { TipoAvisoCocina } from './cocina.models';

/** [frecuencia Hz, inicio s, duración s] */
type Tono = readonly [number, number, number];

/** Avisos con sonido: los de Cocina y el de producto listo para el mesero (RN-13). */
export type SonidoAviso = TipoAvisoCocina | 'PRODUCTO_PREPARADO';

/**
 * Sonidos distintos para pedido nuevo, modificado y cancelado (pedido del
 * stakeholder). Se generan con Web Audio, sin archivos externos.
 */
const PATRONES: Record<SonidoAviso, readonly Tono[]> = {
  PRODUCTO_PREPARADO: [
    [784, 0, 0.12],
    [988, 0.14, 0.12],
    [1319, 0.28, 0.25],
  ],
  PEDIDO_NUEVO: [
    [880, 0, 0.15],
    [1175, 0.2, 0.22],
  ],
  PEDIDO_MODIFICADO: [
    [660, 0, 0.12],
    [660, 0.18, 0.12],
    [660, 0.36, 0.12],
  ],
  PEDIDO_CANCELADO: [
    [440, 0, 0.3],
    [294, 0.34, 0.45],
  ],
};

/**
 * El navegador solo permite sonar después de un gesto del usuario: el
 * contexto se crea o reanuda en `activar()` (al pulsar cualquier botón del
 * monitor) y luego los avisos suenan solos.
 */
export class AlertasSonoras {
  private contexto: AudioContext | null = null;

  activar(): void {
    const ctx = this.obtener();
    if (ctx?.state === 'suspended') {
      void ctx.resume();
    }
  }

  reproducir(tipo: SonidoAviso): void {
    const ctx = this.obtener();
    if (!ctx) {
      return;
    }
    if (ctx.state === 'suspended') {
      void ctx.resume();
    }
    const inicio = ctx.currentTime + 0.02;
    for (const [frecuencia, desfase, duracion] of PATRONES[tipo]) {
      const oscilador = ctx.createOscillator();
      const volumen = ctx.createGain();
      oscilador.type = 'sine';
      oscilador.frequency.value = frecuencia;
      volumen.gain.setValueAtTime(0.0001, inicio + desfase);
      volumen.gain.exponentialRampToValueAtTime(0.35, inicio + desfase + 0.02);
      volumen.gain.exponentialRampToValueAtTime(0.0001, inicio + desfase + duracion);
      oscilador.connect(volumen).connect(ctx.destination);
      oscilador.start(inicio + desfase);
      oscilador.stop(inicio + desfase + duracion + 0.05);
    }
  }

  cerrar(): void {
    void this.contexto?.close();
    this.contexto = null;
  }

  private obtener(): AudioContext | null {
    if (this.contexto) {
      return this.contexto;
    }
    try {
      this.contexto = new AudioContext();
    } catch {
      this.contexto = null;
    }
    return this.contexto;
  }
}
