package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import dev.wakandaacademy.produdoro.handler.APIException;

import org.junit.jupiter.api.BeforeEach;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioApplicationServiceTest {

    @InjectMocks
    private UsuarioApplicationService usuarioApplicationService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioMock;
    private final String usuarioEmail = "usuario@teste.com";
    private final UUID idUsuario = UUID.randomUUID();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        usuarioMock = mock(Usuario.class);
        when(usuarioRepository.buscaUsuarioPorEmail(usuarioEmail)).thenReturn(usuarioMock);
    }

    @Test
    void alteraStatusParaFoco_DeveAlterarStatusParaFoco() {
        //cenario
        doNothing().when(usuarioMock).validaUsuarioPorId(idUsuario);

        //acao
        usuarioApplicationService.mudaStatusParaFoco(usuarioEmail, idUsuario);

        //verificacao
        verify(usuarioRepository).buscaUsuarioPorEmail(usuarioEmail);
        verify(usuarioMock).alteraStatusParaFoco(idUsuario);
        verify(usuarioRepository).salva(usuarioMock);
    }

    @Test
    void alteraStatusParaFoco_DeveLancarExcecaoUsuarioNaoEncontrado() {
        //cenario
        UUID idUsuarioNaoEncontrado = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail("email@email.com")).thenReturn(null);
        doThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"))
                .when(usuarioRepository).buscaUsuarioPorId(idUsuarioNaoEncontrado);

        //acao
        APIException exception = assertThrows(APIException.class,
                () -> usuarioApplicationService.mudaStatusParaFoco("email@email.com", idUsuarioNaoEncontrado));

        //verificacao
        assertEquals("Usuario não encontrado!", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail("email@email.com");
        verify(usuarioRepository, times(1)).buscaUsuarioPorId(idUsuarioNaoEncontrado);
    }

    @Test
    void alteraStatusParaFoco_UsuarioJaEstaEmFoco() {
        //cenario
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaFoco(usuario.getEmail(), usuario.getIdUsuario());

        //acao
        APIException exception = assertThrows(APIException.class, usuario::verificaStatusFoco);

        //verificacao
        assertEquals("Usuário já está em foco!", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail("email@email.com");
    }
}
