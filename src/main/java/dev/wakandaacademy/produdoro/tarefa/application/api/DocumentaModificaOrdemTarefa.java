package dev.wakandaacademy.produdoro.tarefa.application.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.MediaType;

import dev.wakandaacademy.produdoro.handler.ErrorApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Modifica a ordem de uma tarefa", description = "Este método modifica a ordem de uma tarefa com base na nova posição.")
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Ordem da tarefa modificada com sucesso."),
		@ApiResponse(responseCode = "400", description = "Usuário não encontrado!", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorApiResponse.class), examples = @ExampleObject(value = "{\"message\": \"Usuario não encontrado!\"}"))),
		@ApiResponse(responseCode = "401", description = "Usuário(a) não autorizado(a)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorApiResponse.class), examples = @ExampleObject(value = "{\"message\": \"Usuário(a) não autorizado(a) para a requisição solicitada\"}"))),
		@ApiResponse(responseCode = "404", description = "ID da tarefa inválido", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorApiResponse.class), examples = @ExampleObject(value = "{\"message\": \"ID da tarefa inválido\"}"))),
		@ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }"))) })
public @interface DocumentaModificaOrdemTarefa {
}