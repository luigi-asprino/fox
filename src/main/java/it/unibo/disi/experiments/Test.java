package it.unibo.disi.experiments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibo.disi.utils.Utils;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.unsupervised.attribute.Remove;

public class Test {

	public static final String TOKEN_PREFIX_ATTRIBUTE = "_t_";
	public static final String NUMBER_URI_TOKENS = "__numberOfTokensInURI";
	public static final String NUMBER_URI_CAPITAL_TOKENS = "__numberOfTokensInURIStartingWithCapitalLetter";
	public static final String NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT = "__numberOfURITokensFoundInAbstract";

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

			svm.buildClassifier(instances);

			for (int i = 0; i < 1000; i++) {
				String uriEntity = "";
				Map<String, Integer> proprieta = new HashMap<>();
				String _abstaract = "";
				SparseInstance si = new SparseInstance(instances.numAttributes());
				proprieta.forEach((prop, valore) -> {
					Attribute a = instances.attribute(prop);
					if (a != null) {
						si.setValue(a, valore);
					}
				});

				List<String> tokens = Utils.tokenize(_abstaract);
				for (String token : tokens) {
					Attribute a = instances.attribute(TOKEN_PREFIX_ATTRIBUTE + token);
					if (a != null) {
						si.setValue(a, 1.0);
					}
				}

				String[] uriToken = Utils.getUriTokens(uriEntity);

				int n1 = Utils.getNumberOfURITokens(uriToken);

				// Utils.getNumberOfURITokens(TOKEN_PREFIX_ATTRIBUTE+uriTokens)

				double classe = svm.classifyInstance(si);

				// uriEntity classe
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
