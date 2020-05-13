/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package online.prostobank.clients.connectors.api;

import online.prostobank.clients.domain.Email;
import online.prostobank.clients.domain.SystemEmail;

import javax.validation.constraints.NotNull;

/**
 * Отправка {@link Email}
 * @author yv
 */
public interface EmailConnector {

	/**
	 * Отправить сообщение
	 * @param m - {@link Email}
	 */
    void send(@NotNull(message = "Парамерт не задан") Email m);

	/**
	 * Отпраавка тригера события "по умолчанию"
	 * @param m - тригер события
	 */
	default void sendTrigger(@NotNull(message = "Парамерт не задан") Email m) {
		throw new UnsupportedOperationException("Используйте EmarsysEmailConnector");
	}

	/**
	 * Отправка сообщения "по умолчанию"
	 * @param m - {@link Email}
	 */
	default void send(@NotNull(message = "Парамерт не задан") SystemEmail m) {
		throw new UnsupportedOperationException("Тип не поддерживается");
	}
}
