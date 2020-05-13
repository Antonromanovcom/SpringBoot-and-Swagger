package online.prostobank.clients.domain.state.config;

import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.AbstractSpringBootTest;
import online.prostobank.clients.api.dto.state.StateMachineDTO;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.resolver.StateMachineResolver;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;

import java.util.HashMap;
import java.util.List;


@Slf4j
public class ClientStateMachineConfigurationSpringBootTest extends AbstractSpringBootTest {


    @Autowired
    private StateMachineResolver<ClientStates, ClientEvents> resolver;


    private StateMachine<ClientStates, ClientEvents> stateMachine;

    @Before
    public void init() {
        HashMap<Object, Object> context = new HashMap<>();
        context.put("sms_code", true);
        context.put("taxNumber", "7842430448");
        context.put("confirmed", false);
        context.put("kyc", true);
        context.put("550", false);

        StateMachineDTO val1 = new StateMachineDTO(19L, "First", ClientStates.NEW_CLIENT, "7842430448", context);
        stateMachine = resolver.getStateMachine(String.valueOf(val1.getId()), val1.getState(), context);

    }


    @Test
    public void testingAction() {

        List availableEvents = resolver.getAvailableTransition(this.stateMachine);

        System.out.println(availableEvents);

       this.stateMachine.sendEvent(ClientEvents.CE_SMS);

        Assert.assertEquals(stateMachine.getState().getId(), ClientStates.CONTACT_INFO_CONFIRMED);

        availableEvents = resolver.getAvailableTransition(this.stateMachine);

        System.out.println(availableEvents);

        this.stateMachine.sendEvent(ClientEvents.CHECKS);

        Assert.assertEquals(stateMachine.getState().getId(), ClientStates.CHECK_LEAD);

        availableEvents = resolver.getAvailableTransition(this.stateMachine);

        System.out.println(availableEvents);

        this.stateMachine.sendEvent(ClientEvents.CHECKS_DONE);

        Assert.assertEquals(stateMachine.getState().getId(), ClientStates.CHECK_LEAD);

    }


}
