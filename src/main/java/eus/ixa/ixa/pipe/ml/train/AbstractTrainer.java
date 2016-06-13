/*
 *  Copyright 2015 Rodrigo Agerri

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

package eus.ixa.ixa.pipe.ml.train;

//import java.io.FileWriter; //DebugOnly
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import eus.ixa.ixa.pipe.ml.formats.CoNLL02Format;
import eus.ixa.ixa.pipe.ml.formats.CoNLL03Format;
import eus.ixa.ixa.pipe.ml.formats.LemmatizerFormat;
import eus.ixa.ixa.pipe.ml.formats.TabulatedFormat;
import eus.ixa.ixa.pipe.ml.sequence.BilouCodec;
import eus.ixa.ixa.pipe.ml.sequence.BioCodec;
import eus.ixa.ixa.pipe.ml.sequence.SequenceLabelerEvaluator;
import eus.ixa.ixa.pipe.ml.sequence.SequenceLabelerFactory;
import eus.ixa.ixa.pipe.ml.sequence.SequenceLabelerME;
import eus.ixa.ixa.pipe.ml.sequence.SequenceLabelerModel;
import eus.ixa.ixa.pipe.ml.sequence.SequenceSample;
import eus.ixa.ixa.pipe.ml.sequence.SequenceSampleTypeFilter;
import eus.ixa.ixa.pipe.ml.utils.Flags;
import eus.ixa.ixa.pipe.ml.utils.IOUtils;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * Abstract class for common training functionalities. Every other trainer class
 * needs to extend this class.
 * @author ragerri
 * @version 2015-02-25
 */
public abstract class AbstractTrainer implements Trainer {
  
  /**
   * The language.
   */
  private String lang;
  /**
   * String holding the training data.
   */
  private String trainData;
  /**
   * String holding the testData.
   */
  private String testData;
  /**
   * ObjectStream of the training data.
   */
  private ObjectStream<SequenceSample> trainSamples;
  /**
   * ObjectStream of the test data.
   */
  private ObjectStream<SequenceSample> testSamples;
  /**
   * The corpus format: conll02, conll03 and opennlp.
   */
  private String corpusFormat;
  /**
   * beamsize value needs to be established in any class extending this one.
   */
  private int beamSize;
  /**
   * The sequence encoding of the named entity spans, e.g., BIO or BILOU.
   */
  private String sequenceCodec;
  /**
   * Reset the adaptive features every newline in the training data.
   */
  private String clearTrainingFeatures;
  /**
   * Reset the adaptive features every newline in the testing data.
   */
  private String clearEvaluationFeatures;
  /**
   * features needs to be implemented by any class extending this one.
   */
  private SequenceLabelerFactory nameClassifierFactory;

  /**
   * Construct a trainer with training and test data, and with options for
   * language, beamsize for decoding, sequence codec and corpus format (conll or opennlp).
   * @param params the training parameters
   * @throws IOException
   *           io exception
   */
  public AbstractTrainer(final TrainingParameters params) throws IOException {
    
    this.lang = Flags.getLanguage(params);
    this.clearTrainingFeatures = Flags.getClearTrainingFeatures(params);
    this.clearEvaluationFeatures = Flags.getClearEvaluationFeatures(params);
    this.corpusFormat = Flags.getCorpusFormat(params);
    this.trainData = params.getSettings().get("TrainSet");
    this.testData = params.getSettings().get("TestSet");
    trainSamples = getSequenceStream(trainData, clearTrainingFeatures, corpusFormat);
    
    testSamples = getSequenceStream(testData, clearEvaluationFeatures, corpusFormat);
    this.beamSize = Flags.getBeamsize(params);
    this.sequenceCodec = Flags.getSequenceCodec(params);
    if (params.getSettings().get("Types") != null) {
      String netypes = params.getSettings().get("Types");
      String[] neTypes = netypes.split(",");
      trainSamples = new SequenceSampleTypeFilter(neTypes, trainSamples);
      testSamples = new SequenceSampleTypeFilter(neTypes, testSamples);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * es.ehu.si.ixa.pipe.nerc.train.Trainer#train(opennlp.tools.util
   * .TrainingParameters)
   */
  public final SequenceLabelerModel train(final TrainingParameters params) {
    if (getSequenceLabelerFactory() == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractNameFinderTrainer must create and fill the AdaptiveFeatureGenerator features!");
    }
    SequenceLabelerModel trainedModel = null;
    SequenceLabelerEvaluator nerEvaluator = null;
    Set<String> trainSetVocabulary = null;
    trainSetVocabulary = getTrainingTokens();

    /*
    // START DEBUG ONLY
    System.out.println("\nH E A D E R");
    SequenceSample blaSample = null;
    ObjectStream<SequenceSample> blaTrainSamples;
    int i = 1;
    try {
		blaTrainSamples = getSequenceStream(trainData, clearTrainingFeatures, corpusFormat);
		while ((blaSample = blaTrainSamples.read()) != null) {
			System.out.println("Sample: " + blaSample.getTokens().toString());
			for (String x : blaSample.getTokens()) {
				System.out.println("  ~ "+ x);
				i += 1;
			}
		}
		System.out.println("Count: " + i);
		
	} catch (IOException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
    // END DEBUG ONLY
    */
    
    try {
      trainedModel = SequenceLabelerME.train(lang, null, trainSamples, params,
          nameClassifierFactory);
      SequenceLabelerME nerTagger = new SequenceLabelerME(trainedModel);
      nerEvaluator = new SequenceLabelerEvaluator(nerTagger, trainSetVocabulary);
      nerEvaluator.evaluate(testSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading traing and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    
    //System.out.println("Final Result: \n" + nerEvaluator2.getFMeasure());
    System.out.println();
    System.out.println("Total Word accuracy:\t" + nerEvaluator.getTotalWordAccuracy());
    System.out.println("Known Word accuracy:\t" + nerEvaluator.getKnownWordAccuracy());
    System.out.println("Unknown Word accuracy:\t" + nerEvaluator.getUnknownWordAccuracy());
    System.out.println("Total Word Count:\t" + nerEvaluator.getWordCount());
    System.out.println();

    
    /*
    // START DEBUG ONLY
	try {
		FileWriter writer = new FileWriter("DebugEval.txt", true);
		writer.write("ManifEntries:\t"+trainedModel.getNameFinderSequenceModel().getOutcomes());
		writer.write("\n BLA:\t" + nameClassifierFactory.createManifestEntries().size()); 
		System.out.println();
		//writer.write("Factory:\t" +trainedModel.getFactory().toString() +"\n");
		
		//for (String x : trainedModel.) {
		//	writer.write("  outcome: " + x);
		//}

		writer.close();
	} catch (IOException e) {
		e.printStackTrace();
    	}
    
    // END DEBUG ONLY
    */
    
    return trainedModel;
  }

  /**
   * Getting the stream with the right corpus format.
   * @param inputData
   *          the input data
   * @param clearFeatures clear the features
   * @param aCorpusFormat
   *          the corpus format
   * @return the stream from the several corpus formats
   * @throws IOException
   *           the io exception
   */
  public static ObjectStream<SequenceSample> getSequenceStream(final String inputData,
      final String clearFeatures, final String aCorpusFormat) throws IOException {
    ObjectStream<SequenceSample> samples = null;
    if (aCorpusFormat.equalsIgnoreCase("conll03")) {
      ObjectStream<String> nameStream = IOUtils.readFileIntoMarkableStreamFactory(inputData);
      samples = new CoNLL03Format(clearFeatures, nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("conll02")) {
      ObjectStream<String> nameStream = IOUtils.readFileIntoMarkableStreamFactory(inputData);
      samples = new CoNLL02Format(clearFeatures, nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("tabulated")) {
      ObjectStream<String> nameStream = IOUtils.readFileIntoMarkableStreamFactory(inputData);
      samples = new TabulatedFormat(clearFeatures, nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("lemmatizer")) {
      ObjectStream<String> seqStream = IOUtils.readFileIntoMarkableStreamFactory(inputData);
      samples = new LemmatizerFormat(clearFeatures, seqStream);
    } else {
      System.err.println("Test set corpus format not valid!!");
      System.exit(1);
    }
    return samples;
  }
 
  /**
   * Get the features which are implemented in each of the trainers extending
   * this class.
   * @return the features
   */
  public final SequenceLabelerFactory getSequenceLabelerFactory() {
    return nameClassifierFactory;
  }
  
  public final SequenceLabelerFactory setSequenceLabelerFactory(SequenceLabelerFactory tokenNameFinderFactory) {
    this.nameClassifierFactory = tokenNameFinderFactory;
    return nameClassifierFactory;
  }
  
  /**
   * Get the language.
   * @return the language
   */
  public final String getLanguage() {
    return lang;
  }

  /**
   * Set the language.
   * @param aLang
   *          the language
   */
  public final void setLanguage(final String aLang) {
    this.lang = aLang;
  }
  
  /**
   * Get the Sequence codec.
   * @return the sequence codec
   */
  public final String getSequenceCodec() {
    String seqCodec = null;
    if ("BIO".equals(sequenceCodec)) {
      seqCodec = BioCodec.class.getName();
    }
    else if ("BILOU".equals(sequenceCodec)) {
      seqCodec = BilouCodec.class.getName();
    }
    return seqCodec;
  }
  
  /**
   * Set the sequence codec.
   * @param aSeqCodec the sequence codec to be set
   */
  public final void setSequenceCodec(final String aSeqCodec) {
    this.sequenceCodec = aSeqCodec;
  }
  
  public final int getBeamSize() {
    return beamSize;
  }
  
  public final Set<String> getTrainingTokens() {
	  Set<String> trainingTokens = new HashSet<String>();
	  SequenceSample tS = null;
	  ObjectStream<SequenceSample> trainSetSamples;
	  try {
		  trainSetSamples = getSequenceStream(trainData, clearTrainingFeatures, corpusFormat);
		  while ((tS = trainSetSamples.read()) != null) {
			  for (String tok : tS.getTokens()) {
				  trainingTokens.add(tok);
			  }
		  }
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	  return trainingTokens;
  }

}
