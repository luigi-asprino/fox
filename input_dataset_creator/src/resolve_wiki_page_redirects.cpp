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

int create_input_dataset(char* hdt_path, char* file_in, char* file_out) {
	// Load HDT file
	HDT *hdt = HDTManager::mapHDT(hdt_path);

	cout << "Reading " << file_in << endl;
	string line;
	ifstream myfile(file_in);

	ofstream file_out_stream;
	file_out_stream.open(file_out);

	int entity_number = 0;

	if (myfile.is_open()) {
		while (getline(myfile, line)) {

			if (entity_number % 10000 == 0) {
				cout << entity_number << endl;
			}
			entity_number++;

			string uriEntity = line.substr(0, line.find("\t"));
			IteratorTripleString *it = hdt->search(uriEntity.data(),
					"http://dbpedia.org/ontology/wikiPageRedirects", "");
			if (!it->hasNext()) {
				file_out_stream << line << "\n";
			}
			delete it;
		}
		myfile.close();
	} else {
		cout << "Unable to open file " << file_in << endl;
	}
	file_out_stream.close();
	delete hdt;

	return 0;

}

int main(int argc, char** argv) {

	cout << "HDT file Path: " << argv[1] << endl;
	cout << "Input file Path: " << argv[2] << endl;
	cout << "Output file Path: " << argv[3] << endl;
	create_input_dataset(argv[1], argv[2], argv[3]);

	return 0;
}
