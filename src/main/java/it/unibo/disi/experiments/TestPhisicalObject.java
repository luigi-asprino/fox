package it.unibo.disi.experiments;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.json.JSONObject;

import it.unibo.disi.features.SENECA;
import it.unibo.disi.features.Tipalo;
import it.unibo.disi.utils.Utils;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.unsupervised.attribute.Remove;

public class TestPhisicalObject {

	public static final String TOKEN_PREFIX_ATTRIBUTE = "_t_";
	public static final String NUMBER_URI_TOKENS = "__numberOfTokensInURI";
	public static final String NUMBER_URI_CAPITAL_TOKENS = "__numberOfTokensInURIStartingWithCapitalLetter";
	public static final String NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT = "__numberOfURITokensFoundInAbstract";
	public static final String __isDetectedBySENECA = "__isDetectedBySENECA";
	public static final String __isDetectedByORA = "__isDetectedByORA";
	

	public static void main(String[] args) {
		try {

			// Load instances
			// Note: pass the filepath of the XRFF as main parameter
			Instances instances = WekaUtils.loadXRFFInstances("/Users/serenapasserini/eclipse-workspace/fox/physicalObject.xrff");
		
			System.out.println("carico file e instances " + instances);
			
			// Create a new classifier
			SMO svm = new SMO();
			System.out.println("classificatore Svm");

			// remove the attribute id from instances
			Attribute idAtt = instances.attribute("ID");
			System.out.println("idAttribute");
			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(svm);
			System.out.println("nuovo classificatore");
			Remove rm = new Remove();
			rm.setAttributeIndicesArray(new int[] { idAtt.index() });
			fc.setFilter(rm);
			System.out.println("filtro");

			svm.buildClassifier(instances);
			System.out.println("build classifier");
			
			
			//caricamento del file TSV
			String nomeFile = "/Users/serenapasserini/eclipse-workspace/fox/fileTSV.tsv";
			String line = ""; 
			try (BufferedReader br = new BufferedReader(new FileReader(nomeFile))) {

				Configurations configs = new Configurations();
				Configuration config;
				if (args.length > 1) {
					config = configs.properties(args[0]);
				} else {
					
					config = configs.properties("config.properties");
				}
			
			SENECA seneca = SENECA.getInstance(config.getString("SENECA_classes"), config.getString("SENECA_PhysicalObjects"));
			Tipalo tipalo = Tipalo.getInstance(config.getString("Tipalo_classes"), config.getString("Tipalo_PhysicalObjects"));

				
				
		            while ((line = br.readLine()) != null) {
		            	String [] array = line.split("\t");
		               System.out.println(line);
		               
		               String uriEntity = array[0];
		               JSONObject propriet = new JSONObject(array[1]);
		               
						
						System.out.println("mappa di proprieta");
						String _abstaract = array[2];
						SparseInstance si = new SparseInstance(instances.numAttributes());
						System.out.println("numero attributi");
						

						propriet.keys().forEachRemaining(k -> {
							Attribute a = instances.attribute(k);
							System.out.println("per ogni attributo");
							if (a != null) {
								si.setValue(a, propriet.getInt(k));
								System.out.println("setto il valore");
							}
						});
						
						List<String> tokens = Utils.tokenize(_abstaract);
						System.out.println("tokens");
						for (String token : tokens) {
							Attribute a = instances.attribute(TOKEN_PREFIX_ATTRIBUTE + token);
							System.out.println("attributo del token");
							if (a != null) {
								si.setValue(a, 1.0);
								System.out.println("setto il token");
							}
						}

						
						String[] uriToken = Utils.getUriTokens(uriEntity);
						System.out.println("uri token");

						//SENECA
						for (String token : tokens) {
							Attribute a = instances.attribute( __isDetectedBySENECA + token);
						    if(seneca.isPhysicalObject(token)) {
						       a.indexOfValue("YES");
						       System.out.println("è un oggetto fisico");
						    } else {
						      // a.indexOfValue("NO");
						       System.out.println("NON è un oggetto fisico");
						    }
						}
						
						//TIPALO
						for (String token2 : tokens) {
							 Attribute a = instances.attribute(__isDetectedByORA + token2);
						    if(tipalo.isPhysicalObject(token2)) { 
						       a.indexOfValue("YES");
						       System.out.println("è un oggetto fisico");
						    } else {
						       
						      // a.indexOfValue("NO");
						       System.out.println("NON è un oggetto fisico");
						    }
						}
						
			
					   /*
						double classe = svm.classifyInstance(si);
						System.out.println("classe");
                     
						//associare il double all'etichetta
						String nomeClasse = instances.classAttribute().value((int) classe);
						
						
						//scrittura risultati su file TSV (uriEntita, nomeClasse) FATTO
						FileWriter fileWriter = new FileWriter("/Users/serenapasserini/eclipse-workspace/fox/classificazione.tsv");
	 	    			printWriter = new PrintWriter(fileWriter);
	 	    			printWriter.print(FILE_HEADER);
	 	    			
	 	    			//per scrivere
	 	    			printWriter.print(uriEntity + "\t" + nomeClasse +"\n");
	 	    			
						printWriter.close();
						
						*/
		            }
						
			 }
			
			
			

		} catch (Exception e) {
			e.printStackTrace();
		
	}
	
		
	}
}
