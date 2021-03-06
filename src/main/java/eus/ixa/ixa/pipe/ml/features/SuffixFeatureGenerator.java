/*
 * Copyright 2016 Rodrigo Agerri

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

import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import eus.ixa.ixa.pipe.ml.utils.Flags;


public class SuffixFeatureGenerator extends CustomFeatureGenerator {
  
  private Map<String, String> attributes;
  
  public String[] getSuffixes(String lex) {
    Integer start = Integer.parseInt(attributes.get("begin"));
    Integer end = Integer.parseInt(attributes.get("end"));
    String[] suffs = new String[end];
    for (int i = start, l = end; i < l; i++) {
      suffs[i] = lex.substring(Math.max(lex.length() - i - 1, 0));
    }
    return suffs;
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    String[] suffs = getSuffixes(tokens[index]);
    for (String suff : suffs) {
      features.add("suf=" + suff);
      if (Flags.DEBUG) {
        System.err.println("-> " + tokens[index] + ": suf=" + suff);
      }
    }
  }
  
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
    attributes = properties;
  }
}