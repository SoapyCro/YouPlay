package hr.leon.croapps.youplay;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Suggestions {

    private String[] list = new String[10];
    public String[] getList(){
        return list;
    }

    public String[] find(String query){
        StringBuilder string = new StringBuilder("");
        // inicijaliziraj ih sve jer nekad crasha zbog null ptr exceptiona bez toga
        for(int i = 0; i < 10; i++)
            list[i] = "";

        // ako ima nesto upisano kreni trazit
        if(query.length() > 0){
            try {
                // skini s neta suggestione
                String temp_url = "http://suggestqueries.google.com/complete/search?q=" + query + "&client=toolbar&ds=yt&hl=en";
                URL oracle = new URL(temp_url);
                URLConnection yc = oracle.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        yc.getInputStream()));

                // zapisi rezultat u string
                String inputLine;
                while((inputLine = in.readLine()) != null){
                    string.append(inputLine);
                }
                // ako postoji bar 1 suggestion (ako ne nade nista ima cca 40 znakova) napuni listu stringova sa suggestionima
                if (string.length() > 50) {
                    // u listu punimo ono sto se u xml-u nalazi imzmedu " "
                    Pattern p = Pattern.compile("\"(.*?)\"");
                    Matcher m = p.matcher(string);
                    int i = 0;
                    while (m.find()) {
                        if (!(m.group(1).equals("1.0"))) {
                            list[i] = m.group(1);
                            // apostrof
                            list[i] = list[i].replace("&#39;", "\'");
                            i++;
                        }
                    }
                } else list[0] = "";
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SCHEDULE", "exception neki");
            }
        }
        else{
            list[0] = "";
        }
        return list;
    }
}
