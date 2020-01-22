package org.dkpro.core.io.brat;

import java.util.ArrayList;
import java.util.List;

import org.dkpro.core.io.brat.internal.mapping.CommentMapping;
import org.dkpro.core.io.brat.internal.mapping.Mapping;
import org.dkpro.core.io.brat.internal.mapping.RelationMapping;
import org.dkpro.core.io.brat.internal.mapping.SpanMapping;
import org.dkpro.core.io.brat.internal.mapping.TypeMapping;
import org.dkpro.core.io.brat.internal.mapping.TypeMappings;

public class DefaultMappings {
    
    private static Mapping defaultMapping_Brat2UIMA = null;
    private static Mapping defaultMapping_UIMA2Brat = null;
    
    public  static Mapping getDefaultMapping_Brat2UIMA() {
        if (defaultMapping_Brat2UIMA == null) {
            //
            //   TODO: Instead of explictly listing all the types in the dkpro-core 
            //     UIMA type system, we could maybe use reflection to find all
            //     the classes in each of the dkpro-core type packages:
            //
            //     https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
            //
            //   That way, we only need to list the packages, not all the 
            //   classes.
            //            
            List<TypeMapping> txtTypeMappingLst = new ArrayList<TypeMapping>();
            TypeMappings txtTypeMappings = new TypeMappings(txtTypeMappingLst);
            {
                /// Add mappings for NER types
                String[] nerTypeNames = new String[] {
                        "Animal", "Cardinal", "ContactInfo", "Date", "Disease", "Event",
                        "Fac", "Game", "Gpe", "Language", "Law", "Location", "Money", 
                        "NamedEntity", "Nationality", "Norp", "Ordinal", "Organization",
                        "OrgDesc", "Percent", "PerDesc", "Person", "Plant", "Product",
                        "ProductDesc", "Quantity", "Substance", "Time", "WorkOfArt"
                };
                for (String typeName: nerTypeNames) {
                    String aType = "de.tudarmstadt.ukp.dkpro.core.api.ner.type." + typeName;
                    txtTypeMappingLst.add(new TypeMapping(typeName, aType));
                }
            }
            {
                // Add mappings for Segmentation types
                String[] segTypeNames = new String[] {
                        "CompoundPart", "Div", "Document", "Heading", "Lemma", "LexicalPhrase",
                        "LinkingMorpheme", "NGram", "Paragraph", "Sentence", "Split", "Stem", 
                        "StopWord", "SurfaceForm", 
                        "Token"
                };
                for (String typeName: segTypeNames) {
                    String aType = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type."
                            + typeName;
                    txtTypeMappingLst.add(new TypeMapping(typeName, aType));
                }
            }
            
            List<TypeMapping> relTypeMappingLst = new ArrayList<TypeMapping>();
            TypeMappings relTypeMappings = new TypeMappings(relTypeMappingLst);
            
            List<SpanMapping> txtAnnotsLst = new ArrayList<SpanMapping>();
                        
            List<RelationMapping> relMapLst = new ArrayList<RelationMapping>();

            List<CommentMapping> commMapLst = new ArrayList<CommentMapping>();
            
            defaultMapping_Brat2UIMA = new Mapping(txtTypeMappings, relTypeMappings, txtAnnotsLst, 
                    relMapLst, commMapLst);
        }
        return defaultMapping_Brat2UIMA;
    }

    public static Mapping getDefaultMapping_UIMA2Brat() {
        if (defaultMapping_UIMA2Brat == null) {
            //
            //   TODO: Instead of explictly listing all the types in the dkpro-core 
            //     UIMA type system, we could maybe use reflection to find all
            //     the classes in each of the dkpro-core type packages:
            //
            //     https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
            //
            //   That way, we only need to list the packages, not all the 
            //   classes.
            //
            
            List<TypeMapping> txtTypeMappingLst = new ArrayList<TypeMapping>();
            TypeMappings txtTypeMappings = new TypeMappings(txtTypeMappingLst);
            {
                /// Add mappings for NER types
                String[] nerTypeNames = new String[] {
                        "Animal", "Cardinal", "ContactInfo", "Date", "Disease", "Event",
                        "Fac", "Game", "Gpe", "Language", "Law", "Location", "Money", 
                        "NamedEntity", "Nationality", "Norp", "Ordinal", "Organization",
                        "OrgDesc", "Percent", "PerDesc", "Person", "Plant", "Product",
                        "ProductDesc", "Quantity", "Substance", "Time", "WorkOfArt"
                };
                for (String typeName: nerTypeNames) {
                    String aType = "de.tudarmstadt.ukp.dkpro.core.api.ner.type." + typeName;
                    txtTypeMappingLst.add(new TypeMapping(aType, typeName));
                }
            }
            {
                // Add mappings for Segmentation types
                String[] segTypeNames = new String[] {
                        "CompoundPart", "Div", "Document", "Heading", "Lemma", "LexicalPhrase",
                        "LinkingMorpheme", "NGram", "Paragraph", "Sentence", "Split", "Stem", 
                        "StopWord", "SurfaceForm", 
                        "Token"
                };
                for (String typeName: segTypeNames) {
                    String aType = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type."
                            + typeName;
                    txtTypeMappingLst.add(new TypeMapping(aType, typeName));
                }
            }
            
            List<TypeMapping> relTypeMappingLst = new ArrayList<TypeMapping>();
            TypeMappings relTypeMappings = new TypeMappings(relTypeMappingLst);
            
            List<SpanMapping> txtAnnotsLst = new ArrayList<SpanMapping>();
                        
            List<RelationMapping> relMapLst = new ArrayList<RelationMapping>();

            List<CommentMapping> commMapLst = new ArrayList<CommentMapping>();
            
            defaultMapping_UIMA2Brat = new Mapping(txtTypeMappings, relTypeMappings, txtAnnotsLst, 
                    relMapLst, commMapLst);
        }
        
        return defaultMapping_UIMA2Brat;
    }
}
