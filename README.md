# camunda-engine-orientdb
OrientDB persistence for Camunda 

This is work in progress.


Still not tested enough, but first processes work, also with history


*For the testcase, you need to initialize the database in*

```
OrientdbProcessEngineConfiguration.java

OrientGraphFactory f = initDB("camunda2", "root", "simpl4");
```
