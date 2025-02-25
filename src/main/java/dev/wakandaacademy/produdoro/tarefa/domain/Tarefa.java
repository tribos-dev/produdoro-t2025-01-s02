package dev.wakandaacademy.produdoro.tarefa.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Log4j2
@Document(collection = "Tarefa")
public class Tarefa {
	@Id
	private UUID idTarefa;
	@NotBlank
	private String descricao;
	@Indexed
	private UUID idUsuario;
	@Indexed
	private UUID idArea;
	@Indexed
	private UUID idProjeto;
	private StatusTarefa status;
	private StatusAtivacaoTarefa statusAtivacao;
	private int contagemPomodoro;
	private int posicao;

	public Tarefa(TarefaRequest tarefaRequest, int posicao) {
		this.idTarefa = UUID.randomUUID();
		this.idUsuario = tarefaRequest.getIdUsuario();
		this.descricao = tarefaRequest.getDescricao();
		this.idArea = tarefaRequest.getIdArea();
		this.idProjeto = tarefaRequest.getIdProjeto();
		this.status = StatusTarefa.A_FAZER;
		this.statusAtivacao = StatusAtivacaoTarefa.INATIVA;
		this.contagemPomodoro = 1;
		this.posicao = posicao;
	}

	public void pertenceAoUsuario(Usuario usuarioPorEmail) {
		if (!this.idUsuario.equals(usuarioPorEmail.getIdUsuario())) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário não é dono da tarefa solicitada!");
		}
	}

	public void alteraPosicao(int posicao) {
		this.posicao= posicao;
	}

	public void concluiTarefa() {
		this.status = StatusTarefa.CONCLUIDA;
	}

	public void ativaTarefa() {
		this.statusAtivacao = StatusAtivacaoTarefa.ATIVA;
	}

	public void verificaSeJaEstaAtiva() {
		if (statusAtivacao == StatusAtivacaoTarefa.ATIVA) {
			throw APIException.build(HttpStatus.CONFLICT, "Tarefa já está ativa!");
		}

	}

	public void incrementaPomodoroSeStatusEmFoco(Tarefa tarefa, Usuario usuario) {
		log.info("[inicia] Tarefa - incrementaPomodoroSeStatusEmFoco");
		// Não precisava validar aqui se a tarefa pertence ao usuário,
		// porque isso já é feito na camada service quando chamo o detalhaTarefa
		this.pertenceAoUsuario(usuario);
		// Devo lançar excessão ou verificar e então alterar o usuário para foco?
		if (!Objects.equals(usuario.getStatus(), StatusUsuario.FOCO)) {
		    usuario.alteraStatusParaFoco(usuario.getIdUsuario());
		}
		// Nem vou verificar o status da tarefa, vou ativar ela direto.
		this.ativaTarefa();
		this.incrementaPomodoro();
		this.alteraStatusPorQtdeDePomodoros(tarefa, usuario);
		log.info("[finaliza] Tarefa - incrementaPomodoroSeStatusEmFoco");
	}

	private int incrementaPomodoro() {
		return ++contagemPomodoro;
	}

	private void alteraStatusPorQtdeDePomodoros(Tarefa tarefa, Usuario usuario) {
		log.info("[inicia] Tarefa - alteraStatusPorQtdeDePomodoros");
		int totalDePomodoros = tarefa.getContagemPomodoro();
		if (totalDePomodoros % 4 == 0) {
			usuario.mudaStatusParaPausaLonga(usuario.getIdUsuario());
		} else {
			usuario.alteraStatusParaPausaCurta(usuario.getIdUsuario());
		}
		log.info("[finaliza] Tarefa - alteraStatusPorQtdeDePomodoros");
	}

	public void ajustaPosicao(int novaPosicao) {
		this.posicao = novaPosicao;
	}

}
