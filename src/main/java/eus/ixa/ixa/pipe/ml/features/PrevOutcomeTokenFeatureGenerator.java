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
import opennlp.tools.util.featuregen.FeatureGeneratorUtil;

/**
 * Adds bigram features based on tokens and token class using
 * {@code TokenClassFeatureGenerator}.
 * 
 * @author mustaszewski
 *
 */
public class PrevOutcomeTokenFeatureGenerator extends CustomFeatureGenerator {
	private Map<String, String> attributes;
	private static final String SB = "*SB*";

	public PrevOutcomeTokenFeatureGenerator() {
	}

	public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {

		String classType = attributes.get("type");
		String outcomeTokenClassFeat = attributes.get("outcomeTokenClassFeature");

		String po = null;
		String tokClass = null;
		
		if (previousOutcomes != null) {
			if (index > 0) {
				po = previousOutcomes[index - 1];
			}
			else {
				po = SB;
			}
			features.add("tag-1,w=" + po + "," + tokens[index]);

			if (classType.equals("POS")) {
				tokClass = TokenClassFeatureGenerator.tokenShapeFeature4POS(tokens[index]);
			} else {
				tokClass = FeatureGeneratorUtil.tokenFeature(tokens[index]);

			}
			if (outcomeTokenClassFeat.equalsIgnoreCase("true")) {
				features.add("tag-1,wf=" + po + "," + tokClass);
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