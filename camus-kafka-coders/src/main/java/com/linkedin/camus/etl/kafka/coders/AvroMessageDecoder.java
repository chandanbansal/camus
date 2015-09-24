package com.linkedin.camus.etl.kafka.coders;

import com.linkedin.camus.coders.CamusWrapper;
import com.linkedin.camus.coders.Message;
import com.linkedin.camus.coders.MessageDecoder;
import com.linkedin.camus.coders.MessageDecoderException;
import com.linkedin.camus.schemaregistry.CachedSchemaRegistry;
import com.linkedin.camus.schemaregistry.SchemaRegistry;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class AvroMessageDecoder extends MessageDecoder<Message, Record> {
  private static final Logger log = Logger.getLogger(KafkaAvroMessageDecoder.class);

  protected DecoderFactory decoderFactory;
  protected SchemaRegistry<Schema> registry;
  private Schema latestSchema;

  @Override
  public void init(Properties props, String topicName) {
    super.init(props, topicName);
    try {

      String className = props.getProperty(KafkaAvroMessageEncoder.KAFKA_MESSAGE_CODER_SCHEMA_REGISTRY_CLASS);
      Class c = Class.forName(className);
      SchemaRegistry<Schema> registry = (SchemaRegistry<Schema>) c.newInstance();

      log.info("Prop " + KafkaAvroMessageEncoder.KAFKA_MESSAGE_CODER_SCHEMA_REGISTRY_CLASS + " is: "
              + props.getProperty(KafkaAvroMessageEncoder.KAFKA_MESSAGE_CODER_SCHEMA_REGISTRY_CLASS));
      log.info("Underlying schema registry for topic: " + topicName + " is: " + registry);
      registry.init(props);

      this.registry = new CachedSchemaRegistry<Schema>(registry, props);
      this.latestSchema = registry.getLatestSchemaByTopic(topicName).getSchema();
    } catch (Exception e) {
      throw new MessageDecoderException(e);
    }

    decoderFactory = DecoderFactory.get();
  }

  public CamusWrapper<Record> decode(Message message) {
    try {
      DatumReader<Record> reader = new GenericDatumReader<Record>(this.latestSchema);
      return new CamusAvroWrapper(reader.read(null, decoderFactory.binaryDecoder(message.getPayload(), null)));

    } catch (IOException e) {
      throw new MessageDecoderException(e);
    }
  }

  public static class CamusAvroWrapper extends CamusWrapper<Record> {

    public CamusAvroWrapper(Record record) {
      super(record);
      Record header = (Record) super.getRecord().get("header");
      if (header != null) {
        if (header.get("server") != null) {
          put(new Text("server"), new Text(header.get("server").toString()));
        }
        if (header.get("service") != null) {
          put(new Text("service"), new Text(header.get("service").toString()));
        }
      }
    }

    @Override
    public long getTimestamp() {
      Record header = (Record) super.getRecord().get("header");

      if (header != null && header.get("time") != null) {
        return (Long) header.get("time");
      } else if (super.getRecord().get("timestamp") != null) {
        return (Long) super.getRecord().get("timestamp");
      } else {
        return System.currentTimeMillis();
      }
    }
  }
}
