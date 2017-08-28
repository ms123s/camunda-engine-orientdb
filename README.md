# camunda-engine-orientdb
OrientDB persistence for Camunda 

This is work in progress.


Still not tested enough, but first processes work, also with history


_*For the testcase, you need to initialize the database in*_

**OrientdbProcessEngineConfiguration.java**
```java
..
public OrientdbProcessEngineConfiguration() {
    super();
    createDatabaseFactory("camunda2", "root", "simpl4");

..

```
