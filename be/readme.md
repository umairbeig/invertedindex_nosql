## connection to mongodb
App assumes u have mongodb configured with uri mongodb://localhost:27017\
The database used is test\
The collections being table1 , table2 , table 3.

## in case you still have to feed the data to db:

run python3 mongo.py <textfile to be stored into db> -c <collection name of form table x>\
make sure you have the requirements installed that are mentioned in requirements file.\

## run the Spring boot application  \
I used intellij in my case..just run the main method.\
<As the git link was to be provided , i refrained from making jar as anyone can simply clone the repo from git>\

The backend server should run on localhost:8080
