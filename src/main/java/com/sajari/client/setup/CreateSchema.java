package com.sajari.client.setup;

import com.sajari.client.ApiClient;
import com.sajari.client.ApiException;
import com.sajari.client.api.CollectionsApi;
import com.sajari.client.api.PipelinesApi;
import com.sajari.client.api.SchemaApi;
import com.sajari.client.config.AppConfiguration;
import com.sajari.client.model.BatchCreateSchemaFieldsRequest;
import com.sajari.client.model.BatchCreateSchemaFieldsResponse;
import com.sajari.client.model.Collection;
import com.sajari.client.model.Pipeline;
import com.sajari.client.model.PipelineStep;
import com.sajari.client.model.PipelineStepParamBinding;
import com.sajari.client.model.SchemaField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static com.sajari.client.model.PipelineType.QUERY;
import static com.sajari.client.model.PipelineType.RECORD;
import static com.sajari.client.model.SchemaFieldMode.NULLABLE;
import static com.sajari.client.model.SchemaFieldMode.REQUIRED;
import static com.sajari.client.model.SchemaFieldMode.UNIQUE;
import static com.sajari.client.model.SchemaFieldType.INTEGER;
import static com.sajari.client.model.SchemaFieldType.STRING;
import static com.sajari.client.model.SchemaFieldType.TIMESTAMP;
import static java.util.Map.of;

@Slf4j
public class CreateSchema {

    public static final String APP_QUERY_PIPELINE_NAME = "app-query";
    public static final String APP_QUERY_PIPELINE_VERSION = "1";
    public static final String APP_RECORD_PIPELINE_NAME = "app-record";
    public static final String APP_RECORD_PIPELINE_VERSION = "1";

    @Autowired
    private ApiClient apiClient;
    @Autowired
    private AppConfiguration appConfiguration;

    public CreateSchema(ApiClient apiClient, AppConfiguration appConfiguration) {
        this.apiClient = apiClient;
        this.appConfiguration = appConfiguration;
    }

    public void create() throws ApiException {

        // Check if the configured Collection exists
        // If not create the collection
        CollectionsApi collectionsApi = new CollectionsApi(apiClient);

        try {
            collectionsApi.getCollection(appConfiguration.getSajariCollectionId());
        } catch (ApiException e) {
            if (e.getCode() == 404) {

                Collection collection = new Collection().
                        displayName("Google Feed Collection: " + appConfiguration.getSajariCollectionId());

                collectionsApi.createCollection(appConfiguration.getSajariCollectionId(), collection);

                // Create schema
                SchemaApi schemaApi = new SchemaApi(apiClient);
                BatchCreateSchemaFieldsRequest batchCreateSchemaFieldsRequest = new BatchCreateSchemaFieldsRequest()
                        .addFieldsItem(
                                new SchemaField().name("id").type(INTEGER).mode(UNIQUE)
                        ).addFieldsItem(
                                new SchemaField().name("title").type(STRING).mode(REQUIRED)
                        ).addFieldsItem(
                                new SchemaField().name("description").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("link").type(STRING).mode(REQUIRED)
                        ).addFieldsItem(
                                new SchemaField().name("image_link").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("condition").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("availability").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("price").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("gtin").type(INTEGER).mode(REQUIRED)
                        ).addFieldsItem(
                                new SchemaField().name("brand").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("mpn").type(INTEGER).mode(REQUIRED)
                        ).addFieldsItem(
                                new SchemaField().name("google_product_category").type(STRING).mode(REQUIRED)
                        ).addFieldsItem(
                                new SchemaField().name("product_type").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("gender").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("age_group").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("color").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("size").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("item_group_id").type(STRING).mode(REQUIRED)
                        ).addFieldsItem(
                                new SchemaField().name("sale_price").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("sale_price_effective_date").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("additional_image_link").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("shipping_weight").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("coo").type(STRING).mode(NULLABLE)
                        ).addFieldsItem(
                                new SchemaField().name("record_creation_date").type(TIMESTAMP).mode(REQUIRED)
                        ).addFieldsItem(
                                new SchemaField().name("shipping_weight").type(STRING).mode(NULLABLE)
                        );

                try {
                    BatchCreateSchemaFieldsResponse batchCreateSchemaFieldsResponse = schemaApi.batchCreateSchemaFields(appConfiguration.getSajariCollectionId(), batchCreateSchemaFieldsRequest);
                    log.info("Schema created: {}", batchCreateSchemaFieldsResponse.getFields());
                } catch (ApiException ex) {
                    log.error("Failed to create schema. Response code: {}, Response message: {}", ex.getCode(), ex.getResponseBody());
                }

                // Create pipelines
                PipelinesApi pipelinesApi = new PipelinesApi(apiClient);

                Pipeline queryPipeline = new Pipeline().type(QUERY).name(APP_QUERY_PIPELINE_NAME).version(APP_QUERY_PIPELINE_VERSION).description("Query Pipeline").
                        addPreStepsItem(
                                new PipelineStep().id("set-filter").title("Apply a filter to the search").description("Bind the filter expression from the parameter `filter`.")
                        ).addPreStepsItem(
                                new PipelineStep().id("set-fields").title("Limit the fields returned in the results").description("Fields are specified in the parameter `fields`. If not specified then all fields are returned.")
                        ).addPreStepsItem(
                                new PipelineStep().id("pagination").title("Divide results into pages").description("Specify the number of results returned in each page in the parameter `resultsPerPage`.")
                        ).addPreStepsItem(
                                new PipelineStep().id("count-aggregate-filter").title("Count the number of distinct values in the result set for the specified fields").description("Used to display counts for facets and filters.").
                                        params(
                                                of(
                                                        "fields", new PipelineStepParamBinding().bind("count"),
                                                        "filters", new PipelineStepParamBinding().bind("countFilters")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("min-aggregate-filter").title("Determine the minimum value in the result set").description("Used to display ranges for facets.").
                                        params(
                                                of(
                                                        "fields", new PipelineStepParamBinding().bind("min")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("max-aggregate-filter").title("Determine the maximum value in the result set").description("Used to display ranges for facets.").
                                        params(
                                                of(
                                                        "fields", new PipelineStepParamBinding().bind("max")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("sort").title("Sort the results").description("Sort the result set by the fields specified in the parameter `sort`.").
                                        params(
                                                of(
                                                        "fields", new PipelineStepParamBinding().bind("sort")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-spelling").title("Spelling correction").description("Perform spelling correction on parameter `q`, including the spelling suggestions in the search.").
                                        params(
                                                of(
                                                        "model", new PipelineStepParamBinding().constant("default"),
                                                        "phraseLabelWeights", new PipelineStepParamBinding().constant("query:1.0,description:1.0,google_product_category:1.0,title:1.0"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("synonym").title("Add synonyms").description("Include synonyms for parameter `q` in the search.").
                                        params(
                                                of(
                                                        "model", new PipelineStepParamBinding().constant(appConfiguration.getSajariCollectionId()),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("set-score-mode").title("Configure mode for calculating the score for a result").
                                        params(
                                                of(
                                                        "mode", new PipelineStepParamBinding().constant("MAX")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-score-instance-boost").title("Reinforcement learning").description("Boost results that have positive user interactions.").
                                        params(
                                                of(
                                                        "minCount", new PipelineStepParamBinding().constant("5"),
                                                        "threshold", new PipelineStepParamBinding().constant("0.5")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("feature-boost-value").title("Allocate a portion of the overall score to feature boosts").description("Limit the contribution of feature boosts to the total score.").
                                        params(
                                                of(
                                                        "value", new PipelineStepParamBinding().constant("0.2")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("filter-boost").title("Boost if `description` contains parameter `q`").
                                        params(
                                                of(
                                                        "filter", new PipelineStepParamBinding().constant("description ~ q"),
                                                        "score", new PipelineStepParamBinding().constant("0.05")
                                                )
                                        ).condition("q != ''")
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `description`").description("This is the most important field and has the highest weight.").
                                        params(
                                                of(
                                                        "field", new PipelineStepParamBinding().constant("description"),
                                                        "score", new PipelineStepParamBinding().constant("1.0000"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `brand`").params(
                                        of(
                                                "field", new PipelineStepParamBinding().constant("brand"),
                                                "score", new PipelineStepParamBinding().constant("0.5000"),
                                                "text", new PipelineStepParamBinding().bind("q")
                                        )
                                )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `color`").params(
                                        of(
                                                "field", new PipelineStepParamBinding().constant("color"),
                                                "score", new PipelineStepParamBinding().constant("0.2500"),
                                                "text", new PipelineStepParamBinding().bind("q")
                                        )
                                )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `availability`").
                                        params(
                                                of(
                                                        "field", new PipelineStepParamBinding().constant("availability"),
                                                        "score", new PipelineStepParamBinding().constant("0.1250"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `gender`").params(
                                        of(
                                                "field", new PipelineStepParamBinding().constant("gender"),
                                                "score", new PipelineStepParamBinding().constant("0.0625"),
                                                "text", new PipelineStepParamBinding().bind("q")
                                        )
                                )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `google_product_category`").
                                        params(
                                                of(
                                                        "field", new PipelineStepParamBinding().constant("google_product_category"),
                                                        "score", new PipelineStepParamBinding().constant("0.0312"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `price`").params(
                                        of(
                                                "field", new PipelineStepParamBinding().constant("price"),
                                                "score", new PipelineStepParamBinding().constant("0.0156"),
                                                "text", new PipelineStepParamBinding().bind("q")
                                        )
                                )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `product_type`").
                                        params(
                                                of(
                                                        "field", new PipelineStepParamBinding().constant("product_type"),
                                                        "score", new PipelineStepParamBinding().constant("0.0078"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `sale_price`").params(
                                        of(
                                                "field", new PipelineStepParamBinding().constant("sale_price"),
                                                "score", new PipelineStepParamBinding().constant("0.0039"),
                                                "text", new PipelineStepParamBinding().bind("q")
                                        )
                                )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `size`").params(
                                        of(
                                                "field", new PipelineStepParamBinding().constant("size"),
                                                "score", new PipelineStepParamBinding().constant("0.0020"),
                                                "text", new PipelineStepParamBinding().bind("q")
                                        )
                                )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `title`").
                                        params(
                                                of(
                                                        "field", new PipelineStepParamBinding().constant("title"),
                                                        "score", new PipelineStepParamBinding().constant("0.0010"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().id("index-text-index-boost").title("Search field `condition`").params(
                                        of(
                                                "field", new PipelineStepParamBinding().constant("condition"),
                                                "score", new PipelineStepParamBinding().constant("0.0005"),
                                                "text", new PipelineStepParamBinding().bind("q")
                                        )
                                )
                        ).addPostStepsItem(
                                new PipelineStep().
                                        id("promotions").
                                        title("Add promotions to results").
                                        params(
                                                of(
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPostStepsItem(
                                new PipelineStep().
                                        id("train-autocomplete").
                                        title("Live autocomplete training").
                                        description("Allows query completion to adapt to trending and popular queries.").
                                        params(
                                                of(
                                                        "label", new PipelineStepParamBinding().constant("query"),
                                                        "model", new PipelineStepParamBinding().constant("default"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        );
                try {
                    pipelinesApi.createPipeline(appConfiguration.getSajariCollectionId(), queryPipeline);
                } catch (ApiException ex) {
                    log.error("Failed to create pipeline {}. Response code: {}, Response message: {}", queryPipeline.getName(), ex.getCode(), ex.getResponseBody());
                }

                // Create Autocomplete pipeline
                Pipeline autocompletePipeline = new Pipeline().
                        type(QUERY).
                        name("autocomplete").
                        version("1").
                        description("Autocomplete Pipeline").
                        addPreStepsItem(
                                new PipelineStep().
                                        id("autocomplete").
                                        title("Perform query completion").
                                        description("Use to predict the end of a query as a user is typing.").
                                        params(
                                                of(
                                                        "labelWeights", new PipelineStepParamBinding().constant("query:1.0,description:1.0,google_product_category:1.0,title:1.0"),
                                                        "model", new PipelineStepParamBinding().constant("default"),
                                                        "original", new PipelineStepParamBinding().bind("q.original"),
                                                        "outText", new PipelineStepParamBinding().bind("q"),
                                                        "override", new PipelineStepParamBinding().bind("q.override"),
                                                        "overrideSuggestions", new PipelineStepParamBinding().bind("q.overrideSuggestions"),
                                                        "suggestions", new PipelineStepParamBinding().bind("q.suggestions"),
                                                        "text", new PipelineStepParamBinding().bind("q")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().
                                        id("skip-search").
                                        title("Do not perform a search")
                        );

                try {
                    pipelinesApi.createPipeline(appConfiguration.getSajariCollectionId(), autocompletePipeline);
                } catch (ApiException ex) {
                    log.error("Failed to create pipeline {}. Response code: {}, Response message: {}", autocompletePipeline.getName(), ex.getCode(), ex.getResponseBody());
                }

                Pipeline recordPipeline = new Pipeline().
                        type(RECORD).
                        name(APP_RECORD_PIPELINE_NAME).
                        version(APP_RECORD_PIPELINE_VERSION).
                        description("Record Pipeline").
                        addPreStepsItem(
                                new PipelineStep().
                                        id("convert-field").
                                        title("Convert timestamp field").
                                        description("Converts timestamp value into RFC3339 format").
                                        params(
                                                of(
                                                        "field", new PipelineStepParamBinding().constant("record_creation_date"),
                                                        "repeated", new PipelineStepParamBinding().constant("false"),
                                                        "typeName", new PipelineStepParamBinding().constant("TIMESTAMP")
                                                )
                                        )
                        ).addPreStepsItem(
                                new PipelineStep().
                                        id("create-indexes").
                                        title("Create a search index for fields in the record").
                                        description("Allows the specified fields in the record to be searchable.").
                                        params(
                                                of(
                                                        "fields", new PipelineStepParamBinding().constant("description,brand,color,availability,gender,google_product_category,price,product_type,sale_price,size,title,condition")
                                                )
                                        )
                        ).addPostStepsItem(
                                new PipelineStep().
                                        id("train-spelling").
                                        title("Train spelling using words from fields in the record").
                                        description("Used to provide spelling suggestions in a query.").
                                        params(
                                                of(
                                                        "fields", new PipelineStepParamBinding().constant("brand:brand,color:color,availability:availability,gender:gender,price:price,product_type:product_type,sale_price:sale_price,size:size,condition:condition"),
                                                        "model", new PipelineStepParamBinding().constant("default")
                                                )
                                        )
                        ).addPostStepsItem(
                                new PipelineStep().
                                        id("train-autocomplete-v2").
                                        title("Train autocomplete for query completion using fields in the record").
                                        description("Used to provide query completion in a query.").
                                        params(
                                                of(
                                                        "fields", new PipelineStepParamBinding().constant("description:description,google_product_category:google_product_category,title:title"),
                                                        "model", new PipelineStepParamBinding().constant("default")
                                                )
                                        )
                        );

                try {
                    pipelinesApi.createPipeline(appConfiguration.getSajariCollectionId(), recordPipeline);
                } catch (ApiException ex) {
                    log.error("Failed to create pipeline {}. Response code: {}, Response message: {}", recordPipeline.getName(), ex.getCode(), ex.getResponseBody());
                }

            }
        }
    }
}