package online.prostobank.clients.utils;

import org.slf4j.Logger;

import javax.persistence.Embeddable;
import javax.persistence.PostLoad;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс для развешивания зависимостей в entity
 *
 * @author yv
 */
public class AutowireEntityListener {

	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AutowireEntityListener.class);

	static {
		LOG.info("Listener loaded");
	}

	@PostLoad
	public void onPostLoad(Object entity) {
		AutowireHelper.autowire(entity);
		for(Object n : getWireProperties(entity)) {
			AutowireHelper.autowire(n);	
		}
	}

	private Set<Object> getWireProperties(final Object entity) {

		final Class<?> entityClass = entity.getClass();
		final Set<Object> entityToWire = new HashSet<Object>();

		for (final Field field : entityClass.getDeclaredFields()) {
			if (field.getType().getAnnotation(Embeddable.class) != null) {
				field.setAccessible(true);
				try {
					entityToWire.add(field.get(entity));
					entityToWire.addAll(getWireProperties(field.get(entity)));
				} catch (IllegalAccessException e) {
					LOG.error("IAE:", e);
				}
			}
		}
		return entityToWire;
	}

	private static class Navigatable {

		final Object target;
		final Field field;

		Navigatable(final Object target, final Field field) {
			this.target = target;
			this.field = field;
		}

		@Override
		public String toString() {
			return "Navigatable{" + "target=" + target + '}';
		}
		
		
	}
}
