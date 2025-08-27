package br.com.hackathon.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@ApplicationScoped
public class EventhubProducer {

    @Inject
    @Channel("simulacao")
    Emitter<String> emitter;

    @Inject
    ObjectMapper objectMapper;

    public void sendEvent(Object eventPayload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventPayload);
            log .info("Enviando evento para o canal 'api-simulacao'");
            emitter.send(jsonPayload);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar payload para JSON", e);
        } catch (Exception e) {
            log.error("Erro ao emitir evento", e);
        }
    }
}
