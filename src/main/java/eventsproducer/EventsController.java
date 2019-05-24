package eventsproducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventsController {

    @Value(value = "${kafka.topic}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private ListenableFutureCallback<SendResult<String,String>> producerResultCallback;

    private static ObjectWriter objectWriter = new ObjectMapper().writer();

    @PostMapping(path = "/events", consumes = "application/json", produces = "application/json" )
    public ResponseEntity<String> produceEvents(@RequestBody SensorEvents event) throws JsonProcessingException {
        String eventValue = objectWriter.writeValueAsString(event);
        kafkaTemplate.send(topicName, objectWriter.writeValueAsString(event)).addCallback(producerResultCallback);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventValue);
    }
}
