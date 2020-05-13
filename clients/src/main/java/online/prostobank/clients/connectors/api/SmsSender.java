/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package online.prostobank.clients.connectors.api;

import javax.validation.constraints.NotEmpty;

/**
 * Отправка СМС
 * @author yv
 */
public interface SmsSender {
    /**
     * @param number - номер телефона реципиента
     * @param text - содержание сообщения
     * @return результат операции
     */
    boolean send(
            @NotEmpty(message = "Парамерт не задан") String number,
            @NotEmpty(message = "Парамерт не задан") String text
    );
}
