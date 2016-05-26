/*
 * Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package eus.ixa.ixa.pipe.ml.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eus.ixa.ixa.pipe.ml.resources.MorfessorFeature;
import eus.ixa.ixa.pipe.ml.resources.Word2VecCluster;
import eus.ixa.ixa.pipe.ml.utils.Flags;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;

public class MorfessorFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {
  
  private Word2VecCluster word2vecCluster;
  private MorfessorFeature morfessorFeature;
  private static String unknownClass = "O";
  private Set<String> morphemes;
  private Map<String, String> attributes;
  
  
  public MorfessorFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
	
	//String longestSuffix = null;
	
	
    Set<String> candidateMorphemes = morfessorFeature.getCandidates(tokens[index].toLowerCase());
    

    if (!candidateMorphemes.isEmpty()) {
    	for (String x : candidateMorphemes) {
    	features.add("CANDIDATE=" + x);
    	} 
    }
    else {
    	features.add("CANDIDATE="+getRegularSuffixes(tokens[index]));
    }
    	

    /*
    longestSuffix = getLongestSuffix(candidateMorphemes);
    features.add("longestsuffix=" + longestSuffix);
    */
    
    
    /*
    String wordClass = getWordClass(tokens[index].toLowerCase());
    features.add(attributes.get("dict") + "=" + wordClass);
    
    if (Flags.DEBUG) {
      System.err.println("-> " + tokens[index].toLowerCase() + ": " + attributes.get("dict") + "=" + wordClass);
    }
    */
  }
  
  public String getRegularSuffixes(String lex) {
	    Integer start = 0;
	    Integer end = 4;
	    String[] suffs = new String[end];
	    for (int i = start, l = end; i < l; i++) {
	      suffs[i] = lex.substring(Math.max(lex.length() - i - 1, 0));
	    }
	    //return suffs[0];
	    return suffs[suffs.length-1];
	  }
  
  
  private String getLongestSuffix(Set<String> candidates) {
      int maxLength = 0;
      String longestSuffix = null;
      for (String s : candidates) {
          if (s.length() > maxLength) {
              maxLength = s.length();
              longestSuffix = s;
          }
      }
      return longestSuffix;
  }
  
  
  /*
  private Set<String> getCandidates(String token) {
	  Set<String> candidates = new HashSet<String>();
	  
	  return candidates;
  }*/

  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    
  }

  @Override
  public void clearAdaptiveData() {
    
  }

  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
	  
    Object dictResource = resourceProvider.getResource(properties.get("dict"));
    if (!(dictResource instanceof MorfessorFeature)) {
      throw new InvalidFormatException("Not a MorfessorFeature resource for key: " + properties.get("dict"));
    }
    this.morfessorFeature = (MorfessorFeature) dictResource;
    this.attributes = properties;
  }
  
  /*// INIT OF CHARNGRAMFEATUREGENERATOR
  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    this.attributes = properties;
    
  }
   */
  
  
  /*// ORIGINAL INIT OF w2vCLUSTERFEATURE
     @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    Object dictResource = resourceProvider.getResource(properties.get("dict"));
    if (!(dictResource instanceof Word2VecCluster)) {
      throw new InvalidFormatException("Not a Word2VecCluster resource for key: " + properties.get("dict"));
    }
    this.word2vecCluster = (Word2VecCluster) dictResource;
    this.attributes = properties;
  }
   */
  

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("morfessorserializer", new MorfessorFeature.MorfessorFeatureSerializer()); 
    return Collections.unmodifiableMap(mapping);
  }
  /*// ORIGINAL OVVERIDE METHOD OF W2VCLUSTERFEATUREGENERATOR
  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("word2vecserializer", new Word2VecCluster.Word2VecClusterSerializer());
    return Collections.unmodifiableMap(mapping);
  }
  */
}
