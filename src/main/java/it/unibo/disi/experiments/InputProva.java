package it.unibo.disi.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.simple.JSONObject;

public class InputProva {
	
	static ArrayList<String> lista = new ArrayList<String>();
	static ArrayList<String> totale = new ArrayList<String>();
	private static PrintWriter printWriter;
	 

	public static void main (String args[]) throws IOException {
		
		String nomeFile = "inputFile.txt";
	
		//scrivi i risultati su file 
		  PrintWriter outputStream = null;
		  try {
			  outputStream = new PrintWriter(nomeFile);
		  }catch(FileNotFoundException e) {
				System.out.println("Errore nell'apertura del file " + e + nomeFile);		
			} 
		
	
		  boolean doQuery= true;
		  int i = 0 ;
		  
		  
		  while(doQuery) {

		  //query parametrica
		  ParameterizedSparqlString parSp = new ParameterizedSparqlString("PREFIX dbo: <http://dbpedia.org/ontology/> select ?s {?s dbo:wikiPageID ?o }limit 10000 offset " + i*10000 );
		  Query querySoggetti = parSp.asQuery();
		  QueryExecution qExec0 = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", querySoggetti);
		  
		  
		  try {
				
				ResultSet results = qExec0.execSelect();
				  if (!(results.hasNext())) {
				    	System.out.println("non ci sono più risultati");
				    	doQuery=false;
				    	outputStream.close();
				    }
				while (results.hasNext()) {
					QuerySolution sol0 = results.nextSolution();
					System.out.println("query:" + sol0);
					//scrivo sul file il risultato della query 
					outputStream.print(sol0.get("?s").toString()+ "\n"); 
					  
				}//while
					
			} finally {
				
				qExec0.close();
				System.out.println("-----fine query-----");	
			} //finally
		  
				 i++;
			
	 } //while
		  
		
		//lettura del file coi dati (il file contiene solo i soggetti s)
		leggiFile(nomeFile); 
		
	}
	
	//procedura per lettura del file 
	static void leggiFile(String file) throws IOException {
		
		 //creo il file dove salvo i risultati 
		FileWriter fileWriter = new FileWriter("/Users/serenapasserini/eclipse-workspace/com.progettoProva/risultato.tsv");
	    printWriter = new PrintWriter(fileWriter);
	 
	    
	    //file in cui sono salvati tutti i soggetti 
		String nomeFile = file;
		Scanner inputStream = null;
	
		try {	
			inputStream = new Scanner(new File(nomeFile));
	
		}catch(FileNotFoundException e) {
			System.out.println("Errore nell'apertura del file " + e + nomeFile);		
		}
		//inserisco le righe del file nell'arraylist 
		while(inputStream.hasNextLine()) {
			
			lista.add((inputStream.nextLine().toString()));
			System.out.println("aggiungi");
		}
		inputStream.close();
			
	
			//PRIMA QUERY PER IL NUMERO DI PROPRIETA
			//imposto la query parametrica
			ParameterizedSparqlString pss = new ParameterizedSparqlString("select ?p (count(DISTINCT ?o) AS ?c){ ?s ?p ?o } GROUP BY ?p ");
			
			//scorro la lista dei soggetti
			for (String elem : lista) {
				
				System.out.println("soggetto :" + elem);
				totale.add(elem);
				//setto l'IRI
				  pss.setIri("s", elem);
				
				 //converto in query 
				 Query query = pss.asQuery();
			
				 //passo la query allo sparqlendpoint
				 QueryExecution qExec = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", query);
				 
				 JSONObject proprieta = new JSONObject();
				 String astratto = null;

				    
				 
				 //lettura risultati query
					try {
						
						ResultSet results = qExec.execSelect();
						while (results.hasNext()) {
							QuerySolution sol = results.nextSolution();
							//ottengo la proprieta
							String prop = (sol.get("?p")).toString(); 
							//ottengo il numero di proprieta
							int num = (sol.getLiteral("?c").getInt());					
							proprieta.put(prop,num); 
						} //while
					} finally {
						qExec.close();
				} //finally
				
				System.out.println("lista di josn" + proprieta);
					
				//SECONDA QUERY PER GLI ABSTRACT
					ParameterizedSparqlString pssAbstract = new ParameterizedSparqlString("PREFIX dbo: <http://dbpedia.org/ontology/> select distinct ?abstract  { ?s dbo:abstract ?abstract FILTER (lang(?abstract) = 'en')}");

						//setto l'IRI
						pssAbstract.setIri("s", elem);
						
						 //converto in query 
						 Query query2 = pssAbstract.asQuery();
					
						 //passo la query allo sparqlendpoint
						 QueryExecution qExec2 = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", query2);
						 
						 //lettura risultati query
							try {
								
								ResultSet results = qExec2.execSelect();
								while (results.hasNext()) {
									QuerySolution sol = results.nextSolution();
									
								 System.out.println("abstract: " + sol.get("?abstract"));
									astratto = sol.get("?abstract").toString().replace('\n',' ').replace('\t', ' ');
									
									System.out.println(elem + "\t" + proprieta.toString() + "\t" + sol.get("?abstract").toString()+"\n");
										
								}
							} finally {
								qExec.close();
									
						}
							printWriter.print(elem + "\t" + proprieta.toString() + "\t" + astratto +"\n");
							
							
			}//for di lista		
			printWriter.close();
		}
		   
		
	}
	
				

        
		
		
	





