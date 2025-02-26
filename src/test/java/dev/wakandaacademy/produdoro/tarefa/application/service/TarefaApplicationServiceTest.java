package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.EditaTarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaUsuarioListReponse;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;

import static dev.wakandaacademy.produdoro.DataHelper.getTarefaRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    // @Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    // @MockBean
    @Mock
    TarefaRepository tarefaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    TokenService tokenService;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request, 0));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }

    @Test
    void deveEditarDescricaoTarefa() {
        Tarefa tarefa = DataHelper.createTarefa();
        Usuario usuario = DataHelper.createUsuario();
        EditaTarefaRequest editaTarefaRequest = DataHelper.createEditaTarefa();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        tarefaApplicationService.editaTarefa(usuario.getEmail(), tarefa.getIdTarefa(), editaTarefaRequest);
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
        assertEquals("TAREFA 1", tarefa.getDescricao());
    }

    @Test
    void deveConcluirTarefa() {
        Usuario usuario = DataHelper.createUsuario1();
        Tarefa tarefa = DataHelper.createTarefa();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());
        assertEquals(tarefa.getStatus(), StatusTarefa.CONCLUIDA);
    }

    @Test
    @DisplayName("Ativa tarefa - deve ativar tarefa")
    void ativaTarefaDeveAtivarTarefa() {
        UUID idTarefa = DataHelper.createTarefa().getIdTarefa();
        UUID idUsuario = DataHelper.createUsuario().getIdUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        Usuario usuario = DataHelper.createUsuario();
        String email = "email@gmail.com";
        when(usuarioRepository.buscaUsuarioPorEmail(email)).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        tarefaApplicationService.ativaTarefa(email, idTarefa);
        verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefa);
        verify(tarefaRepository, times(1)).desativaTarefaAtiva(idUsuario);
        assertEquals(StatusAtivacaoTarefa.ATIVA, tarefa.getStatusAtivacao());
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    @Test
    @DisplayName("Deve lançar excecao quando Id tarefa for inválido")
    void modificaOrdemDeUmaTarefaQuandoIdtarefaForInvalidoDeveLancarExcecao() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idtarefa = UUID.randomUUID();
        int novaPosicao = 1;

        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.empty());
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);

        APIException ex = assertThrows(APIException.class, () -> tarefaApplicationService
                .usuarioModificaOrdemDeUmaTarefa(usuario.getEmail(), idtarefa, novaPosicao));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusException());
        assertEquals("Tarefa não encontrada!", ex.getMessage());
    }

    @Test
    @DisplayName("Deve lançar excecao quando email usuario for inválido")
    void modificaOrdemDeUmaTarefaQuandoEmailForInvalidoDeveLancarExcecao() {
        Tarefa tarefa = DataHelper.createTarefa();
        int novaPosicao = 1;

        when(usuarioRepository.buscaUsuarioPorEmail(any()))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        APIException ex = assertThrows(APIException.class, () -> tarefaApplicationService
                .usuarioModificaOrdemDeUmaTarefa("testeinvalido@gmail.com", tarefa.getIdTarefa(), novaPosicao));
        verify(tarefaRepository, never()).buscaTarefaPorId(any());

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusException());
        assertEquals("Usuario não encontrado!", ex.getMessage());
    }

    @Test
    @DisplayName("Deve lançar excecao quando tarefa não perntence ao usuario")
    void modificaOrdemDeUmaTarefaQuandoTarefaNaoPertenceAoUsuarioDeveLancarExcecao() {
        Usuario usuario = DataHelper.createUsuarioInvalido();
        Tarefa tarefa = DataHelper.createTarefa();
        int novaPosicao = 1;

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));

        APIException ex = assertThrows(APIException.class, () -> tarefaApplicationService
                .usuarioModificaOrdemDeUmaTarefa(usuario.getEmail(), tarefa.getIdTarefa(), novaPosicao));
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(any());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(any());

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusException());
        assertEquals("Usuário não é dono da tarefa solicitada!", ex.getMessage());
    }

    @Test
    @DisplayName("Deleta tarefas concluídas")
    void deveDeletarTarefasConcluidas() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefasConcluidas = DataHelper.createTarefasConcluidas();
        List<Tarefa> tarefas = DataHelper.createListTarefa();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidas(any())).thenReturn(tarefasConcluidas);
        when(tarefaRepository.buscaTarefasPorUsuario(any())).thenReturn(tarefas);

        tarefaApplicationService.deletaTarefasConcluidas(usuario.getEmail(), usuario.getIdUsuario());

        verify(tarefaRepository, times(1)).deletaTarefasConcluidas(tarefasConcluidas);
        verify(tarefaRepository, times(1)).ajustaPosicaoDasTarefas(tarefas);
    }

    @Test
    @DisplayName("Não deleta tarefas concluídas, quando email inexistente")
    void naoDeveDeletarTarefasConcluidas_quandoEmailInexistente() {
        String usuarioEmail = "email_inexistente@email.com";
        when(usuarioRepository.buscaUsuarioPorEmail(any()))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuário não encontrado!"));

        assertThrows(APIException.class,
                () -> tarefaApplicationService.deletaTarefasConcluidas(usuarioEmail, UUID.randomUUID()));

        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuarioEmail);
    }

    @Test
    @DisplayName("Não deleta tarefas concluídas quando idUsuario inexistente")
    void naoDeveDeletarTarefasConcluidas_quandoIdUsuarioInexistente() {
        Usuario usuario = DataHelper.createUsuario();
        String usuarioEmail = usuario.getEmail();
        UUID idUsuarioInvalido = UUID.randomUUID();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any()))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        assertThrows(APIException.class,
                () -> tarefaApplicationService.deletaTarefasConcluidas(usuarioEmail, idUsuarioInvalido));

        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuarioEmail);
        verify(usuarioRepository, times(1)).buscaUsuarioPorId(idUsuarioInvalido);
    }

    @Test
    @DisplayName("Não deleta tarefas concluídas quando usuarioEmail não pertence ao usuário")
    void naoDeveDeletarTarefasConcluidas_quandoUsuarioEmailNaoPertenceAoUsuario() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idUsuario = usuario.getIdUsuario();
        Usuario usuarioTeste = DataHelper.createUsuarioTeste();
        String usuarioTesteEmail = usuarioTeste.getEmail();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioTeste);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);

        APIException ex = assertThrows(APIException.class,
                () -> tarefaApplicationService.deletaTarefasConcluidas(usuarioTesteEmail, idUsuario));

        assertEquals("Id não pertence ao usuário!", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusException());
    }

    @Test
    @DisplayName("Não deleta tarefas concluídas quando usuário não possui tarefas concluídas")
    void naoDevedeletarTarefasConcluidas_quandoUsuarioNaoPossuiTarefasConcluidas() {
        Usuario usuario = DataHelper.createUsuario();
        String usuarioEmail = usuario.getEmail();
        UUID idUsuario = usuario.getIdUsuario();
        List<Tarefa> tarefasConcluidas = List.of();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidas(any())).thenReturn(tarefasConcluidas);

        APIException ex = assertThrows(APIException.class,
                () -> tarefaApplicationService.deletaTarefasConcluidas(usuarioEmail, idUsuario));

        assertEquals("Usuário não possui nenhuma tarefa concluída!", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusException());
    }

    @Test
    void deveRetornarTarefasCadastradasPeloUsuarioLogado() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(usuario.getIdUsuario())).thenReturn(tarefas);
        List<TarefaUsuarioListReponse> listaTodasTarefasUsuario = tarefaApplicationService
                .listaTodasTarefasUsuario(usuario.getEmail(), usuario.getIdUsuario());

        assertEquals(8, listaTodasTarefasUsuario.size());
        verify(tarefaRepository, times(1)).buscaTarefasDoUsuario(usuario.getIdUsuario());
    }

    @Test
    void deveRetornarListaVaziaDoUsuario() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> listaVazia = new ArrayList<>();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(usuario.getIdUsuario())).thenReturn(listaVazia);
        List<TarefaUsuarioListReponse> listaVaziaDoUsuario = tarefaApplicationService
                .listaTodasTarefasUsuario(usuario.getEmail(), usuario.getIdUsuario());

        assertEquals(0, listaVaziaDoUsuario.size());
        verify(tarefaRepository, times(1)).buscaTarefasDoUsuario(usuario.getIdUsuario());
    }

    @Test
    public void deveLancarExcecaoQuandoUsuarioSolicitarTarefaENaoEstiverLogado() {
        UUID usuarioInexistente = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorId(usuarioInexistente))
                .thenThrow((APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!")));

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.listaTodasTarefasUsuario("email@exemplo.com", usuarioInexistente);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        assertEquals("Usuario não encontrado!", exception.getMessage());

        verify(usuarioRepository, times(1)).buscaUsuarioPorId(usuarioInexistente);
    }

    @Test
    @DisplayName("Deve incrementar pomodoro")
    void deveIncrementarPomodoro() {
        Usuario usuarioEmFoco = DataHelper.createUsuario1();
        Tarefa tarefa = DataHelper.createTarefa();
        int contagemPomodoroAntes = tarefa.getContagemPomodoro();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioEmFoco);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        tarefaApplicationService.incrementaPomodoro(usuarioEmFoco.getEmail(), tarefa.getIdTarefa());

        int contagemPomodoroDepois = tarefa.getContagemPomodoro();
        verify(tarefaRepository, times(1)).salva(any());
        assertEquals(contagemPomodoroAntes + 1, contagemPomodoroDepois);
    }

    @Test
    @DisplayName("Não deve incrementar pomodoro quando a tarefa não existe")
    void naoDeveIncrementarPomodoroQuandoTarefaNaoExiste() {
        Usuario usuarioEmFoco = DataHelper.createUsuario1();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioEmFoco);
        APIException e = assertThrows(APIException.class,
                () -> tarefaApplicationService.incrementaPomodoro(usuarioEmFoco.getEmail(), UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusException());
        assertEquals("Tarefa não encontrada!", e.getMessage());
    }

    @Test
    @DisplayName("Não deve incrementar pomodoro quando o usuário não é dono da tarefa")
    void naoDeveIncrementaPomodoroQuandoUsuarioNaoDonoDaTarefa() {
        Usuario usuarioNaoDonoDaTarefa = DataHelper.createUsuario2();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioNaoDonoDaTarefa);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        APIException e = assertThrows(APIException.class,
                () -> tarefaApplicationService.incrementaPomodoro(usuarioNaoDonoDaTarefa.getEmail(),
                        tarefa.getIdTarefa()));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusException());
        assertEquals("Usuário não é dono da tarefa solicitada!", e.getMessage());
    }

    @Test
    @DisplayName("Deve modificar a ordem da tarefa")
    void modificaOrdemDeUmaTarefa() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        int novaPosicao = 1;

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.buscaTarefasDoUsuario(any())).thenReturn(DataHelper.createListTarefa());

        tarefaApplicationService.usuarioModificaOrdemDeUmaTarefa(usuario.getEmail(), tarefa.getIdTarefa(), novaPosicao);
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(any());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(any());
    }

    @Test
    void deveExcluirTodasAsTarefasDoUsuarioLogado() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(usuario.getIdUsuario())).thenReturn(tarefas);
        tarefaApplicationService.deletaTodasTarefas(usuario.getEmail(), usuario.getIdUsuario());
        verify(tarefaRepository, times(1)).deletaTodasTarefasUsuario(tarefas);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExisteExcluirTarefas() {
        UUID usuarioInexistente = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorId(usuarioInexistente))
                .thenThrow((APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!")));

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.deletaTodasTarefas("email@exemplo.com", usuarioInexistente);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        assertEquals("Usuario não encontrado!", exception.getMessage());

        verify(usuarioRepository, times(1)).buscaUsuarioPorId(usuarioInexistente);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioTentarExcluirTarefaNaoLogado() {
        Usuario usuarioNaoLogado = DataHelper.createUsuario();

        when(tokenService.getUsuarioByBearerToken("email@exemplo.com"))
                .thenThrow((APIException.build(HttpStatus.UNAUTHORIZED,
                        "Usuário(a) não autorizado(a) para a requisição solicitada")));

        APIException exception = assertThrows(APIException.class, () -> {
            tokenService.getUsuarioByBearerToken("email@exemplo.com");
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Usuário(a) não autorizado(a) para a requisição solicitada",
                exception.getMessage());

        verify(usuarioRepository,
                times(0)).buscaUsuarioPorId(usuarioNaoLogado.getIdUsuario());

    }

    @Test
    void deveLancarExcecaoQuandoUsuarioTentarExcluirTarefaInexistente() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = Collections.emptyList();

        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(usuario.getIdUsuario())).thenReturn(tarefas);

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.deletaTodasTarefas(usuario.getEmail(), usuario.getIdUsuario());
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
        assertEquals("Usuário não possui tarefa(as) cadastrada(as)", exception.getMessage());
    }
}
