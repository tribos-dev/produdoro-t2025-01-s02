package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public List<Tarefa> buscaTarefasPorUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasPorUsuario");
        List<Tarefa> listaTarefas = tarefaSpringMongoDBRepository.findAllByIdUsuario(idUsuario);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasPorUsuario");
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
    public List<Tarefa> buscaTarefasConcluidas(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasConcluidas");
        List<Tarefa> tarefasConcluidas = tarefaSpringMongoDBRepository.findAllByIdUsuarioAndStatus(idUsuario,StatusTarefa.CONCLUIDA);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasConcluidas");
        return tarefasConcluidas;
    }

    @Override
    public void deletaTarefasConcluidas(List<Tarefa> tarefasConcluidas) {
        log.info("[inicia] TarefaInfraRepository - deletaTarefasConcluidas");
        tarefaSpringMongoDBRepository.deleteAll(tarefasConcluidas);
        log.info("[finaliza] TarefaInfraRepository - deletaTarefasConcluidas");
    }

    @Override
    public void ajustaPosicaoDasTarefas(List<Tarefa> tarefasDoUsuario) {
        log.info("[inicia] TarefaInfraRepository - ajustaPosicaoDasTarefas");
        int tamanhoDaLista = tarefasDoUsuario.size();
        List<Tarefa> tarefasAjustadas = IntStream.range(0, tamanhoDaLista)
                .mapToObj(i -> this.ajustaPosicaoDaTarefa(tarefasDoUsuario.get(i), i))
                .collect(Collectors.toList());
        tarefaSpringMongoDBRepository.saveAll(tarefasAjustadas);
        log.info("[finaliza] TarefaInfraRepository - ajustaPosicaoDasTarefas");
    }

    private Tarefa ajustaPosicaoDaTarefa(Tarefa tarefa, int novaPosicao) {
        tarefa.ajustaPosicao(novaPosicao);
        return tarefa;
    }

}
