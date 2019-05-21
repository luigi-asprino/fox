package it.unibo.disi.fox.experiments.fdistinctions;

import java.util.Random;

import it.unibo.disi.fox.fdistinctions.experiments.WekaUtils;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class WekaExperimentsTest {

	public static void main(String[] args) {

		try {

			// Load instances
			// Note: pass the filepath of the XRFF as main parameter
			Instances instances = WekaUtils.loadXRFFInstances(args[0]);

			// Create a new classifier
			SMO svm = new SMO();

			// remove the attribute id from instances
			Attribute idAtt = instances.attribute("ID");
			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(svm);
			Remove rm = new Remove();
			rm.setAttributeIndicesArray(new int[] { idAtt.index() });
			fc.setFilter(rm);
			
			
			System.out.println(instances.classAttribute().value((int) 0));
			System.out.println(instances.classAttribute().value((int) 1));
			
			
			// SparseInstance si = new SparseInstance(instances.numAttributes());
			// si.setValue(instances.attribute("http://xmlns.com/foaf/0.1/givenName"), 200);

			// 10 fold cross-validation of SVM on dataset input dataset
			Evaluation eval = new Evaluation(instances);
			eval.crossValidateModel(fc, instances, 2, new Random(System.currentTimeMillis()));
			System.out.println(eval.toSummaryString("\nResults\n\n", false));
			double[][] cm = eval.confusionMatrix();
			System.out.println(String.format("%d\t%d\n%d\t%d\n", (int) cm[0][0], (int) cm[0][1], (int) cm[1][0], (int) cm[1][1]));
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
