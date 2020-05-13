package online.prostobank.clients.domain;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.client.PassportDTO;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Embeddable
public class PersonValue extends PersonValueEntity {

    public PersonValue(){}

    public PersonValue (PassportDTO passportDTO) {
        super(passportDTO);
    }

	public List<String> diff(@Nonnull PersonValue other) {
		log.debug("Calculating PersonValue diff");
		List<String> diff = new ArrayList<>();
		findDiff(other, PersonValue::getSer, "серия: '%s' -> '%s'").ifPresent(diff::add);
		findDiff(other, PersonValueEntity::getNum, "номер: '%s' -> '%s'").ifPresent(diff::add);
		findDiff(other, PersonValueEntity::getDob, "дата рождения: '%s' -> '%s'").ifPresent(diff::add);
		findDiff(other, PersonValueEntity::getPob, "место рождения: '%s' -> '%s'").ifPresent(diff::add);
		findDiff(other, PersonValueEntity::getIssuerCode, "код подразделения: '%s' -> '%s'").ifPresent(diff::add);
		findDiff(other, PersonValueEntity::getDoi, "дата выдачи: '%s' -> '%s'").ifPresent(diff::add);
		findDiff(other, PersonValueEntity::getIssuer, "кем выдан: '%s' -> '%s'").ifPresent(diff::add);
		findDiff(other, PersonValueEntity::getSnils, "снилс: '%s' -> '%s'").ifPresent(diff::add);
		return diff;
	}

	private Optional<String> findDiff(PersonValue second, Function<PersonValue, ?> getter, String text) {
		return Optional.of(second)
				.filter(it -> !Objects.equals(getter.apply(it), getter.apply(this)))
				.map(it -> String.format(text,
						Objects.toString(this, ""),
						Objects.toString(it, "")));
	}

	@Override
	public PersonValue clone() {
		log.debug("Cloning PersonValue");
		try {
			return (PersonValue) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e.getLocalizedMessage(), e);
			throw new RuntimeException("got unexpected CloneNotSupportedException");
		}
	}
}
