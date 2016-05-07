/*
 * Copyright 2016 Michael Ustaszewski

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;

/**
 * Splits fine-grained POS tags on a predetermined separator (e.g. "subst:sg:gen:m") in order to use sublabels preceding the current token as features.
 * In the trainParams.properties file, the following parameters have to be specified:
 * <ol>
 * <li>SublabelFeatures: whether to use this type of feature. Possible values: yes|no. Default: no.
 * <li>SublabelSeparator: the character used in a tagset to separate sublabes within fine-grained POS tags (e.g. the colon in subst:sg:gen:m). Possible values: any string. Default: ":".
 * <li>SublabelWordclass: whether to extract word class (i.e. coarse grained POS) from the fine-grained tag, too. Word class needs to be the first entry in a fine-grained POS tag (e.g. the substantive in subst:sg:gen_n)
 * <li>SublabelClasses: Definitions of the grammatical classes plus the corresponding values you would like to extract: Format: grammaticalClass1:value1|value2|valueN,grammaticalClass2:value1|valueN You can specify as many classes and values as you want; concatenate them using a comma (e.g. SublabelClasses=number:pl|sg,gender:m1|m2|m3|f|n,case:nom|gen|dat|acc|inst|loc|voc)
 * <li>SublabelRange: Indicates for how many previous tokens sublabels are to be extracted. Default is 2. Note that the second field (after the colon) must be zero, because only sublabels of preceding tokens are to be extracted.
 * 
 * @author mustaszewski
 * @version 2016-05-05
 */
public class SublabelFeatureGenerator extends CustomFeatureGenerator {

	private Map<String, String> attributes;
	private HashMap<String, Pattern> sublabelDefinitions;
	private String separator;

	public SublabelFeatureGenerator() {
	}

	public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {
		if (preds != null) {
			if (index > 0) {
				HashMap<String, String> prevValues = new HashMap<String, String>();
				prevValues = getPrevLabels(preds, index);
				for (Map.Entry<String, String> prevValue : prevValues.entrySet()) {
					features.add(prevValue.getKey() + "=" + prevValue.getValue());
				}
			}
		}
	}

	private HashMap<String, String> getPrevLabels(String[] preds, int index) {
		HashMap<String, String> prevValues = new HashMap<String, String>();
		String[] prevSublabels = preds[index - 1].split(separator);
		if (attributes.get("wordClass").equals("true")) {
			prevValues.put("prevPOS", prevSublabels[0]);
		}
		for (String labelValue : prevSublabels) {
			labelValue = labelValue.split("-", 2)[0];
			for (Map.Entry<String, Pattern> labelDef : sublabelDefinitions.entrySet()) {
				String labelClass = labelDef.getKey();
				Pattern morphologiclaValues = labelDef.getValue();
				if (morphologiclaValues.matcher(labelValue).matches()) {
					prevValues.put(labelClass, labelValue.split("-")[0]);
				}
			}
		}
		return prevValues;
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
		this.attributes = properties;
		this.separator = getSeparator(properties);
		this.sublabelDefinitions = getSublabelDefinitions(properties);
	}

	public HashMap<String, Pattern> getSublabelDefinitions(Map<String, String> properties) {
		HashMap<String, Pattern> sublabelDefinitions = new HashMap<String, Pattern>();
		sublabelDefinitions.put("prevPOS", Pattern.compile("void"));
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String category = entry.getKey();
			if (category != "separator") {
				String featPrefix = "prev" + category.substring(0, 1).toUpperCase() + category.substring(1);
				String values = entry.getValue();
				Pattern x = Pattern.compile(values);
				sublabelDefinitions.put(featPrefix, x);
			}
		}
		return sublabelDefinitions;
	}

	public String getSeparator(Map<String, String> properties) {
		String separator = properties.get("separator");
		return separator;
	}
}
