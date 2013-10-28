package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestZoomaLuceneIndexer {
    private Version version;
    private Analyzer analyzer;

    private Path annotationIndexPath;
    private Path summaryIndexPath;

    private URI verifiableSemanticTag;
    private URI unverifiableSemanticTag;

    private AnnotationDAO singleAnnotationDAO;
    private AnnotationDAO multiAnnotationDAO;
    private AnnotationDAO verifiedAnnotationDAO;

    //    private PropertyDAO singlePropertyDAO;
    private PropertyDAO propertyDAO;

    @Before
    public void setUp() {
        try {
            // create index setup
            version = Version.LUCENE_35;
            analyzer = new EnglishAnalyzer(version);

            // directories that need to be persisted for querying
            try {
                annotationIndexPath = Files.createTempDirectory("test-annotation-index");
                summaryIndexPath = Files.createTempDirectory("test-summary-index");
            }
            catch (IOException e) {
                e.printStackTrace();
                fail("Could not open temp directories");
            }

            // create mock objects
            Property property1 = new SimpleTypedProperty(new URI("http://www.test.com/property"), "type1", "value1");
            Property property2 = new SimpleTypedProperty(new URI("http://www.test.com/property"), "type2", "value2");

            URI semanticTag1 = new URI("http://www.test.com/semantic-tag-1");
            URI semanticTag2 = new URI("http://www.test.com/semantic-tag-2");
            URI semanticTag3 = new URI("http://www.test.com/semantic-tag-3");

            verifiableSemanticTag = semanticTag1;
            unverifiableSemanticTag = semanticTag3;

            AnnotationProvenance prov1 = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(new URI("http://www.test.com/source1"), "source1"),
                                                                        AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                                        "TEST",
                                                                        new Date());
            Annotation anno1 = new SimpleAnnotation(new URI("http://www.test.com/annotation1"),
                                                    Collections.<BiologicalEntity>emptySet(),
                                                    property1,
                                                    prov1,
                                                    semanticTag1);
            AnnotationProvenance prov2 = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(new URI("http://www.test.com/source2"), "source2"),
                                                                        AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                                        "TEST",
                                                                        new Date());
            Annotation anno2 = new SimpleAnnotation(new URI("http://www.test.com/annotation2"),
                                                    Collections.<BiologicalEntity>emptySet(),
                                                    property2,
                                                    prov2,
                                                    semanticTag2);
            // anno3 is alternate mapping for property1
            AnnotationProvenance prov3 = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(new URI("http://www.test.com/source3"), "source3"),
                                                                        AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                                        "TEST",
                                                                        new Date());
            Annotation anno3 = new SimpleAnnotation(new URI("http://www.test.com/annotation3"),
                                                    Collections.<BiologicalEntity>emptySet(),
                                                    property1,
                                                    prov3,
                                                    semanticTag3);
            // anno4 verifies anno1 from different source
            AnnotationProvenance prov4 = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(new URI("http://www.test.com/source2"), "source2"),
                                                                        AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                                        "TEST",
                                                                        new Date());
            Annotation anno4 = new SimpleAnnotation(new URI("http://www.test.com/annotation4"),
                                                    Collections.<BiologicalEntity>emptySet(),
                                                    property1,
                                                    prov4,
                                                    semanticTag1);
            // anno5 verifies anno3 from same source
            AnnotationProvenance prov5 = new SimpleAnnotationProvenance(new SimpleDatabaseAnnotationSource(new URI("http://www.test.com/source3"), "source3"),
                                                                        AnnotationProvenance.Evidence.MANUAL_CURATED,
                                                                        "TEST",
                                                                        new Date());
            Annotation anno5 = new SimpleAnnotation(new URI("http://www.test.com/annotation5"),
                                                    Collections.<BiologicalEntity>emptySet(),
                                                    property1,
                                                    prov5,
                                                    semanticTag3);


            // create mocked DAOs
//            singlePropertyDAO = mock(PropertyDAO.class);
//            when(singlePropertyDAO.read()).thenReturn(Collections.singleton(property1));

            propertyDAO = mock(PropertyDAO.class);
            Collection<Property> properties = new HashSet<>();
            properties.add(property1);
            properties.add(property2);
            when(propertyDAO.read()).thenReturn(properties);

            singleAnnotationDAO = mock(AnnotationDAO.class);
            when(singleAnnotationDAO.read()).thenReturn(Collections.singleton(anno1));

            multiAnnotationDAO = mock(AnnotationDAO.class);
            Collection<Annotation> multiAnnotations = new HashSet<>();
            multiAnnotations.add(anno1);
            multiAnnotations.add(anno2);
            when(multiAnnotationDAO.read()).thenReturn(multiAnnotations);

            verifiedAnnotationDAO = mock(AnnotationDAO.class);
            Collection<Annotation> verifiedAnnotations = new HashSet<>();
            verifiedAnnotations.add(anno1);
            verifiedAnnotations.add(anno3);
            verifiedAnnotations.add(anno4);
            verifiedAnnotations.add(anno5);
            when(verifiedAnnotationDAO.read()).thenReturn(verifiedAnnotations);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @After
    public void tearDown() {
//        singlePropertyDAO = null;
        propertyDAO = null;

        singleAnnotationDAO = null;
        multiAnnotationDAO = null;
        verifiedAnnotationDAO = null;

        FileVisitor deletor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                else {
                    // directory iteration failed
                    throw e;
                }
            }
        };

        try {
            Files.walkFileTree(annotationIndexPath, deletor);
            Files.walkFileTree(summaryIndexPath, deletor);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateVerifiedAnnotationSummaryIndex() {

        Directory annotationDir = null;
        Directory summaryDir = null;

        try {
            annotationDir = new NIOFSDirectory(annotationIndexPath.toFile());
            summaryDir = new NIOFSDirectory(summaryIndexPath.toFile());
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Couldn't open test index directories");
        }

        // setup indexer, use RAM directories if we don't need to reuse
        ZoomaLuceneIndexer indexer = new ZoomaLuceneIndexer();
        indexer.setAnalyzer(analyzer);
        indexer.setAnnotationDAO(verifiedAnnotationDAO);
        indexer.setPropertyDAO(propertyDAO);
        indexer.setPropertyIndex(new RAMDirectory());
        indexer.setPropertyTypeIndex(new RAMDirectory());
        indexer.setAnnotationCountIndex(new RAMDirectory());
        indexer.setAnnotationIndex(annotationDir);
        indexer.setAnnotationSummaryIndex(summaryDir);


        // create indices needed for this test
        try {
            indexer.createAnnotationIndex(verifiedAnnotationDAO.read());
            indexer.createAnnotationSummaryIndex(verifiedAnnotationDAO.read());
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Couldn't create annotation summary index");
        }

        // close indexer
        indexer.destroy();

        // reopen indexed directories
        try {
            annotationDir = new NIOFSDirectory(annotationIndexPath.toFile());
            summaryDir = new NIOFSDirectory(summaryIndexPath.toFile());
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Couldn't open test index directories");
        }

        // create annotation summary search service
        LuceneAnnotationSummarySearchService searchService = new LuceneAnnotationSummarySearchService();
        searchService.setAnalyzer(analyzer);
        searchService.setIndex(summaryDir);
        searchService.setAnnotationIndex(annotationDir);
        searchService.init();

        // do test query to verify results are valid
        Map<AnnotationSummary, Float> results = searchService.searchAndScore("value1");

        // assert result is as expected
        assertEquals("Wrong number of results", 2, results.keySet().size());

        // get both summaries
        AnnotationSummary verifiedSummary = null;
        AnnotationSummary unverifiedSummary = null;
        for (AnnotationSummary as : results.keySet()) {
            if (as.getSemanticTags().contains(verifiableSemanticTag)) {
                verifiedSummary = as;
            }

            if (as.getSemanticTags().contains(unverifiableSemanticTag)) {
                unverifiedSummary = as;
            }
        }

        assertNotNull("Could not find a verified annotation summary", verifiedSummary);
        assertNotNull("Could not find an unverified annotation summary", unverifiedSummary);

        assertTrue("Verified summary should score higher than unverified summary",
                   results.get(verifiedSummary) > results.get(unverifiedSummary));
    }
}
