package dev.wakandaacademy.produdoro.tarefa.application.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class EditaTarefaRequest {
    @NotBlank(message = "Campo descrição tarefa não pode estar vazio")
    @Size(min = 3, max = 250, message = "Caracteres no minimo 3 e máximo 250")
    private String descricao;

    public EditaTarefaRequest(String descricao) {
        this.descricao = descricao;
    }
}
