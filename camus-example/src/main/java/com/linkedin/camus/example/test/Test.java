package com.linkedin.camus.example.test;

import com.linkedin.camus.etl.kafka.CamusJob;

/**
 * Created by chandan.bansal on 23/09/15.
 */
public class Test {
    public static void main(String[] args) throws Exception  {
        String a[] = {"-P", "camus-example/src/main/resources/camus-temp.properties"};
        CamusJob.main(a);
    }

}
