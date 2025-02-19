package dev.wakandaacademy.produdoro.tarefa.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaUsuarioListReponse;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }
    
	@Override
	public void usuarioModificaOrdemDeUmaTarefa(String emailUsario, UUID idTarefa, int novaPosicao) {
        log.info("[inicia] TarefaApplicationService - usuarioModificaOrdemDeUmaTarefa");
        Tarefa tarefa = detalhaTarefa(emailUsario, idTarefa);
        List<Tarefa> tarefasUsuario = List.of();
        tarefaRepository.modificaOrdemTarefa(tarefa.getPosicao(), tarefasUsuario, novaPosicao);
        tarefa.alteraPosicao(novaPosicao);
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - usuarioModificaOrdemDeUmaTarefa");
	}

    @Override
    public void concluiTarefa(String usuarioEmail, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService --> concluiTarefa");
        Tarefa tarefa = detalhaTarefa(usuarioEmail, idTarefa);
        tarefa.concluiTarefa();
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService --> concluiTarefa");
    }

    @Override
    public void ativaTarefa(String email, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - ativaTarefa");
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Id da tarefa invalido!"));
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(email);
        tarefa.pertenceAoUsuario(usuario);
        tarefa.verificaSeJaEstaAtiva();
        tarefaRepository.desativaTarefaAtiva(usuario.getIdUsuario());
        tarefa.ativaTarefa();
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - ativaTarefa");
    }

    @Override
    public List<TarefaUsuarioListReponse> listaTodasTarefasUsuario(String email, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - listaTodasTarefasUsuario");
        usuarioRepository.buscaUsuarioPorEmail(email);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - listaTodasTarefasUsuario");
        return TarefaUsuarioListReponse.converte(tarefas);

    }
}
