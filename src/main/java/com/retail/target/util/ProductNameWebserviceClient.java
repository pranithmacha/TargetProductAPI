package com.retail.target.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.target.data.ProductFromService;
import com.retail.target.errors.ConnectionException;
import com.retail.target.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Created by pranith macha on 12/2/17.
 */

@Service
public class ProductNameWebserviceClient {

    private RestTemplate restTemplate;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String get(String url) {
        restTemplate = new RestTemplate();
        String result = "";
        try {
            result = restTemplate.getForObject(url, String.class);
        } catch (HttpClientErrorException httpError) {
            log.error("errror while getting response from name service", httpError);
            if (httpError.getStatusCode() == HttpStatus.NOT_FOUND)
                throw new ResourceNotFoundException("product not found in name service");
        } catch (RestClientException ex) {
            log.error("error while getting name from name ws", ex);
            throw new ConnectionException("connection exception");
        }
        return result;
    }

    public ProductFromService getProduct(Long productId) {
        ObjectMapper mapper = new ObjectMapper();
        ProductFromService productFromService = new ProductFromService();
        String rest = get(Constants.PRODUCT_NAME_WS_URL + productId.toString());
        try {
            productFromService = mapper.readValue(rest, ProductFromService.class);
            if (productFromService.getName() == null || productFromService.getName().isEmpty())
                throw new IOException("product not found in name ws");
            log.info("got product name from name ws for id: " + productId.toString());
        } catch (IOException ex) {
            log.error("could not parse JSON response from name ws " + productFromService.toString(), ex);
            throw new ResourceNotFoundException("could not fetch name");
        }
        return productFromService;
    }
}

