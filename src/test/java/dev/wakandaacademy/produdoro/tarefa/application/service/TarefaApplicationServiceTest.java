package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaUsuarioListReponse;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;
    @Mock
    UsuarioRepository usuarioRepository;
	
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
    void deveConcluirTarefa() {
        Usuario usuario = DataHelper.createUsuario1();
        Tarefa tarefa = DataHelper.createTarefa();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());
        assertEquals(tarefa.getStatus(), StatusTarefa.CONCLUIDA);
    }

    void ativaTarefaDeveAtivarTarefa(){
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

		APIException ex = assertThrows(APIException.class, () -> tarefaApplicationService.usuarioModificaOrdemDeUmaTarefa(usuario.getEmail(), idtarefa, novaPosicao));
        
		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusException());
        assertEquals("Tarefa não encontrada!", ex.getMessage());
	}
	
	@Test
	@DisplayName("Deve lançar excecao quando email usuario for inválido")
	void modificaOrdemDeUmaTarefaQuandoEmailForInvalidoDeveLancarExcecao() {
		Tarefa tarefa = DataHelper.createTarefa();
		int novaPosicao = 1;

		when(usuarioRepository.buscaUsuarioPorEmail(any())).thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

		APIException ex = assertThrows(APIException.class, () -> tarefaApplicationService.usuarioModificaOrdemDeUmaTarefa("testeinvalido@gmail.com", tarefa.getIdTarefa(), novaPosicao));
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

		APIException ex = assertThrows(APIException.class, () -> tarefaApplicationService.usuarioModificaOrdemDeUmaTarefa(usuario.getEmail(), tarefa.getIdTarefa(), novaPosicao));
		verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(any());
		verify(tarefaRepository, times(1)).buscaTarefaPorId(any());

		assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusException());
        assertEquals("Usuário não é dono da Tarefa solicitada!", ex.getMessage());
	}

    @Test
    void deveRetornarTarefasCadastradasPeloUsuarioLogado() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(usuario.getIdUsuario())).thenReturn(tarefas);
        List<TarefaUsuarioListReponse> listaTodasTarefasUsuario = tarefaApplicationService.listaTodasTarefasUsuario(usuario.getEmail(), usuario.getIdUsuario());

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
        List<TarefaUsuarioListReponse> listaVaziaDoUsuario = tarefaApplicationService.listaTodasTarefasUsuario(usuario.getEmail(), usuario.getIdUsuario());

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
	@DisplayName("Deve modificar a ordem da tarefa")
	void modificaOrdemDeUmaTarefa(){
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

}
