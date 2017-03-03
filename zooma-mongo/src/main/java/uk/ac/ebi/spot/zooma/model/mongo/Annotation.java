package uk.ac.ebi.spot.zooma.model.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by olgavrou on 02/08/2016.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "annotations")
public class Annotation {
    @Id
    @CreatedBy
    private String id;
    @NonNull
    private BiologicalEntity biologicalEntities;
    @NonNull
    private Property property;
    @NonNull
    private Collection<String> semanticTag;
    @NonNull
    private MongoAnnotationProvenance provenance;
    @NonNull
    private boolean batchLoad;
    private Float quality;

    public float getQuality() {
        if (this.quality == null){
            this.quality = calculateAnnotationQuality();
        }
        return quality;
    }

    /**
     * Quality can not be set manually.
     * It can only calculated based on the annotation's {@link AnnotationProvenance}
     */
    public void setQuality(){
        this.quality = calculateAnnotationQuality();
    }

    /**
     * quality is caluclated based on provenance.
     * So we are making sure that it will be calculated once the provenance is set
     */
    public void setProvenance(MongoAnnotationProvenance provenance){
        this.provenance = provenance;
        this.quality = calculateAnnotationQuality();
    }

    /**
     * Returns a float value that is the quality score for the given annotation.
     * <p/>
     * This score is evaluated by an algorithm that considers: <ul> <li>Source (e.g. Atlas, AE2, ZOOMA)</li>
     * <li>Evidence (Manually created, Inferred, etc.)</li> <li>Creator - Who made this annotation?</li> <li>Time of
     * creation - How recent is this annotation?</li> </ul>
     */
    private float calculateAnnotationQuality() throws IllegalArgumentException {

        if (this.provenance == null){
            throw new IllegalArgumentException("Provenance isn't set yet, can not calculate annotation provenance");
        }
        // evidence is most important factor, invert so ordinal 0 gets highest score
        int evidenceScore = MongoAnnotationProvenance.Evidence.values().length - this.provenance.getEvidence().ordinal();
        // creation time should then work backwards from most recent to oldest
        long age = this.provenance.getAnnotatedDate().toLocalTime().toNanoOfDay();

        return (float) (evidenceScore + Math.log10(age));

    }

    @Override
    public String toString() {

        StringJoiner semTags = new StringJoiner(",");
        for(String s : getSemanticTag()){
            semTags.add("\"" + s + "\"");
        }

        String string = "{" +
                "\"provenance\" : {" +
                "\"evidence\" : \"" + getProvenance().getEvidence().toString() + "\"," +
                "\"annotatedDate\" : \"" + getProvenance().getAnnotatedDate().toString() + "\"," +
                "\"generatedDate\" : \"" + getProvenance().getGeneratedDate().toString() + "\"," +
                "\"accuracy\" : \"" + getProvenance().getAccuracy().toString() + "\"," +
                "\"generator\" : \"ZOOMA" + "\"," +
                "\"source\" : { " +
                "\"name\" : \"" + getProvenance().getSource().getName().toString() + "\"," +
                "\"topic\" : \"" + getProvenance().getSource().getTopic().toString() + "\"," +
                "\"type\" : \"" + getProvenance().getSource().getType() + "\"," +
                "\"uri\" : \"" + getProvenance().getSource().getUri() + "\"" +
                "}," +
                "\"annotator\" : \"" + getProvenance().getAnnotator() + "\"" +
                "}," +
                "\"biologicalEntities\" : {" +
                "\"studies\" : {" +
                "\"study\" : \"" + getBiologicalEntities().getStudies().getStudy() + "\"" +
                "}," +
                "\"bioEntity\" : \"" + getBiologicalEntities().getBioEntity() + "\"" +
                "}," +
                "\"semanticTag\" : [" + semTags.toString() + "]," +
                "\"quality\" : \"" + getQuality() + "\"," +
                "\"property\" : {" +
                "\"propertyType\" : \"" + getProperty().getPropertyType() + "\"," +
                "\"propertyValue\" : \"" + getProperty().getPropertyValue() + "\"" +
                "}" +
                "}";

        return string;
    }

    /**
     * Makes the annotation into a simple map
     *
     * @return all the properties of the annotation in a simple map
     */
    public Map<String, Object> toSimpleMap(){
        Map<String, Object> map = new HashMap<>();

        map.put("id", getId());
        map.put("quality", getQuality());
        map.put("propertyType", getProperty().getPropertyType());
        map.put("propertyValue", getProperty().getPropertyValue());
        map.put("semanticTag", getSemanticTag());
        map.put("bioEntity", getBiologicalEntities().getBioEntity());
        map.put("study", getBiologicalEntities().getStudies().getStudy());
        map.put("sourceName", getProvenance().getSource().getName());
        map.put("sourceTopic", getProvenance().getSource().getTopic());
        map.put("sourceType", getProvenance().getSource().getType());
        map.put("sourceUri", getProvenance().getSource().getUri());
        map.put("accuracy", getProvenance().getAccuracy());
        map.put("evidence", getProvenance().getEvidence());
        map.put("annotatedDate", getProvenance().getAnnotatedDate());
        map.put("generatedDate", getProvenance().getGeneratedDate());
        map.put("generator", getProvenance().getGenerator());
        map.put("annotator", getProvenance().getAnnotator());

        return map;
    }
}
