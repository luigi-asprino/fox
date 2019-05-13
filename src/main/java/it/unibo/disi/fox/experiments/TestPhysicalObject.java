package it.unibo.disi.experiments;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.disi.features.SENECA;
import it.unibo.disi.features.Tipalo;
import it.unibo.disi.utils.Utils;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.unsupervised.attribute.Remove;

public class TestPhysicalObject {

	public static final String TOKEN_PREFIX_ATTRIBUTE = "_t_";
	public static final String __isDetectedBySENECA = "__isDetectedBySENECA";
	public static final String __isDetectedByORA = "__isDetectedByORA";
	private static final String FILE_HEADER = "URIEntità" + "\t" + "Tipo Oggetto"+"\t" + "Descrizione"+"\n";
	private static Logger logger = LoggerFactory.getLogger(TestClassInstance.class);

	public static void main(String[] args) {
		try {

			//file di configurazione
			Configurations configs = new Configurations();
			Configuration config;
			if (args.length > 1) {
				config = configs.properties(args[0]);
			} else {
				
				config = configs.properties("config.properties");
			}
			
			// Load instances
			// Note: pass the filepath of the XRFF as main parameter
			Instances instances = WekaUtils.loadXRFFInstances(config.getString("physicalobject_instances"));
			FileWriter fileWriter = new FileWriter(config.getString("classificationphysicalobject"));
 			PrintWriter printWriter = new PrintWriter(fileWriter);
 			printWriter.print(FILE_HEADER);
			logger.info("carico file e instances ");
			
			// Create a new classifier
			SMO svm = new SMO();			

			// remove the attribute id from instances
			Attribute idAtt = instances.attribute("ID");
			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(svm);
			logger.info("nuovo classificatore");
			Remove rm = new Remove();
			rm.setAttributeIndicesArray(new int[] { idAtt.index() });
			fc.setFilter(rm);
		

			svm.buildClassifier(instances);
			logger.info("build classifier");
			
			
			//caricamento del file TSV
			String nomeFile = config.getString("inputfile");
			String line = ""; 
			try (BufferedReader br = new BufferedReader(new FileReader(nomeFile))) {

			
			SENECA seneca = SENECA.getInstance(config.getString("SENECA_classes"), config.getString("SENECA_PhysicalObjects"));
			Tipalo tipalo = Tipalo.getInstance(config.getString("Tipalo_classes"), config.getString("Tipalo_PhysicalObjects"));

			ArrayList<Attribute> atts = new ArrayList<>();
			for(int i=0;i<instances.numAttributes();i++) {
				atts.add(instances.attribute(i));
			}
				
				
		            while ((line = br.readLine()) != null) {
		            	String [] array = line.split("\t");
		               
		               
		               String uriEntity = array[0];
		               JSONObject propriet = new JSONObject(array[1]);
		               
						
						logger.info("mappa di proprieta");
						String _abstaract = array[2];
						SparseInstance si = new SparseInstance(instances.numAttributes());
					
						propriet.keys().forEachRemaining(k -> {
							Attribute a = instances.attribute(k);
							if (a != null) {
								si.setValue(a, propriet.getInt(k));
							
							}
						});
						
						List<String> tokens = Utils.tokenize(_abstaract);
						
						for (String token : tokens) {
							Attribute a = instances.attribute(TOKEN_PREFIX_ATTRIBUTE + token);
							
							if (a != null) {
								si.setValue(a, 1.0);
							
							}
						}
						
						  //SENECA physical object
						Attribute a = instances.attribute(__isDetectedBySENECA );
					     if(seneca.isPhysicalObject(uriEntity)) { 
					    	 si.setValue(a, a.indexOfValue("YES"));
					       logger.info("è un oggetto fisico");
					    } else {
					    	si.setValue(a, a.indexOfValue("NO"));
					       logger.info("non è un oggetto fisico");
					    }
											
					    //TIPALO
						Attribute at = instances.attribute(__isDetectedByORA );
					    if(tipalo.isPhysicalObject(uriEntity)) {
					       si.setValue(at, at.indexOfValue("YES"));
					       logger.info("è un oggetto fisico");
					     } else {
					       
					    	 si.setValue(at, at.indexOfValue("NO"));
					    	 logger.info("non è un oggetto fisico");
					    }
						
			
						Instances dataUnlabeled = new Instances("TestInstances",atts, 0);
					    dataUnlabeled.add(si);
					    dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);        
					    
					 	
					    double tipo = svm.classifyInstance(dataUnlabeled.firstInstance());
				 
					    //associare il double all'etichetta
					    String tipoOggetto = instances.classAttribute().value((int) tipo);
 	    			
					    //scrittura su file 
					    printWriter.print(uriEntity + "\t" + tipoOggetto + "\t" + _abstaract+ "\n");
 	
						
		            }
		            printWriter.close();
		            logger.info("scrittura su file conclusa");
						
			 }
			
			
			

		} catch (Exception e) {
			e.printStackTrace();
		
	}
	
		
	}
}
