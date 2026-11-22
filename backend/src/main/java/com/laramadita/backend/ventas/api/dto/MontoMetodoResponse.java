package com.laramadita.backend.ventas.api.dto;

import java.math.BigDecimal;

/** Monto cobrado con un método de pago (Efectivo, Yape, Plin, Tarjeta, Transferencia). */
public record MontoMetodoResponse(String metodo, BigDecimal monto) {
}
