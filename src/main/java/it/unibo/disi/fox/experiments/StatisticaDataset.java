package it.unibo.disi.experiments;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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

public class StatisticaDataset {

	static Configuration config;
	private static Logger logger = LoggerFactory.getLogger(DatasetStatistica.class);
	private static int numColonna = 10;
	public static final int CHECKPOINT = 100, QUERY_ATTEMPTS = 3, SLEEP = 60000;
	private static int numeroTot = 11;
	private static HashMap<String,Integer> mappa = new HashMap<String,Integer>();
	private static FileOutputStream fos;
	private static String[]rigaCompleta; 
	public static final String TOKEN_PREFIX_ATTRIBUTE = "_t_";
	public static final String NUMBER_URI_TOKENS = "__numberOfTokensInURI";
	public static final String NUMBER_URI_CAPITAL_TOKENS = "__numberOfTokensInURIStartingWithCapitalLetter";
	public static final String NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT = "__numberOfURITokensFoundInAbstract";
	public static final String __isDetectedBySENECA = "__isDetectedBySENECA";
	public static final String __isDetectedByORA = "__isDetectedByORA";
	static String dbpediaSparqlEndpoint = "https://dbpedia.org/sparql";
	
		public static void main(String args[]) throws Exception {
		//file di configurazione 
		Configurations configs = new Configurations();
		if (args.length > 1) {
			config = configs.properties(args[0]);
		} else {
			config = configs.properties("config.properties");
		}
		
		//inputFile = dataset.tsv
		String inputFile = config.getString("fileDaLeggere2");
		//risultato è il file finale completo con tutte le features
		String risultato = config.getString("fileStatistica");
				
		//PRIMA PASSATA MAPPA CON INTESTAZIONE
		scriviHeaderFile(inputFile, risultato, dbpediaSparqlEndpoint);
		logger.info("header write");	
		
		//SECONDA PASSATA PER RIEMPIRE IL FILE
		logger.info("numero totale colonne" + numeroTot);
		riempiFile();
		logger.info("FINE");
								
}
		
		private static void scriviHeaderFile(String entities, String outputFile, String dbpediaSPARQLEndpoint) throws IOException  {
			logger.info("Creating input dataset!");
			fos = new FileOutputStream(new File(outputFile), true);
			
			
			Scanner inputStream = null;
			try {
				inputStream = new Scanner(new File(entities));
				
			} catch (FileNotFoundException e) {
				logger.error("Error while reading {} {}", e.getMessage(), entities);
			}
			
			//QUERY PROPRIETA
			String queryStringProperties = "select ?p (count(DISTINCT ?o) AS ?c){ "
							+ "?s ?p ?o "
							+ "} "
							+ "GROUP BY ?p ";
					
			ParameterizedSparqlString pss = new ParameterizedSparqlString(queryStringProperties);
			int c = 0;
			boolean blocked = false;
			//scrivo l'header del file (intestazione)
			String HEADER_FILE = "entities" + "\t" + "class/instance" + "\t"+ "physicalObject" + "\t"+ "NUMBER_URI_TOKENS" + "\t" + "NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT" + "\t" + "NUMBER_URI_CAPITAL_TOKENS" + "\t" + "__isDetectedBySENECA_Class" + "\t" + "__isDetectedByORA_Class" + "\t" + "__isDetectedBySENECA_PO" + "\t" + "__isDetectedByORA_PO" + "\t" + "Abstract" + "\t" ;
			
			//MAPPA <campo,colonna>
			mappa.put("entities",0);
			mappa.put("class/instance",1);
			mappa.put("physicalObject",2);
			mappa.put("NUMBER_URI_TOKENS",3);
			mappa.put("NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT",4);
			mappa.put("NUMBER_URI_CAPITAL_TOKENS", 5);
			mappa.put("__isDetectedBySENECA_Class",6);
			mappa.put("__isDetectedByORA_Class",7);
			mappa.put("__isDetectedBySENECA_PO",8);
			mappa.put("__isDetectedByORA_PO",9);
			mappa.put("Abstract",10);
			
			
			while (inputStream.hasNextLine()) {
				if (c > 0 && c % CHECKPOINT == 0) {
					logger.info("Dumped {} entities", c);
				}
				c++;

				String[] splitta = inputStream.nextLine().split("\t");
				String currentEntityURI = splitta[0];
				
				pss.setIri("s", currentEntityURI);
				QueryExecution qExec = QueryExecutionFactory.sparqlService(dbpediaSPARQLEndpoint, pss.asQuery());
				boolean propertiesRetrieved = false;
				
				for (int i = 0; i < QUERY_ATTEMPTS && !propertiesRetrieved; i++) {
					try {
						ResultSet results = qExec.execSelect();
						while (results.hasNext()) {
							QuerySolution sol = results.nextSolution();
							String prop = sol.getResource("p").getURI();
							
							//inserisco la proprieta e il numero di colonna nella mappa
							mappa.put(prop,numColonna);
						
							//stringa HEADER_FILE
							if(!(HEADER_FILE.equals(prop))) {
								HEADER_FILE = HEADER_FILE + prop + "\t";
								//mi serve per sapere quante proprieta ci sono
								numeroTot++;
							}
						}
						//incremento il numero di colonna
						numColonna++;
						propertiesRetrieved = true;
					} catch (Exception e) {
						e.printStackTrace();
						try {
							logger.info("SLEEP"); 
							Thread.sleep(SLEEP);
							logger.info("RESUME");
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} finally {
						qExec.close();
					}	
				}

				if (!propertiesRetrieved) {
					logger.info("Could not retrieve properties for entity {}", currentEntityURI);
					blocked = true;
					break;
				}	
			}
			//scrivo HEADER su file 
			fos.write((HEADER_FILE + "\n").getBytes());
			logger.info("scrittura HEADER_FILE");		
		}
		
		
		private static void riempiFile() throws Exception {
			
			//file da cui leggo le istanze per Class/instance
			final  String FILE_HEADER = "URIEntità" + "\t" + "NomeClasse" + "\t" + "NUMBER_URI_TOKENS "+ "\t"+"NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT" + "\t"+ "NUMBER_URI_CAPITAL_TOKENS" + "\t" + "__isDetectedBySENECA"+ "\t"+ "__isDetectedByORA" +"\t" + "Descrizione"+"\n";
			Instances instances = WekaUtils.loadXRFFInstances(config.getString("classinstance_instances"));
			FileWriter fileWriter = new FileWriter(config.getString("classificationclassinstance2"));
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.print(FILE_HEADER);
			
			// file da cui leggo le istanze per PO
		    final String FILE_HEADER2 = "URIEntità" + "\t" + "Tipo Oggetto"+"\t" + "__isDetectedBySENECA"+ "\t"+ "__isDetectedByORA" + "\t" + "Descrizione"+"\n";
						Instances instances2 = WekaUtils.loadXRFFInstances(config.getString("physicalobject_instances"));
						FileWriter fileWriter2 = new FileWriter(config.getString("classificationphysicalobject2"));
			 			PrintWriter printWriter2 = new PrintWriter(fileWriter2);
			 			printWriter2.print(FILE_HEADER2);
						logger.info("carico file e instances ");
			
						// Create a new classifier CLASS/INSTANCE
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
			
					
						// Create a new classifier PO
						SMO svm2 = new SMO();			
						// remove the attribute id from instances
						Attribute idAtt2 = instances2.attribute("ID");
						FilteredClassifier fc2 = new FilteredClassifier();
						fc2.setClassifier(svm2);
						logger.info("nuovo classificatore");
						Remove rm2 = new Remove();
						rm2.setAttributeIndicesArray(new int[] { idAtt2.index() });
						fc2.setFilter(rm2);
						svm2.buildClassifier(instances2);
						logger.info("build classifier");
						
						
						//caricamento del file TSV
						String nomeFile = config.getString("fileDaLeggere2");
						String line = ""; 
						
						Scanner inputStream2 = null;
						try {
							inputStream2 = new Scanner(new File(nomeFile));
							
						} catch (FileNotFoundException e) {
							logger.error("Error while reading {} {}", e.getMessage(), nomeFile);
						}
						
						
						
							SENECA seneca = SENECA.getInstance(config.getString("SENECA_classes"), config.getString("SENECA_PhysicalObjects"));
							Tipalo tipalo = Tipalo.getInstance(config.getString("Tipalo_classes"), config.getString("Tipalo_PhysicalObjects"));

							//CLASS
							ArrayList<Attribute> atts = new ArrayList<>();
							for(int i=0;i<instances.numAttributes();i++) {
								atts.add(instances.attribute(i));
							}
							logger.info("Number of attributes: "+atts.size());
							
							//PO
							ArrayList<Attribute> atts2 = new ArrayList<>();
							for(int i=0;i<instances2.numAttributes();i++) {
								atts2.add(instances2.attribute(i));
							}
							
							//LEGGO il FILE dataset.tsv
							while (inputStream2.hasNextLine()) {
									String[] splitta2 = inputStream2.nextLine().split("\t");
									String uriEntity = splitta2[0];
					            	
					                JSONObject propriet = new JSONObject(splitta2[1]);
					                
					                String _abstaract = " ";
					               
					                
									//splitta2[2];
									rigaCompleta = new String[numeroTot];
									
									//CLASS
									SparseInstance si = new SparseInstance(instances.numAttributes());
									propriet.keys().forEachRemaining(k -> {
										Attribute a = instances.attribute(k);
										
										if (a != null) {
											si.setValue(a, propriet.getInt(k));
											//logger.trace("setto il token per ogni attributo");
										}
									});
									
									//PO
									SparseInstance si2 = new SparseInstance(instances2.numAttributes());
									propriet.keys().forEachRemaining(k2 -> {
										Attribute a2 = instances2.attribute(k2);
										if (a2 != null) {
											si2.setValue(a2, propriet.getInt(k2));
										}
									});
									
									//CLASS
									List<String> tokens = Utils.tokenize(_abstaract);
									//TOKEN PREFIX ATTRIBUTE
									for (String token : tokens) {
										Attribute a = instances.attribute(TOKEN_PREFIX_ATTRIBUTE + token);
									
										if (a != null) {
											si.setValue(a, 1.0);
											//logger.trace("setto il token TOKEN_PREFIX_ATTRIBUTE");
										}
									}
									
									//PO
									List<String> tokens2 = Utils.tokenize(_abstaract);
									
									for (String token : tokens2) {
										Attribute a2 = instances2.attribute(TOKEN_PREFIX_ATTRIBUTE + token);
										
										if (a2 != null) {
											si2.setValue(a2, 1.0);
										}
									}

									String[] uriToken = Utils.getUriTokens(uriEntity);
									//getNUMBER URI TOKENS
									int n1 = Utils.getNumberOfURITokens(uriToken);
									
									//getNUMBER_OF_TOKENS_FOUND_IN_ABSTRACT
									int n2 = Utils.getNumberOfURITokensInAbstract(uriToken, tokens);
									
									//getNUMBER_URI_CAPITAL_TOKENS
									int n3 = Utils.getNumberOfURITokensStartingWithCapitalCharacters(uriToken);
									
										//NUMBER URI TOKENS
										Attribute b = instances.attribute(NUMBER_URI_TOKENS );
										if (b != null) {
											si.setValue(b, n1);
											//logger.trace("setto il token NUMBER_URI_TOKENST");
										}
									
										//NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT
										Attribute d = instances.attribute( NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT );
										if (d != null) {
											si.setValue(d, n2);
											//logger.trace("setto il token NUMBER_OF_TOKENS_FOUND_IN_ABSTRACT");
										}
										
										//NUMBER_URI_CAPITAL_TOKENS
										Attribute c = instances.attribute(NUMBER_URI_CAPITAL_TOKENS  );
										if (c != null) {
											si.setValue(c, n3);
											//logger.trace("setto il token NUMBER_URI_CAPITAL_TOKENS");
										}
									
										String seneca_output;
									   //SENECA classe
										Attribute a = instances.attribute(__isDetectedBySENECA );
									     if(seneca.isClass(uriEntity)) { 
									    	 seneca_output = "YES";
									    	si.setValue(a, a.indexOfValue("YES"));
									       //logger.info("è una classe");
									    } else {
									    	seneca_output = "NO";
									    	si.setValue(a, a.indexOfValue("NO"));
									       //logger.info("non è una classe");
									    }
									
									    String tipalo_output;
									    //TIPALO
										Attribute at = instances.attribute(__isDetectedByORA );
									    if(tipalo.isClass(uriEntity)) {
									    	tipalo_output= "YES";
									       si.setValue(at, at.indexOfValue("YES"));
									       //logger.info("è una classe");
									     } else {
									    	 tipalo_output = "NO";
									    	 si.setValue(at, at.indexOfValue("NO"));
									    	 //logger.info("non è una classe");
									    }
			
									    Instances dataUnlabeled = new Instances("TestInstances",atts, 0);
									    dataUnlabeled.add(si);
									    dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);  
									    
									
									 
									double classe = svm.classifyInstance(dataUnlabeled.firstInstance());
									
									String seneca_PO ;
									  //SENECA physical object
									Attribute a2 = instances2.attribute(__isDetectedBySENECA );
								     if(seneca.isPhysicalObject(uriEntity)) { 
								    	 seneca_PO = "YES";
								    	 si2.setValue(a2, a2.indexOfValue("YES"));
								       logger.info("è un oggetto fisico");
								    } else {
								    	seneca_PO = "NO";
								    	si2.setValue(a2, a2.indexOfValue("NO"));
								       logger.info("non è un oggetto fisico");
								    }
										
								    String tipalo_PO;
								    //TIPALO
									Attribute at2 = instances2.attribute(__isDetectedByORA );
								    if(tipalo.isPhysicalObject(uriEntity)) {
								    	tipalo_PO = "YES";
								       si2.setValue(at2, at2.indexOfValue("YES"));
								       logger.info("è un oggetto fisico");
								     } else {
								    	 tipalo_PO = "NO";
								    	 si2.setValue(at2, at2.indexOfValue("NO"));
								    	 logger.info("non è un oggetto fisico");
								    }
									
						
									Instances dataUnlabeled2 = new Instances("TestInstances",atts2, 0);
								    dataUnlabeled2.add(si2);
								    dataUnlabeled2.setClassIndex(dataUnlabeled2.numAttributes() - 1);        
								    
								 	
								    double tipo = svm2.classifyInstance(dataUnlabeled2.firstInstance());
									
									//associare il double all'etichetta
									String nomeClasse = instances.classAttribute().value((int) classe);
									String tipoOggetto = instances2.classAttribute().value((int) tipo);
									Integer numURIToken = n1;
									Integer numOfTokenFoundInAbstract = n2;
									Integer uriCapital = n3;
									
									//assegno i valori alla riga corrente 
									rigaCompleta[0] = uriEntity;
									rigaCompleta[1] = nomeClasse;
									rigaCompleta[2] = tipoOggetto;
									rigaCompleta[3] = numURIToken.toString();
									rigaCompleta[4] = numOfTokenFoundInAbstract.toString();
									rigaCompleta[5] = uriCapital.toString();
									rigaCompleta[6] = seneca_output;
									rigaCompleta[7] = tipalo_output;
									rigaCompleta[8] = seneca_PO;
									rigaCompleta[9] = tipalo_PO;
									rigaCompleta[10]= _abstaract;
									
							
									printWriter.print(uriEntity + "\t" + nomeClasse + "\t"+ numURIToken.toString() + "\t"+ numOfTokenFoundInAbstract.toString() + "\t" + uriCapital.toString()  + "\t" + seneca_output  + "\t"+ tipalo_output + "\t"+_abstaract +"\n");
									printWriter2.print(uriEntity + "\t" + tipoOggetto + "\t"+ seneca_PO +"\t"+ tipalo_PO +"\t"+ _abstaract+ "\n");
									
									
									//QUERY PROPRIETA
									String queryStringProperties2 = "select ?p (count(DISTINCT ?o) AS ?c){ "
													+ "?s ?p ?o "
													+ "} "
													+ "GROUP BY ?p ";
											
									ParameterizedSparqlString pss2 = new ParameterizedSparqlString(queryStringProperties2);
									
									pss2.setIri("s", uriEntity);
									QueryExecution qExec2 = QueryExecutionFactory.sparqlService(dbpediaSparqlEndpoint, pss2.asQuery());
									boolean propertiesRetrieved = false;
									String rigaPerFile = "";
								
									//SCORRO I RISULTATI DELLA QUERY 
									for (int i = 0; i < QUERY_ATTEMPTS && !propertiesRetrieved; i++) {
										try {
							
											//rigaPerFile = "";
											Integer num2 = null;
											ResultSet results2 = qExec2.execSelect();
											while (results2.hasNext()) {
												
												QuerySolution sol2 = results2.nextSolution();
												
												String prop2 = sol2.getResource("p").getURI();
												//ottengo il numero di proprieta
												num2 = (sol2.getLiteral("?c").getInt());
												String numero = num2.toString();
												
												//scorro la mappa per trovare la proprieta 
												if(mappa.containsKey(prop2)) {
													
													
													//ottengo il numero della colonna
													int colonna = mappa.get(prop2);
													
													//logger.info("numero " + numero + " colonna " + colonna);
													//inserisco il numero della proprieta nella posizione (nColonna) dell'array
													rigaCompleta[colonna] = numero;
													
												} 
												
												
											}
											
											propertiesRetrieved = true;
										} catch (Exception e) {
											e.printStackTrace();
											try {
												logger.info("SLEEP"); 
												Thread.sleep(SLEEP);
												logger.info("RESUME");
											} catch (InterruptedException e1) {
												e1.printStackTrace();
											}
										} finally {
											qExec2.close();
										}
										
									}
									
							
									//assegno i valori alla riga 
									for (int j=0; j<rigaCompleta.length;j++) {
										rigaPerFile = rigaPerFile + rigaCompleta[j] + "\t";
										
									}
									
									
									rigaPerFile = rigaPerFile + "\n";
									//scrivo la riga sul file 
									fos.write((rigaPerFile).getBytes());
									logger.info("riga scritta su file");
									
							
					            } //fine lettura riga del file di Input 
							
					            printWriter.close();
					            printWriter2.close();
					            logger.info("scrittura su file conclusa");
		
						}//fine try 
		}//fine procedura 


