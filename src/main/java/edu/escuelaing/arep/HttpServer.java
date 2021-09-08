package edu.escuelaing.arep;

import java.net.*;
import java.nio.charset.Charset;
import java.io.*;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpServer {
    private static final HttpServer _instance = new HttpServer();
    private static final String HTTP_MESSAGE = "HTTP/1.1 200 OK\n" + "Content-Type: #/#\r\n" + "\r\n";
    private static final String WHEATER_QUERY = "https://api.openweathermap.org/data/2.5/weather?q=#&appid=6ff8f8b1dd43268717ea79493222b474";

    public static HttpServer getInstance(){
        return _instance;
    }

    private HttpServer(){}

    public void start(String[] args) throws IOException{
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(4567);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            try {
                serverConnection(clientSocket);
            } catch (URISyntaxException e) {
                System.err.println("URI incorrect.");
                System.exit(1);
            }
        }
        serverSocket.close();
    }

    public void serverConnection(Socket clientSocket) throws IOException, URISyntaxException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        clientSocket.getInputStream()));
        String inputLine, outputLine;
        ArrayList<String> request = new ArrayList<String>();

        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            request.add(inputLine);
            if (!in.ready()) {
                break;
            }
        }
        String uriStr = request.get(0).split(" ")[1];
        URI resourceURI = new URI(uriStr);
        outputLine = getResource(resourceURI);
        
        out.println(outputLine);
        out.close();
        in.close();
        clientSocket.close();
    }

    public String getResource(URI resourceURI) throws URISyntaxException{
        System.out.println(resourceURI.toString());
        String cityname = "";
        return computeHTMLResponse(cityname);
    }

    public String computeHTMLResponse(String cityname){
        return getDefaultPage();
    }

    public static JSONObject getWheaterJSON(String cityname) throws IOException{
        InputStream is = new URL(WHEATER_QUERY.replaceFirst("#", cityname)).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String jsonText = ""; int cp;
        while ((cp = rd.read()) != -1) jsonText += (char) cp;
        JSONObject json = new JSONObject(jsonText);
        return json;
    }

    private String getDefaultPage(){
        String page = HTTP_MESSAGE.replaceFirst("#", "text").replaceFirst("#", "html");
        page += "<!DOCTYPE html>\n"
        +"<html lang=\"en\">\n"
        +"<head>\n"
        +    "<meta charset=\"UTF-8\">\n"
        +    "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
        +    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
        +    "<title>Wnow</title>\n"
        +"</head>\n"
        +"<style>\n"
        +    "*, *::after, *::before { margin: 0px; }\n"
        +    "body { overflow-x: hidden; }\n"
        +    ".container { display: flex; justify-content: center; align-items: center; width: 100vw; height: 100vh; }\n"
        +    ".user_input { display: flex; justify-content:space-between; align-items: center; flex-direction: column; }\n"
        +    ".user_input input{ margin-bottom: 2vh; }\n"
        +"</style>\n"
        +"<body>\n"
        +    "<div class=\"container\">\n"
        +        "<div class=\"user_input\">\n"
        +            "<input id=\"user_input\" type=\"text\" placeholder=\"City\">\n"
        +            "<input type=\"button\" id=\"user_button\" value=\"Let's check!\">\n"
        +        "</div>\n"
        +    "</div>\n"
        +    "<script>\n"
        +        "document.addEventListener(\"DOMContentLoaded\", () => {\n"
        +            "document.getElementById(\"user_button\").addEventListener(\"click\", () => {\n"
        +             "console.log('hey');\n"
        +                "user_city = document.getElementById(\"user_input\");\n"
        +                "window.location.replace(window.location.href.replace(\"page.html\",\"\") + \"consulta?lugar=\" + user_city.value);\n"
        +            "})\n"
        +        "});\n"
        +    "</script>\n"
        +"</body>\n"
        +"</html>\n";
        return page;
    }
    
    public static void main(String[] args) throws IOException {
        //System.out.println(HttpServer.getWheaterJSON("london"));
        HttpServer.getInstance().start(args);
    }
}
