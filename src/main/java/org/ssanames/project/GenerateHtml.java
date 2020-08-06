package org.ssanames.project;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//Class to generate html of plot created using LineChart() from NamesAnalysis.java
public class GenerateHtml {

    public void GenerateHtml(String name) throws IOException {
        //initializes image Name and directory where image is saved and html will be saved
        String imageName = name + "_Occur_Over_Time.jpeg";
        String pathName = System.getProperty("user.dir") + "\\images\\";

        //generates html with image object
        String html = "<html><head><title>Plot of popularity for name: " + name.toUpperCase() + "</title></head>"
                + "<body>"
                + "<div id='image-div'><img src="+ pathName + imageName + ">"
                + "</div>"
                + "</body></html>";

        Document document = Jsoup.parse(html);

        //writes html file to directory
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathName + name.toLowerCase() +"_popularity_plot.html"));
            writer.write(document.toString());
            writer.close();
        }catch (IOException e){
            e.getMessage();
            throw e;
        }
    }
}

