package com.laramadita.backend.shared.error;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Traduce excepciones a respuestas Problem Details coherentes en toda la API. */
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(RecursoNoEncontradoException.class)
	ProblemDetail noEncontrado(RecursoNoEncontradoException e) {
		return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", e.getMessage());
	}

	@ExceptionHandler(ConflictoException.class)
	ProblemDetail conflicto(ConflictoException e) {
		return problema(HttpStatus.CONFLICT, "Conflicto", e.getMessage());
	}

	@ExceptionHandler(ReglaNegocioException.class)
	ProblemDetail reglaNegocio(ReglaNegocioException e) {
		return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio no cumplida", e.getMessage());
	}

	@ExceptionHandler(OperacionNoAutorizadaException.class)
	ProblemDetail operacionNoAutorizada(OperacionNoAutorizadaException e) {
		return problema(HttpStatus.FORBIDDEN, "Operación no autorizada", e.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail accesoDenegado(AccessDeniedException e) {
		return problema(HttpStatus.FORBIDDEN, "Acceso denegado",
				"No tiene permiso para realizar esta operación");
	}

	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail noAutenticado(AuthenticationException e) {
		return problema(HttpStatus.UNAUTHORIZED, "No autenticado",
				"Credenciales inválidas o usuario inactivo");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail validacion(MethodArgumentNotValidException e) {
		ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "Solicitud inválida",
				"Uno o más campos no cumplen las validaciones");
		Map<String, String> errores = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors()
				.forEach(fe -> errores.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
		p.setProperty("errores", errores);
		return p;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail cuerpoIlegible(HttpMessageNotReadableException e) {
		return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida",
				"El cuerpo de la solicitud no es válido o está mal formado");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ProblemDetail rutaInexistente(NoResourceFoundException e) {
		return problema(HttpStatus.NOT_FOUND, "Ruta no encontrada", "El recurso solicitado no existe");
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail inesperado(Exception e) {
		log.error("Error no controlado", e);
		return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
				"Ocurrió un error inesperado. Intente nuevamente");
	}

	private static ProblemDetail problema(HttpStatus status, String titulo, String detalle) {
		ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalle);
		p.setTitle(titulo);
		p.setType(URI.create("about:blank"));
		return p;
	}
}
