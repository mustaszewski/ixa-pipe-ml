package eus.ixa.ixa.pipe.ml.sequence;


import java.io.FileWriter; // DEBUG ONLY
import java.io.IOException; // DEBUG ONLY

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorUtil;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 * Class for determining contextual features for a tag/chunk style
 * named-entity recognizer.
 */
public class DefaultSequenceContextGenerator implements SequenceContextGenerator {

  private AdaptiveFeatureGenerator[] featureGenerators;
  
  @Deprecated
  private static AdaptiveFeatureGenerator windowFeatures = new CachedFeatureGenerator(
      new AdaptiveFeatureGenerator[]{
      new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
      new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
      new OutcomePriorFeatureGenerator(),
      new PreviousMapFeatureGenerator(),
      new BigramNameFeatureGenerator()
      });
  
  /**
   * Creates a name context generator with the specified cache size.
   *
 * @param featureGenerators the array of feature generators
 */
public DefaultSequenceContextGenerator(AdaptiveFeatureGenerator... featureGenerators) {

    if (featureGenerators != null) {
      this.featureGenerators = featureGenerators;
    }
    else {
      // use defaults
      this.featureGenerators = new AdaptiveFeatureGenerator[]{
          windowFeatures,
          new PreviousMapFeatureGenerator()};
    }
  }

  public void addFeatureGenerator(AdaptiveFeatureGenerator generator) {
      AdaptiveFeatureGenerator generators[] = featureGenerators;

      featureGenerators = new AdaptiveFeatureGenerator[featureGenerators.length + 1];

      System.arraycopy(generators, 0, featureGenerators, 0, generators.length);

      featureGenerators[featureGenerators.length - 1] = generator;
  }

  public void updateAdaptiveData(String[] tokens, String[] outcomes) {

    if (tokens != null && outcomes != null && tokens.length != outcomes.length) {
        throw new IllegalArgumentException(
            "The tokens and outcome arrays MUST have the same size!");
      }

    for (AdaptiveFeatureGenerator featureGenerator : featureGenerators) {
      featureGenerator.updateAdaptiveData(tokens, outcomes);
    }
  }

  public void clearAdaptiveData() {
    for (AdaptiveFeatureGenerator featureGenerator : featureGenerators) {
      featureGenerator.clearAdaptiveData();
    }
  }

  /**
   * Return the context for finding names at the specified index.
   * @param index The index of the token in the specified toks array for which the context should be constructed.
   * @param tokens The tokens of the sentence.  The <code>toString</code> methods of these objects should return the token text.
   * @param preds The previous decisions made in the tagging of this sequence.  Only indices less than i will be examined.
   * @param additionalContext Addition features which may be based on a context outside of the sentence.
   *
   * @return the context for finding names at the specified index.
   */
  public String[] getContext(int index, String[] tokens, String[] preds, Object[] additionalContext) {
    List<String> features = new ArrayList<String>();

    for (AdaptiveFeatureGenerator featureGenerator : featureGenerators) {
      featureGenerator.createFeatures(features, tokens, index, preds);
    }
/*
    //previous outcome features
    String po = BilouCodec.OTHER;
    String ppo = BilouCodec.OTHER;

    // TODO: These should be moved out here in its own feature generator!
     * Done by mustaszewski: moved to PrevOutcomeFeatureGenerator (including some more previous outcome-relate features)
    if (preds != null) {
      if (index > 1){
        ppo = preds[index-2];
      }

      if (index > 0) {
        po = preds[index-1];
      }
      features.add("po=" + po);
      features.add("pow=" + po + "," + tokens[index]);
      features.add("powf=" + po + "," + FeatureGeneratorUtil.tokenFeature(tokens[index]));
      features.add("ppo=" + ppo);
    }
    */
    /*
    // START DEBUG ONLY
	try {
		FileWriter writer = new FileWriter("DebugContext.txt", true);
		writer.write("Feature List\t"+features.toString()+"\n");
		writer.write("\r\n");   // write new line
		writer.close();
	} catch (IOException e) {
		e.printStackTrace();
    	}
    
    // END DEBUG ONLY
	*/
    return features.toArray(new String[features.size()]);
  }
}
