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
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;


/**
 * Adds bigram features based on tokens and token class using
 * {@code TokenClassFeatureGenerator}.
 * 
 * @author mustaszewski
 *
 */
public class PrevOutcomeFeatureGenerator extends CustomFeatureGenerator {
	private static final String SB = "*SB*";
	private Map<String, String> attributes;



	public PrevOutcomeFeatureGenerator() {
	}

	public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {

		// Get Attributes
		int outcomesRange = Integer.parseInt(attributes.get("outcomesRange"));
		//String ngramFeat = attributes.get("ngramFeatures");
		//int ngramRange = Integer.parseInt(attributes.get("ngramRange"));


		// Get previous outcomes depending on index and outcomes range
		String[] prevOutcomesAll = new String[outcomesRange];
		for (int i = 1, ll = outcomesRange; i <= ll; i++) {
			if (index - i >= 0) {
				prevOutcomesAll[i - 1] = previousOutcomes[index - i];
			} else {
				prevOutcomesAll[i - 1] = SB;
			}
		}

		// Get longest possible array of previous outcomes at current index
		int maxOutcomeLength;
		if (index + 1 <= outcomesRange) {
			maxOutcomeLength = index + 1;
		} else {
			maxOutcomeLength = outcomesRange;
		}

		// pass values of prevOutcomesAll array to new array depending on
		// maxOutcomeLength
		//ArrayList<String> prevTags = new ArrayList<String>();
		if (prevOutcomesAll != null) {
			for (int i = 1, mol = maxOutcomeLength + 1; i < mol; i++) {
				features.add("tag-" + i + "=" + prevOutcomesAll[i - 1]);
				//prevTags.add(prevOutcomesAll[i - 1]);
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
	public void init(Map<String, String> properties, FeatureGeneratorResourceProvider resourceProvider)
			throws InvalidFormatException {
		attributes = properties;
	}
}
