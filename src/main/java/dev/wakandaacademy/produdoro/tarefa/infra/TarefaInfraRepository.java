package dev.wakandaacademy.produdoro.tarefa.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

	private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;

	@Override
	public Tarefa salva(Tarefa tarefa) {
		log.info("[inicia] TarefaInfraRepository - salva");
		try {
			tarefaSpringMongoDBRepository.save(tarefa);
		} catch (DataIntegrityViolationException e) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Tarefa já cadastrada", e);
		}
		log.info("[finaliza] TarefaInfraRepository - salva");
		return tarefa;
	}

	@Override
	public Optional<Tarefa> buscaTarefaPorId(UUID idTarefa) {
		log.info("[inicia] TarefaInfraRepository - buscaTarefaPorId");
		Optional<Tarefa> tarefaPorId = tarefaSpringMongoDBRepository.findByIdTarefa(idTarefa);
		log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorId");
		return tarefaPorId;
	}

	@Override
	public void modificaOrdemTarefa(int posicaoAtual, List<Tarefa> tarefasUsuario, int novaPosicao) {
		log.info("[inicia] TarefaInfraRepository - modificaOrdemTarefa");
		int menorPosicao = (novaPosicao < 0 ) ? 0 : Math.min(posicaoAtual, novaPosicao);
		int posicaoInicial = (posicaoAtual < novaPosicao ) ? menorPosicao + 1 : menorPosicao;
		int maiorFinal = (novaPosicao >= (tarefasUsuario.size()) ) ? tarefasUsuario.size() - 1 : Math.max(posicaoAtual, novaPosicao);
		List<Tarefa> tarefasReodernadas = (posicaoAtual < novaPosicao ) ? ordenaTarefasCrescente(tarefasUsuario, posicaoInicial, maiorFinal) : ordenaTarefasDecrescente(tarefasUsuario, posicaoInicial, maiorFinal);
		tarefaSpringMongoDBRepository.saveAll(tarefasReodernadas);
		log.info("[finaliza] TarefaInfraRepository - modificaOrdemTarefa");
	}

	private List<Tarefa> ordenaTarefasCrescente(List<Tarefa> tarefasUsuario, int posicaoInicial, int maiorFinal) {
		log.info("[inicia] TarefaInfraRepository - ordenaTarefasCrescente");
		return IntStream.range(posicaoInicial, maiorFinal)
		.mapToObj(i -> {
			return retornaTarefaAtualizada(tarefasUsuario.get(i), i +1);
		}).collect(Collectors.toList());
	}
	
	private List<Tarefa> ordenaTarefasDecrescente(List<Tarefa> tarefasUsuario, int posicaoInicial, int maiorFinal) {
		log.info("[inicia] TarefaInfraRepository - ordenaTarefasDecrescente");
		return IntStream.range(posicaoInicial, maiorFinal)
		.mapToObj(i -> {
			return retornaTarefaAtualizada(tarefasUsuario.get(i), i -1);
		}).collect(Collectors.toList());
	}

	private Tarefa retornaTarefaAtualizada(Tarefa tarefa, int posicao) {
		log.info("[inicia] TarefaInfraRepository - retornaTarefa");
		tarefa.alteraPosicao(posicao);
		log.info("[finaliza] TarefaInfraRepository - retornaTarefa");
		return tarefa;
	}
}
