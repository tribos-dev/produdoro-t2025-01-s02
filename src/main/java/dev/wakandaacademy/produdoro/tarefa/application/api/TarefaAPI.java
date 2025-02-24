package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.config.security.swagger.DocumentaModificaOrdemTarefa;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tarefa")
public interface TarefaAPI {
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    TarefaIdResponse postNovaTarefa(@RequestBody @Valid TarefaRequest tarefaRequest);

    @GetMapping("/{idTarefa}")
    @ResponseStatus(code = HttpStatus.OK)
    TarefaDetalhadoResponse detalhaTarefa(@RequestHeader(name = "Authorization",required = true) String token,
    		@PathVariable UUID idTarefa);

    @DocumentaModificaOrdemTarefa
    @PatchMapping("/{idTarefa}/modifica-posicao-tarefa")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void usuarioModificaOrdemDeUmaTarefa(@RequestHeader(name = "Authorization",required = true) String token,
    		@PathVariable UUID idTarefa, @RequestParam(required = true, name = "posicao") int novaPosicao);

    @GetMapping("/lista-tarefas/{idUsuario}")
    @ResponseStatus(code = HttpStatus.OK)
    List<TarefaUsuarioListReponse> listaTodasTarefasUsuario(
            @RequestHeader(name = "Authorization", required = true) String token, @PathVariable UUID idUsuario);

    @PatchMapping("/conclui-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void concluiTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                       @PathVariable UUID idTarefa);

    @PatchMapping("/ativa-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void ativaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                     @PathVariable UUID idTarefa);

    @PostMapping("/incrementa-pomodoro/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void incrementaPomodoro(@RequestHeader(name = "Authorization",required = true) String token,
                            @PathVariable UUID idTarefa);

}
