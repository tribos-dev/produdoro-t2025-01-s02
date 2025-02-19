package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

}
