CC=g++
CFLAGS= -g -Wall
RM=rm -rf
MKDIR_P = mkdir -p

all: directories input_dataset_creator

directories:
	$(MKDIR_P) bin/

input_dataset_creator:
	$(CC) $(CFLAGS) -lhdt -lcds -o bin/input_dataset_creator src/input_dataset_creator.cpp

clean:
	$(RM) bin/