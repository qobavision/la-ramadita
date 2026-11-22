package com.laramadita.backend.pedidos.model;

/** Evento de dominio: el mesero reabrió una cuenta pendiente de pago (RN-07). Caja la retira de cobros. */
public record CuentaReabierta(Integer atencionId, String identificacion) {
}
