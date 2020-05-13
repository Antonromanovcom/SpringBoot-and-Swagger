package online.prostobank.clients.services;

import online.prostobank.clients.domain.PropertyEntity;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;
import online.prostobank.clients.domain.repository.PropertyRepository;
import online.prostobank.clients.services.interfaces.DbPropertiesServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DbPropertiesService implements DbPropertiesServiceI {
    private static final Logger log = LoggerFactory.getLogger(DbPropertiesService.class);

    private final PropertyRepository repository;

    public DbPropertiesService(@Nonnull PropertyRepository repository) {
        this.repository = repository;
    }

    @Override
    public @Nonnull PropertyEntity submitNewProperty(@Nonnull String key,
                                                     @Nonnull String value) throws PropertyServiceException {
            final boolean notExists = repository.isExists(key) == 0;
            if (notExists) {
                return repository.save(new PropertyEntity(key, value));
            } else {
                final String errprMessage = "Unable to submit new property, property already exists";
                log.error(errprMessage);
                throw new PropertyServiceException(errprMessage);
            }
    }

    @Override
    public @Nonnull PropertyEntity getPropertyByKey(@Nonnull String key) throws PropertyServiceException {
        try {
            return Optional.of(repository)
                    .map(o -> o.getPropertyByKey(key))
                    .orElseThrow(() -> new PropertyServiceException("Property not found"));
        } catch (PropertyServiceException e) {
            throw new PropertyServiceException("Unable to retrieve property from database");
        }
    }

    @Override
    public @Nonnull PropertyEntity updateProperty(@Nonnull String key,
                                                  @Nonnull String value) throws PropertyServiceException {
        try {
            final PropertyEntity property = repository.getPropertyByKey(key);
            property.setValue(value);
            repository.save(property);
            return property;
        } catch (Exception e ) {
            throw new PropertyServiceException("Unable to retrieve property from database");
        }
    }


    @Override
    public @Nonnull List<PropertyEntity> getAllProperties(@Nonnull Long limit) throws PropertyServiceException {
        try {
            return repository.getAllProperties()
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e ) {
            throw new PropertyServiceException("Unable to retrieve property from database");
        }
    }
}
