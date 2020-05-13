package online.prostobank.clients.connectors.abs;

import club.apibank.connectors.kub.AbsConnector.AbsRestOperations;
import club.apibank.connectors.kub.data.dto.request.AbsServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * @author yv
 */

@Slf4j
@Component
public class AbsRestOperationsImpl implements AbsRestOperations {

    @Override
    public <RESP> RESP doRequest(String url, AbsServiceRequest absServiceRequest, Class<RESP> respClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.setAccept(Collections.singletonList(MediaType.TEXT_XML));

        HttpEntity http = new HttpEntity<>(absServiceRequest, headers);

        // dev ssh tunnel
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals("localhost"));

	    RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        Jaxb2RootElementHttpMessageConverter converter = new Jaxb2RootElementHttpMessageConverter();
        converter.canRead(respClass, MediaType.APPLICATION_XML);
        converter.canRead(AbsServiceRequest.class, MediaType.TEXT_XML);
        converter.canWrite(respClass, MediaType.APPLICATION_XML);
        converter.canWrite(AbsServiceRequest.class, MediaType.TEXT_XML);

        restTemplate.setMessageConverters(Collections.singletonList(converter));

        ResponseEntity<RESP> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, http, respClass);
            log.debug("Обмен с АБС: {}", responseEntity.getStatusCodeValue());
        } catch (HttpStatusCodeException e) {
            log.error("Ошибка обмена с АБС: {}, ответ вернулся: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return null;
        }
        return responseEntity.getBody();
    }

    @Override
    public <REQ, RESP> RESP doRequest(String url, REQ absServiceRequest, Class<RESP> respClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML + ";charset=UTF-8");
        headers.setAccept(Collections.singletonList(MediaType.TEXT_XML));
        HttpEntity http = new HttpEntity<>(absServiceRequest, headers);

        // dev ssh tunnel
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals("localhost"));

	    RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        MappingJackson2XmlHttpMessageConverter converter = new MappingJackson2XmlHttpMessageConverter();
        converter.canRead(absServiceRequest.getClass(), MediaType.TEXT_XML);
        converter.canRead(absServiceRequest.getClass(), MediaType.APPLICATION_XML);
        converter.canRead(respClass, MediaType.APPLICATION_XML);
        converter.canRead(respClass, MediaType.TEXT_XML);

        converter.canWrite(absServiceRequest.getClass(), MediaType.TEXT_XML);
        converter.canWrite(absServiceRequest.getClass(), MediaType.APPLICATION_XML);
        converter.canWrite(respClass, MediaType.APPLICATION_XML);
        converter.canWrite(respClass, MediaType.TEXT_XML);

        restTemplate.setMessageConverters(Collections.singletonList(converter));

        ResponseEntity<RESP> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, http, respClass);
            log.info("abs kub exchange completed with status code: {}", responseEntity.getStatusCodeValue());
        } catch (HttpStatusCodeException e) {
            log.warn("There is an error occured during ABS request: status-code: {}, response-body: {}",
                     e.getRawStatusCode(), e.getResponseBodyAsString());
            return null;
        }
        return responseEntity.getBody();
    }
}
