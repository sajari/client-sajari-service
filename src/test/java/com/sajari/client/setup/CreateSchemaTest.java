package com.sajari.client.setup;

import com.sajari.client.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class CreateSchemaTest {

    @Autowired
    private CreateSchema createSchema;

    @Test
    void testSchema() throws ApiException {

        createSchema.create();

    }
}