package dev.wakandaacademy.produdoro.tarefa.application.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

public interface TarefaRepository {

    Tarefa salva(Tarefa tarefa);

    Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);
    
	void modificaOrdemTarefa(Tarefa tarefa, List<Tarefa> tarefasUsuario, int novaPosicao);

    List<Tarefa> buscaTarefasPorUsuario(UUID idUsuario);
    
    void desativaTarefaAtiva(UUID idUsuario);

    List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario);

    void deletaTodasTarefasUsuario(List<Tarefa> tarefas);
}
