package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import dev.wakandaacademy.produdoro.handler.APIException;

import org.junit.jupiter.api.BeforeEach;

import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class UsuarioApplicationServiceTest {

    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

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
    @Test
    void deveMudarStatusParaPausaCurta(){
        //cenario
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);

        //acao
        usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(),usuario.getIdUsuario());

        //verificacao
        assertEquals(StatusUsuario.PAUSA_CURTA,usuario.getStatus());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, times(1)).buscaUsuarioPorId(usuario.getIdUsuario());
        verify(usuarioRepository, times(1)).salva(usuario);
    }
    @Test
    void deveLancaExcecaoSeUsuarioJaEstaEmPausaCurta(){
        //cenario
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaCurta(usuario.getEmail(),usuario.getIdUsuario());

        //acao
        APIException exception = assertThrows(APIException.class, usuario::verificaStatusPausaCurta);

        //verificacao
        assertEquals("já está em pausa curta", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail("email@email.com");
    }
    @Test
    void deveMudarStatusParaPausaLonga(){
        Usuario usuario = DataHelper.createUsuario1();

        when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());

        assertEquals(StatusUsuario.PAUSA_LONGA,usuario.getStatus());
        verify(usuarioRepository,times(1)).salva(usuario);
    }

    @Test
    void naoDeveMudarStatusParaPausaLonga(){
        Usuario usuario = DataHelper.createUsuario1();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());

        APIException e = assertThrows(APIException.class, usuario::validaStatusPausaLonga);
        assertEquals("Usuário Ja esta em PAUSA LONGA!",e.getMessage());
        assertEquals(HttpStatus.CONFLICT,e.getStatusException());
    }

    @Test
    void naoDeveMudarStatusParaPausaLongaQuandoCredencialInvalida(){
        Usuario usuario = DataHelper.createUsuario1();
        UUID idInvalido = UUID.fromString("416e5778-a873-45c3-928a-8e3f2f8bf3d");
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        APIException e = assertThrows(APIException.class,() -> usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), idInvalido));
        assertEquals(HttpStatus.UNAUTHORIZED,e.getStatusException());
    }
}