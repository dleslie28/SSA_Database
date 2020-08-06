package org.ssanames.project;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            ParseBabyNames pb = new ParseBabyNames("SsaNames.db");
            NamesAnalysis na = new NamesAnalysis("SsaNames.db");
            GenerateHtml html = new GenerateHtml();
            //pb.createTable(2010);
            //pb.loadTable(2010);
            //na.getRankChange("Madison", 'F', 2017, 2018);
            na.LineChart("Devonna", 'F');
            html.GenerateHtml("Devonna");
        } catch (IOException ie) {
            System.err.println("I/O error: " + ie.getMessage());
        }
    }
}
