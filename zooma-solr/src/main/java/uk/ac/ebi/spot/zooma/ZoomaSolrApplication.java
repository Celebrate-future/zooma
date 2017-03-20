package uk.ac.ebi.spot.zooma;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.solr.AnnotationRepository;

import java.util.ArrayList;
import java.util.Collection;

@SpringBootApplication
@EnableRabbit
@ComponentScan("uk.ac.ebi.spot")
public class ZoomaSolrApplication {
	public static void main(String[] args) {
		SpringApplication.run(ZoomaSolrApplication.class, args);
	}
}