package org.getaviz.run.local;

import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.database.DatabaseConnector;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Loader {
    private static SettingsConfiguration config = SettingsConfiguration.getInstance();
    private static DatabaseConnector connector = DatabaseConnector.getInstance(config.getDefaultBoldAddress());

    public static void main(String[] args) {
        boolean isSilentMode = true;
        String pathToNodesCsv = "";
        String pathToTypeOfRelationsCsv = "";

        Scanner userInput = new Scanner(System.in);
        System.out.print("Silent mode? (y/n): "); // Silent mode to run with default values
        String input = userInput.nextLine();
        if (input.equals("n")) {
            isSilentMode = false;
        }

        // Get files for nodes and typeofs
        List<Path> files = config.getInputCSVFiles();
        for(Path p : files) {
            if (p.toString().endsWith("_Test.csv")) {
                pathToNodesCsv = p.toString();
            } else if (p.toString().endsWith("_TypeOf.csv")) {
                pathToTypeOfRelationsCsv = p.toString();
            }
        }

        // Make sure the graph is empty
        connector.executeWrite("MATCH (n) DETACH DELETE n;");

        // 1. Upload nodes
        System.out.println("SAPExportCreateNodes: " + pathToNodesCsv);
        if (!isSilentMode) {
            System.out.print("Loading nodes in Neo4j. Press any key to continue...");
            userInput.nextLine();
        }
        pathToNodesCsv = pathToNodesCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToNodesCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "CREATE (n:Elements)\n" +
                        "SET n = row"
        );

        // 2. Upload relations
        if (!isSilentMode) {
            System.out.print("Creating 'CONTAINS' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        connector.executeWrite("MATCH (a:Elements), (b:Elements) WHERE a.element_id = b.container_id CREATE (a)-[r:CONTAINS]->(b)");

        // 3. Upload TypeOfRelations
        System.out.println("SAPExportCreateTypeOfRelations: " + pathToTypeOfRelationsCsv);
        if (!isSilentMode) {
            System.out.print("Creating 'TYPEOF' relationships. Press any key to continue...");
            userInput.nextLine();
        }
        pathToTypeOfRelationsCsv = pathToTypeOfRelationsCsv.replace("\\", "/");
        connector.executeWrite(
                "LOAD CSV WITH HEADERS FROM \"file:///" + pathToTypeOfRelationsCsv + "\"\n" +
                        "AS row FIELDTERMINATOR ';'\n" +
                        "MATCH (a:Elements {element_id: row.element_id}), (b:Elements {element_id: row.type_of_id})\n" +
                        "CREATE (a)-[r:TYPEOF]->(b)"
        );

        connector.close();
        System.out.println("Loader step was completed");
    }
}