@cd %~dp0
@start "" "html/index.html"
@"java.exe" -Dfile.encoding=UTF-8 -classpath "bin;ext-jars\lucene-analyzers-common-8.1.0.jar;ext-jars\lucene-core-8.1.0.jar;ext-jars\lucene-queryparser-8.1.0.jar" gniza.GennizaServer
