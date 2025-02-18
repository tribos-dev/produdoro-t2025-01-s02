package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
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
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
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
}
