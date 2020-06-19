package eventsproducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;


import java.io.UnsupportedEncodingException;
import java.util.Optional;

@RestController
@EnableJms
public class EventsController {

    Logger log = LoggerFactory.getLogger(EventsController.class);

    @Value(value = "${kafka.topic}")
    private String topicName;

    @Value(value = "${schema-registry.cloud.urls}")
    private String[] schemaRegistryCloudURLs;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ListenableFutureCallback<SendResult<String,String>> producerResultCallback;

    private static ObjectWriter objectWriter = new ObjectMapper().writer();

    @PostMapping(path = "/events", consumes = "application/json", produces = "application/json" )
    public ResponseEntity<String> produceEvents(@RequestBody SensorEvents event) throws JsonProcessingException {
        String eventValue = objectWriter.writeValueAsString(event);
        kafkaTemplate.send(topicName, eventValue).addCallback(producerResultCallback);
        // This returns success regardless of async result from kafkaTemplate.send
        return ResponseEntity.status(HttpStatus.CREATED).body(eventValue);
    }

    @PostMapping(path = "/mqmessage/{destinationName}", consumes = "application/json", produces = "application/json" )
    public ResponseEntity<String> postMQMessage(@RequestBody String mqTextMessage,
                                                @PathVariable("destinationName") String destinationName,
                                                @RequestParam("encoding") Optional<String> encoding)
            throws UnsupportedEncodingException {

        //setMessageConverter(encoding);
        if(encoding.isPresent()) {
            byte[] mqPayload = mqTextMessage.getBytes(encoding.get());
            jmsTemplate.convertAndSend(destinationName, mqPayload);
        }
        else{
            jmsTemplate.convertAndSend(destinationName, mqTextMessage);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(path = "/v1/metadata/schemaRegistryUrls")
    public ResponseEntity<String[]> getSchemaRegistryURL(){
        log.info("Going to return Schema Registry URLS: {}", schemaRegistryCloudURLs);
        return ResponseEntity.ok().body(schemaRegistryCloudURLs);
    }

    private void setMessageConverter(@RequestParam("encoding") Optional<String> encoding) {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTargetType(MessageType.TEXT);
        messageConverter.setEncoding("UTF-8");

        if(encoding.isPresent()){
            messageConverter.setTargetType(MessageType.BYTES);
            messageConverter.setEncoding(encoding.get());
        }

        jmsTemplate.setMessageConverter(messageConverter);
    }
}
