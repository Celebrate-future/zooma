package uk.ac.ebi.spot.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by olgavrou on 25/10/2016.
 */
public class AnnotationProperty {

    private String text;

    private List<Property> properties;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Property> getProperties() {
        properties = new ArrayList<>();
        ArrayList<String> textLines = new ArrayList<>(Arrays.asList(text.split("\r\n")));

        for (String textLine : textLines) {
            String[] line = textLine.split("\t");
            if (line.length > 0) {
                String propertyValue = line[0];
                if (line.length > 1) {
                    String propertyType = line[1];
                    TypedProperty typedProperty = new SimpleTypedProperty(propertyType, propertyValue);
                    properties.add(typedProperty);
                } else {
                    Property untypedProperty = new SimpleUntypedProperty(propertyValue);
                    properties.add(untypedProperty);
                }
            }
        }
        return properties;
    }

}
