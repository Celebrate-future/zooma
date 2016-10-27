package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.spot.model.Annotation;
import uk.ac.ebi.spot.model.MongoAnnotation;
import uk.ac.ebi.spot.service.SaveService;
import uk.ac.ebi.spot.services.MongoAnnotationRepositoryService;

/**
 * Created by olgavrou on 19/09/2016.
 */
@Controller
public class Save2Mongo implements SaveService<Annotation>{

    @Autowired
    private MongoAnnotationRepositoryService mongoAnnotationRepositoryService;

    public void save(Annotation annotation){

        MongoAnnotation mongoAnnotation = (MongoAnnotation) annotation;
        mongoAnnotationRepositoryService.save(mongoAnnotation);
    }

}