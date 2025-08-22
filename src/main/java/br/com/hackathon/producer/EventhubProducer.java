package br.com.hackathon.producer;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class EventhubProducer {

    @Channel("credito-api")
    Emitter<String> emmiter;

    public void send(String payload) {
        emmiter.send(payload);
    }
}
