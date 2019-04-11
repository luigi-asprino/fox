package it.unibo.disi.experiments;

import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class WekaExperiments {

	public static void main(String[] args) {

		try {
			Instances instances = WekaUtils.loadXRFFInstances(args[0]);
			SMO svm = new SMO();

			Attribute idAtt = instances.attribute("ID");

			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(svm);
			Remove rm = new Remove();
			rm.setAttributeIndicesArray(new int[] { idAtt.index() });
			fc.setFilter(rm);

			Evaluation eval = new Evaluation(instances);

			eval.crossValidateModel(fc, instances, 10, new Random(System.currentTimeMillis()));

			System.out.println(eval.toSummaryString("\nResults\n\n", false));
			double[][] cm = eval.confusionMatrix();
			System.out.println(String.format("%d\t%d\n%d\t%d\n", (int) cm[0][0], (int) cm[0][1], (int) cm[1][0], (int) cm[1][1]));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}