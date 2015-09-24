package com.linkedin.camus.example.schemaregistry;

import com.linkedin.camus.example.records.PageVisits;
import com.linkedin.camus.schemaregistry.MemorySchemaRegistry;
import org.apache.avro.Schema;
import org.apache.log4j.Logger;


/**
 * This is a little dummy registry that just uses a memory-backed schema
 * registry to store two dummy Avro schemas. You can use this with
 * camus.properties
 */
public class DummySchemaRegistry extends MemorySchemaRegistry<Schema> {
  private static final Logger log = Logger.getLogger(DummySchemaRegistry.class);

  public DummySchemaRegistry() {
    super();
    log.info("######### START Registering schema ##############");
//    super.register("DUMMY_LOG", DummyLog.newBuilder().build().getSchema());
//    super.register("DUMMY_LOG_2", DummyLog2.newBuilder().build().getSchema());
    super.register("page_visits_avro", PageVisits.SCHEMA$);
    log.info("######### END Registering schema ##############");
  }
}
