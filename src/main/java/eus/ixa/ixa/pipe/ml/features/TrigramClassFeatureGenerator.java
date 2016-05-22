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

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;

/**
 * Adds trigram features based on tokens and token class using
 * {@code TokenClassFeatureGenerator}.
 * 
 * @author ragerri
 * 
 */
public class TrigramClassFeatureGenerator extends FeatureGeneratorAdapter {
	String classType = "POS"; // TO DO: GET THIS DYNAMICALLY

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
	String wc, pwc, ppwc, nwc, nnwc;
	wc = pwc = ppwc = nwc = nnwc = null;
	if (classType.equals("POS")) {
		wc = TokenClassFeatureGenerator.tokenShapeFeature4POS(tokens[index]);
	}
	else {
		wc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
	}
    
    // trigram features
    if (index > 1) {
      features.add("ppw,pw,w=" + tokens[index - 2] + "," + tokens[index - 1] + "," + tokens[index]);
  	if (classType.equals("POS")) {
        pwc = TokenClassFeatureGenerator.tokenShapeFeature4POS(tokens[index - 1]);
        ppwc = TokenClassFeatureGenerator.tokenShapeFeature4POS(tokens[index - 2]);
	}
	else {
	      pwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 1]);
	      ppwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 2]);
	}

      features.add("ppwc,pwc,wc=" + ppwc + "," + pwc + "," + wc);
    }
    if (index + 2 < tokens.length) {
      features.add("w,nw,nnw=" + tokens[index] + "," + tokens[index + 1] + "," + tokens[index + 2]);
      
    	if (classType.equals("POS")) {
    	      nwc = TokenClassFeatureGenerator.tokenShapeFeature4POS(tokens[index + 1]);
    	      nnwc = TokenClassFeatureGenerator.tokenShapeFeature4POS(tokens[index + 2]);
    	}
    	else {
    	      nwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 1]);
    	      nnwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 2]);
    	}
      

      features.add("wc,nwc,nnwc=" + wc + "," + nwc + "," + nnwc);
    }
  }
}
