package online.prostobank.clients.services.interfaces;

import online.prostobank.clients.domain.PropertyEntity;
import online.prostobank.clients.domain.exceptions.PropertyServiceException;

import javax.annotation.Nonnull;
import java.util.List;

public interface DbPropertiesServiceI {
    @Nonnull
    PropertyEntity submitNewProperty(@Nonnull String key,
                                     @Nonnull String value) throws PropertyServiceException;

    @Nonnull
    PropertyEntity getPropertyByKey(@Nonnull String key) throws PropertyServiceException;

    @Nonnull
    PropertyEntity updateProperty(@Nonnull String key,
                                  @Nonnull String value) throws PropertyServiceException;

    @Nonnull
    List<PropertyEntity> getAllProperties(@Nonnull Long limit) throws PropertyServiceException;
}
