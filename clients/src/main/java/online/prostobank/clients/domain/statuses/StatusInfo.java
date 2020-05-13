/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package online.prostobank.clients.domain.statuses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static online.prostobank.clients.security.UserRolesConstants.*;

/**
 * Описание графа состояний для заявки
 *
 * @author yv
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StatusInfo {

	// соответствуют ролям из KeyCloak в реалме ProstoBank!
	String POS_ADMIN = ROLE_POS_ADMIN;
	String POS_FRONT = ROLE_POS_FRONT;
	String POS_ADMIN_HOME = ROLE_POS_ADMIN_HOME;
	String POS_FRONT_HOME = ROLE_POS_FRONT_HOME;
	String POS_OUTER_API_ADMIN = ROLE_POS_OUTER_API_ADMIN;
	String POS_OUTER_API_MANAGER = ROLE_POS_OUTER_API_MANAGER;
	String POS_CONSULTANT = ROLE_POS_CONSULTANT;

//	String POS_BACK = "pos-back";
//	String POS_COURIER = "pos-courier";
//	String POS_FRONT_HUNTER = "pos-front-hunter";
//	String POS_ADMIN_HUNTER = "pos-admin-hunter";


	int value() default 0;

	int[] next() default 0;

	String[] tags() default "";

	String name() default "";

	boolean decline() default false;

	boolean error() default false;

	String[] allowedTo() default {POS_ADMIN, POS_FRONT,
//			POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
			POS_FRONT_HOME, POS_ADMIN_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN, POS_CONSULTANT};

	// APIKUB-646 домашний колцентр. необходимо создать роли, которые могут редактировать заявки в нужных статусах (до "В работе менеджера")
	String[] editable() default {POS_ADMIN, POS_FRONT,
//			POS_BACK, POS_FRONT_HUNTER, POS_ADMIN_HUNTER,
			POS_FRONT_HOME, POS_ADMIN_HOME, POS_OUTER_API_MANAGER, POS_OUTER_API_ADMIN};
}
