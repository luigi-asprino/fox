CC=g++
CFLAGS= -g -Wall
RM=rm -rf
MKDIR_P = mkdir -p

all: directories input_dataset_creator resolve_wiki_page_redirects

directories:
	$(MKDIR_P) bin/

input_dataset_creator:
	$(CC) $(CFLAGS) -lhdt -lcds -o bin/input_dataset_creator src/input_dataset_creator.cpp

resolve_wiki_page_redirects:
	$(CC) $(CFLAGS) -lhdt -lcds -o bin/wiki_redirects_filter src/resolve_wiki_page_redirects.cpp

clean:
	$(RM) bin/