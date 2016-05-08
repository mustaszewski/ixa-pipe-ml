/*
 *  Copyright 2014 Rodrigo Agerri

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
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.ml.utils.Flags;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.featuregen.StringPattern;

/**
 * Generates a class name for the specified token. The classes are as follows
 * where the first matching class is used:
 * <ul>
 * <li>lc - lowercase alphabetic</li>
 * <li>2d - two digits</li>
 * <li>4d - four digits</li>
 * <li>an - alpha-numeric</li>
 * <li>dd - digits and dashes</li>
 * <li>ds - digits and slashes</li>
 * <li>dc - digits and commas</li>
 * <li>dp - digits and periods</li>
 * <li>num - digits</li>
 * <li>sc - single capital letter</li>
 * <li>ac - all capital letters</li>
 * <li>ic - initial capital letter</li>
 * <li>cp - a capital letter followed by a period
 * <li>other - other</li>
 * </ul>
 */
public class TokenClassFeatureGenerator extends CustomFeatureGenerator {

  private boolean isLower;
  private boolean isWordAndClassFeature;
  private static Pattern capPeriod;
  private String classType; //
  static {
    capPeriod = Pattern.compile("^\\p{javaUpperCase}\\.$"); //Pattern.compile("\\p{Lu}"); // or "\\p{javaUpperCase}" //orig: Pattern.compile("^[A-Z]\\.$")
  }
 

  public TokenClassFeatureGenerator() {
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {
	
	String wordClass = "";
	if (classType.equals("NERC")) {
	  wordClass = tokenShapeFeature(tokens[index]);
      features.add("wc=" + wordClass);
	}
	else if (classType.equals("POS")) {
	  wordClass = tokenShapeFeature4POS(tokens[index]);
      features.add("WC4POS=" + wordClass);
	}

    if (isWordAndClassFeature) {
      if (isLower) {
        features.add("w&c=" + tokens[index].toLowerCase()
            + "," + wordClass);
      } else {
        features.add("w&c=" + tokens[index]
            + "," + wordClass);
      }
    }
    if (Flags.DEBUG) {
      System.err.println("-> " + tokens[index].toLowerCase() + ": w&c=" + tokens[index].toLowerCase()
          + "," + wordClass);
    }
  }

  public static String tokenShapeFeature(String token) {

    StringPattern pattern = StringPattern.recognize(token);

    String feat;
    if (pattern.isAllLowerCaseLetter()) {
      feat = "lc";
    } else if (pattern.digits() == 2) {
      feat = "2d";
    } else if (pattern.digits() == 4) {
      feat = "4d";
    }
    else if (pattern.containsDigit()) {
      if (pattern.containsLetters()) {
        feat = "an";
      } else if (pattern.containsHyphen()) {
        feat = "dd";
      } else if (pattern.containsSlash()) {
        feat = "ds";
      } else if (pattern.containsComma()) {
        feat = "dc";
      } else if (pattern.containsPeriod()) {
        feat = "dp";
      } else {
        feat = "num";
      }
    } else if (pattern.isAllCapitalLetter() && token.length() == 1) {
      feat = "sc";
    } else if (pattern.isAllCapitalLetter()) {
      feat = "ac";
    } else if (capPeriod.matcher(token).find()) {
      feat = "cp";
    } else if (pattern.isInitialCapitalLetter()) {
      feat = "ic";
    } else {
      feat = "other";
    }

    return (feat);
  }
  
  public static String tokenShapeFeature4POS(String token) {

	    StringPattern pattern = StringPattern.recognize(token);

	    String feat;
	    if (pattern.isAllLowerCaseLetter()) { // UNCHANGED
	      feat = "lc";
	      
	    } /*else if (pattern.digits() == 2) {
	      feat = "2d";
	    } else if (pattern.digits() == 4) {
	      feat = "4d";
	    }*/
	    else if (pattern.containsDigit()) {
	      if (pattern.containsLetters()) {
	        feat = "an";
	      } else {
	    	  feat = "num";
	      }
	      
	      /*else if (pattern.containsHyphen()) {
	        feat = "dd";
	      } else if (pattern.containsSlash()) {
	        feat = "ds";
	      } else if (pattern.containsComma()) {
	        feat = "dc";
	      } else if (pattern.containsPeriod()) {
	        feat = "dp";
	      } else {
	        feat = "num";
	      }*/
	    } else if (pattern.isAllCapitalLetter() && token.length() == 1) { // UNCHANGED
	      feat = "sc";
	    } else if (pattern.isAllCapitalLetter()) { // UNCHANGED
	      feat = "ac";
	    } else if (capPeriod.matcher(token).find()) { //UNCHANGED
	      feat = "cp";
	    } else if (pattern.isInitialCapitalLetter()) { //UNCHANGED
	      feat = "ic";
	    } else {
	      feat = "other"; //UNCHANGED
	    }

	    return (feat);
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
   processRangeOptions(properties);
  }
  
  /**
   * Process the options of which type of features are to be generated.
   * @param properties the properties map
   */
  private void processRangeOptions(Map<String, String> properties) {
    String featuresRange = properties.get("range");
    String[] rangeArray = Flags.processTokenClassFeaturesRange(featuresRange);
    if (rangeArray[0].equalsIgnoreCase("lower")) {
      isLower = true;
    }
    if (rangeArray[1].equalsIgnoreCase("wac")) {
      isWordAndClassFeature = true;
    }
    String type = properties.get("type");
    this.classType = type;
  }

}