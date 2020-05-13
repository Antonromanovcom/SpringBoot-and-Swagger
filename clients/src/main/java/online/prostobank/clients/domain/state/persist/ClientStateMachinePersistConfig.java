package online.prostobank.clients.domain.state.persist;

import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.persist.custom.CustomStateCustomMachinePersister;
import online.prostobank.clients.domain.state.persist.custom.interfaces.CustomMachinePersister;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientStateMachinePersistConfig {

    @Bean
    public CustomMachinePersister<ClientStates, ClientEvents, String> persister() {
        return new CustomStateCustomMachinePersister<>(new ClientStateMachinePersistProcessor());
    }
}
