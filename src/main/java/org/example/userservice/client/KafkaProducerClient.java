package org.example.userservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerClient {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    public void sendMessage(String topic, String message){
        kafkaTemplate.send(topic, message);
    }
}
