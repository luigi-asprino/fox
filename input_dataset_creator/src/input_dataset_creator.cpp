//============================================================================
// Name        : input_dataset_creatore.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <map>
#include <string>
#include <iostream>
#include <HDTManager.hpp>
#include <fstream>
#include <algorithm>

using namespace std;
using namespace hdt;

int create_input_dataset(char* hdt_path, char* fileout) {
	// Load HDT file
	HDT *hdt = HDTManager::mapHDT(hdt_path);

	ofstream myfile;
	myfile.open(fileout);

	IteratorTripleString *it_all = hdt->search("", "", "");
	cout << "Total number of triples " << it_all->estimatedNumResults() << endl;
	delete it_all;

	// Enumerate all triples matching a pattern ("" means any)
	IteratorTripleString *it = hdt->search("",
			"http://dbpedia.org/ontology/wikiPageID", "");
	int entity_number = 0;
	while (it->hasNext()) {
		if (entity_number % 10000 == 0) {
			cout << entity_number << endl;
		}
		entity_number++;

		TripleString *triple = it->next();

		IteratorTripleString *it_red = hdt->search(triple->getSubject().data(),
				"http://dbpedia.org/ontology/wikiPageRedirects", "");

		if (!it_red->hasNext()) { // check if it is a redirect page

			myfile << triple->getSubject();
			myfile << "\t";

			// get features
			map<string, int> properties;
			IteratorTripleString *it_properties = hdt->search(
					triple->getSubject().data(), "", "");

			while (it_properties->hasNext()) {
				TripleString *triple_properties = it_properties->next();
				int count = 0;
				map<string, int>::iterator it = properties.find(
						triple_properties->getPredicate());
				if (it != properties.end()) {
					count = properties.lower_bound(
							triple_properties->getPredicate())->second;
				}
				count++;
				properties.erase(triple_properties->getPredicate());
				properties.insert(
						pair<string, int>(triple_properties->getPredicate(),
								count));
			}
			map<string, int>::iterator itr;
			myfile << "{";
			for (itr = properties.begin(); itr != properties.end(); ++itr) {
				if (itr != properties.begin()) {
					myfile << ",";
				}
				myfile << "\"" << itr->first << "\":" << itr->second;
			}
			myfile << "}";
			delete it_properties;

			// get abstract
			myfile << "\t";
			IteratorTripleString *it_abstract = hdt->search(
					triple->getSubject().data(),
					"http://dbpedia.org/ontology/abstract", "");
			while (it_abstract->hasNext()) {
				TripleString *triple_abstract = it_abstract->next();
				string _abstract = triple_abstract->getObject();
				std::replace(_abstract.begin(), _abstract.end(), '\t', ' ');
				std::replace(_abstract.begin(), _abstract.end(), '\n', ' ');
				myfile << _abstract;
			}
			delete it_abstract;

			myfile << "\n";
		}

		delete it_red;
	}
	delete it; // Remember to delete iterator to avoid memory leaks!

	myfile.close();

	delete hdt; // Remember to delete instance when no longer needed!

	return 0;

}

int test() {

	string _abstract = "parola\tparola2\nparola3";
	cout << _abstract << endl;

	std::replace(_abstract.begin(), _abstract.end(), '\t', ' ');
	std::replace(_abstract.begin(), _abstract.end(), '\n', ' ');

	cout << _abstract << endl;

	cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!

	map<string, int> my_map;

	my_map.insert(pair<string, int>("Ab", 1));
	my_map.insert(pair<string, int>("Abb", 2));
	my_map.insert(pair<string, int>("Abc", 3));
	my_map.insert(pair<string, int>("Abd", 4));
	my_map.insert(pair<string, int>("Ac", 5));
	my_map.insert(pair<string, int>("Ad", 5));
	my_map.erase("Ab");

	my_map.insert(pair<string, int>("Ab", 0));

	map<string, int>::iterator itr;
	cout << "\nThe map gquiz1 is : \n";
	cout << "\tKEY\tELEMENT\n";
	for (itr = my_map.begin(); itr != my_map.end(); ++itr) {
		cout << '\t' << itr->first << '\t' << itr->second << '\n';
	}
	cout << endl;

	cout << my_map.lower_bound("Ab")->second << endl;
	cout << "Low" << endl;
	cout << my_map.lower_bound("ED")->second << endl;
	return 0;
}

int main(int argc, char** argv) {

	cout << "HDT file Path: " << argv[1] << endl;
	cout << "Output file Path: " << argv[2] << endl;
	create_input_dataset(argv[1], argv[2]);

	return 0;
}
