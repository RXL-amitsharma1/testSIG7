package com.rxlogix.optimize

import com.orientechnologies.orient.core.metadata.schema.OClass
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.rxlogix.Constants
import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmProduct
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx
import com.tinkerpop.blueprints.impls.orient.OrientVertexType
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.Query
import org.hibernate.ScrollMode
import org.hibernate.ScrollableResults
import org.hibernate.StatelessSession

/**
 * In v. 2.2 of OrientDB and following releases, when using PLocal or Memory,please set MaxDirectMemorySize (JVM setting)
 * to a high value, like 512g -XX:MaxDirectMemorySize=512g
 */

class GraphFaersDataService {

    /**
     * Edges - Relationship between two edges
     */
    public static final String PRODUCT_HAS_MANY_TRADE_NAMES = "class:E_Product_Has_Many_Licenses"
    public static final String PRODUCT_CONSISTS_OF_INGREDIENTS = "class:E_Product_Consists_Of_Ingredients"
    public static final String PRODUCT_BELONGS_TO_A_FAMILY = "class:E_Product_Belongs_To_A_Family"

    /**
     * Vertices - these the Nodes.
     */
    public static final String PRODUCT = "Lm_Product"
    public static final String PRODUCT_FAMILY = "Lm_Product_Family"
    public static final String INGREDIENT = "Lm_Ingredient"
    public static final String LICENSE = "Lm_License"

    /**
     * Properties of Vertices
     */
    public static final String PRODUCT_ID = "product_id"
    public static final String PRODUCT_NAME = "name"
    public static final String PRODUCT_GENERIC_NAME = "generic_name"

    public static final String PRODUCT_FAMILY_ID = "family_id"
    public static final String PRODUCT_FAMILY_NAME = "name"

    public static final String INGREDIENT_ID = "ingredient_id"
    public static final String INGREDIENT_NAME = "ingredient"

    public static final String LICENSE_ID = "license_id"
    public static final String LICENSE_TRADE_NAME = "trade_name"

    def grailsApplication
    def sessionFactory_faers


    void seed() {
        checkAndCreateInitialSchema()
        //Create Indexes. Following methods just loads the indexed data for faster retrieval
        log.info("======= Started Creating Indexes for FAERS DB=======")
        searchProductList("ACI")
        searchProductList("FOLIC", INGREDIENT)
        log.info("======= End Creating Indexes for FAERS DB=======")
    }

    /**
     * This method checks if schema already exists. If not, then it will create a new Product Schema.
     */
    void checkAndCreateInitialSchema() {
        OrientGraph graph = getPVSGraph()
        graph.setAutoStartTx(false);
        try {
            //TODO: Check whether schema already exists or not?
            // Create Product Vertex Class
            OrientVertexType lmProductVertex = graph.createVertexType(PRODUCT);
            // Create Properties OF Product
            lmProductVertex.createProperty(PRODUCT_ID, OType.LONG);
            //Create Unique constraints and index the product id.
            lmProductVertex.createIndex(PRODUCT_ID + "_idx", OClass.INDEX_TYPE.UNIQUE, PRODUCT_ID);
            lmProductVertex.createProperty(PRODUCT_NAME, OType.STRING);
            lmProductVertex.createProperty(PRODUCT_GENERIC_NAME, OType.STRING);
            // Created Index on Product Name for faster retrieval.
            lmProductVertex.createIndex(PRODUCT_NAME + "_idx", "FULLTEXT", null, null, "LUCENE", PRODUCT_NAME)

            // Create Product Ingredient Vertex Class
            OrientVertexType lmIngredientVertex = graph.createVertexType(INGREDIENT);
            lmIngredientVertex.createProperty(INGREDIENT_ID, OType.LONG);
            lmIngredientVertex.createIndex(INGREDIENT_ID + "_idx", OClass.INDEX_TYPE.UNIQUE, INGREDIENT_ID);
            // Creates unique constraint
            lmIngredientVertex.createProperty(INGREDIENT_NAME, OType.STRING);
            // Created Index on Product Name for faster retrieval.
            lmIngredientVertex.createIndex(INGREDIENT_NAME + "_idx", "FULLTEXT", null, null, "LUCENE", INGREDIENT_NAME);

            if (getDevelopmentRestrictionLimit() < 0) {
                populateProductInGraphData(graph)
                populateIngredientInGraphData(graph)

            } else {
                devPopulateProductInGraphData(graph)
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            graph.shutdown();
        }

        if (getDevelopmentRestrictionLimit() < 0) {
            try {
                createEdges()
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

    }


    void populateProductInGraphData(OrientGraph graph) throws Exception {
        log.info("=========Populating Products - Started=========")
        ScrollableResults productResultSet
        try {
            StatelessSession statelessSession_faers = sessionFactory_faers.openStatelessSession()
            statelessSession_faers.beginTransaction()
            String sqlQueryProducts = "select id, name, genericName from LmProduct"
            Query query = statelessSession_faers.createQuery(sqlQueryProducts).setFetchSize(100).setReadOnly(true)
            productResultSet = query.scroll(ScrollMode.FORWARD_ONLY)
            graph.begin()
            while (productResultSet.next()) {
                Vertex vProduct = graph.addVertex("class:" + PRODUCT)
                setPropertyIfNotNull(vProduct, PRODUCT_ID, productResultSet.getLong(0))
                setPropertyIfNotNull(vProduct, PRODUCT_NAME, productResultSet.getString(1))
                setPropertyIfNotNull(vProduct, PRODUCT_GENERIC_NAME, productResultSet.get(2))
            }
            graph.commit()
            log.info("=========Products Successfully Populated=========")
        } catch (Exception pe) {
            log.info("=========Populating Products - Failed=========")
            pe.printStackTrace()
        } finally {
            if (productResultSet)
                productResultSet.close()
        }
    }

    void populateIngredientInGraphData(OrientGraph graph) {
        log.info("=========Populating Ingredients - Started=========")
        ScrollableResults ingredientResultSet
        try {
            StatelessSession statelessSession_faers = sessionFactory_faers.openStatelessSession()
            String sqlQueryIngredients = "Select id, ingredient from LmIngredient"
            Query query = statelessSession_faers.createQuery(sqlQueryIngredients).setFetchSize(100).setReadOnly(true)
            ingredientResultSet = query.scroll(ScrollMode.FORWARD_ONLY)
            graph.begin()
            int batchCount = 1
            Boolean isCommitedFlag = false
            while (ingredientResultSet.next()) {
                batchCount++
                isCommitedFlag = false
                Vertex vIngredient = graph.addVertex("class:" + INGREDIENT)
                setPropertyIfNotNull(vIngredient, INGREDIENT_ID, ingredientResultSet.getLong(0))
                setPropertyIfNotNull(vIngredient, INGREDIENT_NAME, ingredientResultSet.getString(1))
                if (batchCount >= 100) {
                    graph.commit()
                    isCommitedFlag = true;
                    graph.begin()
                }
            }
            if (!isCommitedFlag)
                graph.commit()

            log.info("=========Ingredients Populated Successfully=========")
        } catch (Exception ie) {
            log.info("=========Populating Ingredients - Failed=========")
            ie.printStackTrace()
        } finally {
            if (ingredientResultSet)
                ingredientResultSet.close()
        }
    }


    void createEdges() {
        String dbLocation = Holders.config.signal.faers.graphDB.location
        OrientGraphNoTx graph = new OrientGraphNoTx(dbLocation)
        log.info("=========Populating Products Ingredient Edges - Started=========")
        ScrollableResults piMappingResultSet
        try {
            StatelessSession statelessSession_faers = sessionFactory_faers.openStatelessSession()
            statelessSession_faers.beginTransaction()
            String sqlQueryProductsIngredientsMapping = "Select productId, ingredientId from LmProductIngredientMapping "
            Query query = statelessSession_faers.createQuery(sqlQueryProductsIngredientsMapping).setFetchSize(100).setReadOnly(true)
            piMappingResultSet = query.scroll(ScrollMode.FORWARD_ONLY)
            int batchCount = 1
            while (piMappingResultSet.next()) {
                Vertex vProduct = fetchGraphVertexByLmId(piMappingResultSet.getLong(0).toString(), graph, PRODUCT)
                Vertex vIngredient = fetchGraphVertexByLmId(piMappingResultSet.getLong(1).toString(), graph, INGREDIENT)
                graph.addEdge(PRODUCT_CONSISTS_OF_INGREDIENTS, vProduct, vIngredient, "consistsOf");
                batchCount++
                if (batchCount >= 2000) {
                    graph.shutdown()
                    sleep(100)
                    System.gc()
                    graph = new OrientGraphNoTx(dbLocation)
                    batchCount = 1
                }
            }
            log.info("=========Products Ingredient Edges Successfully Populated=========")
        } catch (Exception pe) {
            log.info("=========Populating Products Ingredient Edges Products - Failed=========")
            pe.printStackTrace()
        } finally {
            if (piMappingResultSet)
                piMappingResultSet.close()
            if (graph.isClosed()) {
                graph.shutdown()
            }
        }
    }


    Vertex fetchGraphVertexByLmId(String id, OrientBaseGraph graph, String type = PRODUCT) {
        Vertex resultVertex
        if (id) {
            String searchField = (type == PRODUCT) ? PRODUCT_ID : INGREDIENT_ID
            String stm = "SELECT FROM " + type + " WHERE " + searchField + " = " + id + "";
            for (Vertex vertex : (Iterable<Vertex>) graph.command(new OCommandSQL(stm)).execute()) {
                resultVertex = vertex
                break
            }
        }
        return resultVertex
    }

    void setPropertyIfNotNull(Vertex vertex, String propertyName, Object propertyValue) {
        if (propertyValue != null)
            vertex.setProperty(propertyName, propertyValue)
    }


    List searchProductList(String searchTerm, String type = PRODUCT) {
        List products = []
        if (searchTerm) {
            OrientGraph graph = getPVSGraph()
            String searchField = (type == PRODUCT) ? PRODUCT_NAME : INGREDIENT_NAME
            try {
                String stm = "SELECT FROM " + type + " WHERE " + searchField + " LUCENE '" + searchTerm + "*' LIMIT 200";
                for (Vertex vProduct : (Iterable<Vertex>) graph.command(new OCommandSQL(stm)).execute()) {
                    Map product = new HashMap()
                    product.id = (type == PRODUCT) ? vProduct.getProperty(PRODUCT_ID) : vProduct.getProperty(INGREDIENT_ID)
                    product.name = (type == PRODUCT) ? vProduct.getProperty(PRODUCT_NAME) : vProduct.getProperty(INGREDIENT_NAME)
                    product.genericName = (type == PRODUCT) ? vProduct.getProperty(PRODUCT_GENERIC_NAME) : vProduct.getProperty(INGREDIENT_NAME)
                    products.add(product)
                }
            } finally {
                graph.shutdown()
            }
        }
        return products

    }

    def fetchVertex(String id, String type = PRODUCT) {
        List products = []
        if (id) {
            OrientGraph graph = getPVSGraph()
            String searchField = (type == PRODUCT) ? PRODUCT_ID : INGREDIENT_ID
            try {
                String stm = "SELECT FROM " + type + " WHERE " + searchField + " = " + id + "";
                for (Vertex vProduct : (Iterable<Vertex>) graph.command(new OCommandSQL(stm)).execute()) {
                    Map product = new HashMap()
                    product.id = (type == PRODUCT) ? vProduct.getProperty(PRODUCT_ID) : vProduct.getProperty(INGREDIENT_ID)
                    product.name = (type == PRODUCT) ? vProduct.getProperty(PRODUCT_NAME) : vProduct.getProperty(INGREDIENT_NAME)
                    product.genericName = (type == PRODUCT) ? vProduct.getProperty(PRODUCT_GENERIC_NAME) : vProduct.getProperty(INGREDIENT_NAME)
                    products.add(product)
                    break
                }
            } finally {
                graph.shutdown()
            }
        }
        return products.first() ?: null
    }

    def fetchRelatedProductsByIngredients(def ingredientId) {
        List products = []
        if (ingredientId) {
            OrientGraph graph = getPVSGraph()
            try {
                String stm = "select expand(out) from (select expand(in_consistsOf) from Lm_Ingredient where ingredient_id = " + ingredientId + ") LIMIT 200";
                for (Vertex vProduct : (Iterable<Vertex>) graph.command(new OCommandSQL(stm)).execute()) {
                    Map product = new HashMap()
                    product.id = vProduct.getProperty(PRODUCT_ID)
                    product.name = vProduct.getProperty(PRODUCT_NAME)
                    product.genericName = vProduct.getProperty(PRODUCT_GENERIC_NAME)
                    products.add(product)
                }
            } finally {
                graph.shutdown()
            }
        }
        return products
    }

    def fetchRelatedIngredientsByProduct(def productId) {
        List ingredients = []
        if (productId) {
            OrientGraph graph = getPVSGraph()
            try {
                String stm = "select expand(in) from  (select expand(out_consistsOf) from lm_product where product_id  = " + productId + ") LIMIT 200";
                for (Vertex vProduct : (Iterable<Vertex>) graph.command(new OCommandSQL(stm)).execute()) {
                    Map ingredient = new HashMap()
                    ingredient.id = vProduct.getProperty(INGREDIENT_ID)
                    ingredient.ingredient = vProduct.getProperty(INGREDIENT_NAME)
                    ingredient.genericName = vProduct.getProperty(INGREDIENT_NAME)
                    ingredients.add(ingredient)
                }
            } finally {
                graph.shutdown()
            }
        }
        return ingredients
    }

    OrientGraph getPVSGraph() {
        String dbLocation = Holders.config.signal.faers.graphDB.location
        OrientGraph graph = new OrientGraph(dbLocation)
        return graph
    }

    public Integer getDevelopmentRestrictionLimit() {
        return (Integer) Holders.config.signal.faers.graphDB.seed.restriction.dataSet
    }

/**
 * This method is only for seeding limited records on developers machine.
 * WARNING: Value of configuration 'signal.faers.graphDB.seed.restriction.dataSet' should not exceed 100 at any given time
 * @param graph
 * @throws Exception
 */
    void devPopulateProductInGraphData(OrientGraph graph) throws Exception {
        int maxResultLimit = getDevelopmentRestrictionLimit()
        log.info("=============Seeding started of Faers DB with Database record limit of :" + maxResultLimit + " ===========")
        LmProduct."${Constants.DataSource.FAERS}".list([max: maxResultLimit]).each { LmProduct lmProduct ->
            graph.begin()
            Vertex vProduct = graph.addVertex("class:" + PRODUCT)
            setPropertyIfNotNull(vProduct, PRODUCT_ID, lmProduct.id)
            setPropertyIfNotNull(vProduct, PRODUCT_NAME, lmProduct?.name)
            setPropertyIfNotNull(vProduct, PRODUCT_GENERIC_NAME, lmProduct.genericName)

            if (lmProduct?.ingredients && lmProduct?.ingredients?.size()) {
                lmProduct.ingredients.each { LmIngredient lmIngredient ->
                    Vertex vIngredient = null;
                    Boolean ingredientExistFlag = false;
                    for (Vertex vIngredientExist : graph.getVertices(INGREDIENT + "." + INGREDIENT_ID, lmIngredient?.id)) {
                        vIngredient = vIngredientExist;
                        break;// Ingredient is unique
                    }
                    if (!ingredientExistFlag) {
                        vIngredient = graph.addVertex("class:" + INGREDIENT)
                        setPropertyIfNotNull(vIngredient, INGREDIENT_ID, lmIngredient.id)
                        setPropertyIfNotNull(vIngredient, INGREDIENT_NAME, lmIngredient?.ingredient)
                    }
                    graph.addEdge("class:$PRODUCT_CONSISTS_OF_INGREDIENTS", vProduct, vIngredient, "consistsOf");
                }
            }
            graph.commit()
        }
        log.info("=============Seeding Ended of Faers DB with Database record limit of :" + maxResultLimit + " ===========")
    }

}
