package dev.wakandaacademy.produdoro.usuario.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {
 
    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;
    
    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveMudarStatusParaPausaLonga(){
        Usuario usuario = DataHelper.createUsuario();
        
        when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());

        assertEquals(StatusUsuario.PAUSA_LONGA,usuario.getStatus());
        verify(usuarioRepository,times(1)).salva(usuario);
    }

    @Test
    void naoDeveMudarStatusParaPausaLonga(){
        Usuario usuario = DataHelper.createUsuario();
        
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());

        APIException e = assertThrows(APIException.class, usuario::validaStatusPausaLonga);
        assertEquals("Usuário Ja esta em PAUSA LONGA!",e.getMessage());
        assertEquals(HttpStatus.CONFLICT,e.getStatusException());
    }

    @Test
    void naoDeveMudarStatusParaPausaLongaQuandoCredencialInvalida(){
        Usuario usuario = DataHelper.createUsuario();
        UUID idInvalido = UUID.fromString("416e5778-a873-45c3-928a-8e3f2f8bf3d");
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        APIException e = assertThrows(APIException.class,() -> usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), idInvalido));
        assertEquals(HttpStatus.UNAUTHORIZED,e.getStatusException());
    }
}
