package online.prostobank.clients.utils.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@RequiredArgsConstructor
public class AttachmentHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private final String hostHeaderValue;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException
    {
        HttpHeaders headers = request.getHeaders();
        headers.remove(HttpHeaders.HOST);
        headers.add(HttpHeaders.HOST, hostHeaderValue);

        return execution.execute(request, body);
    }
}
