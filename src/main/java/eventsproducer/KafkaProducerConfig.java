package eventsproducer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private static String JAAS_CONFIG_FORMAT = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";

    @Value(value = "${kafka.bootstrapServers}")
    private String bootstrapServers;

    @Value(value = "${kafka.username}")
    private String username;

    @Value(value = "${kafka.password}")
    private String password;

    @Bean
    public ProducerFactory<String, String> eventsProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,"SASL_PLAINTEXT");
        configProps.put("sasl.mechanism","SCRAM-SHA-256");
        configProps.put("sasl.jaas.config",String.format(JAAS_CONFIG_FORMAT,username,password));
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(eventsProducerFactory());
    }

    @Bean
    public ListenableFutureCallback<SendResult<String,String>> producerResultCallback(){
        return new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Sent message=[" + result.getProducerRecord().value()+
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message: " + ex.getMessage());
            }
        };
    }
}
