package dev.wakandaacademy.produdoro.tarefa.application.repository;

import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository {

    Tarefa salva(Tarefa tarefa);

    Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);

    void modificaOrdemTarefa(Tarefa tarefa, List<Tarefa> tarefasUsuario, int novaPosicao);

    List<Tarefa> buscaTarefasPorUsuario(UUID idUsuario);

    void desativaTarefaAtiva(UUID idUsuario);

    List<Tarefa> buscaTarefasConcluidas(UUID idUsuario);

    void deletaTarefasConcluidas(List<Tarefa> tarefasConcluidas);

    void ajustaPosicaoDasTarefas(List<Tarefa> tarefasDoUsuario);

    List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario);

    void deletaTodasTarefasUsuario(List<Tarefa> tarefas);
}
