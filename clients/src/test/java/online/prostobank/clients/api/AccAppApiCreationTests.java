package online.prostobank.clients.api;

import online.prostobank.clients.AbstractSpringBootTest;
import online.prostobank.clients.api.dto.anketa.ContactInfoVerifyDTO;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

/**
 *
 * @author yurij
 */
public class AccAppApiCreationTests extends AbstractSpringBootTest {
    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();


    @Test
    @Ignore
    public void testSaveInitial() {
        ContactInfoVerifyDTO dto = new ContactInfoVerifyDTO();
        dto.setCity("Москва");
        dto.setPartner("PROSTO");
        dto.setPhone("9096667788");
        dto.setUserId("1");

        HttpEntity<ContactInfoVerifyDTO> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                        createURLWithPort("/api/main/bids/contact-info-verify"),
                        HttpMethod.POST, entity, String.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseEntity<String> response2 = restTemplate.exchange(
                        createURLWithPort("/api/main/bids/contact-info-verify"),
                        HttpMethod.POST, entity, String.class);
        Assert.assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    @Ignore
    public void testSaveVerify() {
        ContactInfoVerifyDTO verifyDto = new ContactInfoVerifyDTO();
        verifyDto.setCity("Москва");
        verifyDto.setPartner("PROSTO");
        verifyDto.setPhone("9096667799");
        verifyDto.setUserId("1");

        HttpEntity<ContactInfoVerifyDTO> verify = new HttpEntity<>(verifyDto, headers);

    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private static String LAST_CC;

}
