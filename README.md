# camunda-engine-orientdb
OrientDB persistence for Camunda 

This is work in progress.

Still not tested enough, but all included tests, except testParallelExecution () are now ok



_*For the testcase, you need to initialize the database in*_

**OrientdbProcessEngineConfiguration.java**
```java
..
public OrientdbProcessEngineConfiguration() {
    super();
    createDatabaseFactory("camunda2", "root", "simpl4");

..

```
