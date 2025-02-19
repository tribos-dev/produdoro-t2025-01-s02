package dev.wakandaacademy.produdoro.tarefa.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
	private final MongoTemplate mongoTemplate;

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
		int menorPosicao = (novaPosicao < 0) ? 0 : Math.min(posicaoAtual, novaPosicao);
		int posicaoInicial = (posicaoAtual < novaPosicao) ? menorPosicao + 1 : menorPosicao;
		int posicaoFinal = (novaPosicao >= (tarefasUsuario.size())) ? tarefasUsuario.size() - 1
				: Math.max(posicaoAtual, novaPosicao);
		int direcao = (posicaoAtual < novaPosicao) ? 1 : -1;
		List<Tarefa> tarefasReodernadas = ordenaTarefas(tarefasUsuario, posicaoInicial, posicaoFinal, direcao);
		tarefaSpringMongoDBRepository.saveAll(tarefasReodernadas);
		log.info("[finaliza] TarefaInfraRepository - modificaOrdemTarefa");
	}

	private List<Tarefa> ordenaTarefas(List<Tarefa> tarefasUsuario, int posicaoInicial, int posicaoFinal, int direcao) {
		log.info("[inicia] TarefaInfraRepository - ordenaTarefas");
		return IntStream.range(posicaoInicial, posicaoFinal).mapToObj(posicao -> {
			return atualizaTarefa(tarefasUsuario.get(posicao), posicao + direcao);
		}).collect(Collectors.toList());
	}

	private Tarefa atualizaTarefa(Tarefa tarefa, int posicao) {
		log.info("[inicia] TarefaInfraRepository - atualizaTarefa");
		tarefa.alteraPosicao(posicao);
		log.info("[finaliza] TarefaInfraRepository - atualizaTarefa");
		return tarefa;
	}

	public List<Tarefa> buscaTarefasPorUsuario(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - buscaTarefaPorUsuario");
		List<Tarefa> listaTarefas = tarefaSpringMongoDBRepository.findAllByIdUsuario(idUsuario);
		log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorUsuario");
		return listaTarefas;
	}

	@Override
	public void desativaTarefaAtiva(UUID idUsuario) {
		log.info("[inicia] TarefaInfraRepository - desativaTarefaAtiva");
		Query query = new Query(Criteria.where("statusAtivação").is("ATIVA").and("idUsuario").is(idUsuario));
		Update update = new Update().set("statusAtivacao", "INATIVA");
		mongoTemplate.updateMulti(query, update, Tarefa.class);
		log.info("[finaliza] TarefaInfraRepository - desativaTarefaAtiva");

	}

	@Override
    public List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasPorUsuario");
        List<Tarefa> tarefas = tarefaSpringMongoDBRepository.findAllTarefaByidUsuario(idUsuario);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasPorUsuario");
        return tarefas;
    }
}
