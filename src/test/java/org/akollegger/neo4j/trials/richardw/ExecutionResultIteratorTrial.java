package org.akollegger.neo4j.trials.richardw;

import org.junit.Test;
import org.neo4j.cypher.commands.Query;
import org.neo4j.cypher.javacompat.CypherParser;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * A trial illustrating the answer to a StackOverflow question from RichardW.
 *
 * @see <a href="http://stackoverflow.com/questions/8650948/neo4j-cypher-how-to-iterate-over-executionresult-result">Stack Overflow</a>
 */
public class ExecutionResultIteratorTrial {

    private Node fruit;

    @Test
    public void shouldIterateOverNodesFromCypherQuery() throws IOException {

        // 1. create an embedded graph database, establish relationship type
        String graphdir = "richardw.graphdb";
        GraphDatabaseService graphdb = new EmbeddedGraphDatabase(graphdir);
        RelationshipType IS_A = DynamicRelationshipType.withName("IS_A");

        // 2. create a graph of fruits
        Transaction tx = graphdb.beginTx();
        try {
            Node fruit = graphdb.getReferenceNode();

            Node apple = graphdb.createNode();
            apple.setProperty("kind", "apple");
            apple.createRelationshipTo(fruit, IS_A);

            Node banana = graphdb.createNode();
            banana.setProperty("kind", "banana");
            banana.createRelationshipTo(fruit, IS_A);

            Node blueberry = graphdb.createNode();
            blueberry.setProperty("kind", "blueberry");
            blueberry.createRelationshipTo(fruit, IS_A);

            tx.success();
        } finally {
            tx.finish();
        }

        // 3. run the query
        CypherParser parser = new CypherParser();
        ExecutionEngine engine = new ExecutionEngine(graphdb);
        Query query = parser.parse("START n=node(0) MATCH (n)<-[:IS_A]-(x) RETURN x");
        ExecutionResult result = engine.execute(query);

        // iterate over nodes in result and print all properties
        System.out.println("Found kinds of fruit...");
        Iterator<Node> kindsOfFruit = result.columnAs("x");
        while (kindsOfFruit.hasNext()) {
            Node kindOfFruit = kindsOfFruit.next();
            System.out.println("Kind #" + kindOfFruit.getId());
            for (String propertyKey : kindOfFruit.getPropertyKeys()) {
                System.out.println("\t" + propertyKey + " : " + kindOfFruit.getProperty(propertyKey));
            }
        }
        
        graphdb.shutdown();
        FileUtils.deleteRecursively(new File(graphdir));

    }
}
