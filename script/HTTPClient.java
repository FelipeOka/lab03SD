import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPClient {

    public static void main(String[] args) {
        String url = "http://ufsc.br";
        String html = getHTML(url);
        if (html != null) {
            List<String> tags = extractTags(html);
            for (String tag : tags) {
                new Thread(() -> fetchContent(tag)).start();
            }
        }
    }

    private static String getHTML(String url) {
        try {
            // Extrai host e caminho da URL
            String[] parts = url.split("/");
            String host = parts[2];
            String path = "/" + String.join("/", Arrays.copyOfRange(parts, 3, parts.length));

            // Cria socket e se conecta ao servidor
            Socket socket = new Socket(host, 80);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(("GET " + path + " HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
                    "Connection: close\r\n\r\n").getBytes());

            // Lê a resposta do servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            // Fecha o socket
            socket.close();

            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<String> extractTags(String html) {
        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("<(link|img|script)[^>]*>");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            tags.add(matcher.group());
        }
        return tags;
    }

    private static void fetchContent(String tag) {
        try {
            // Extrai URL da tag
            Matcher matcher = Pattern.compile("src=\"(http://.*?|https://.*?)\"").matcher(tag);
            if (matcher.find()) {
                String url = matcher.group(1);

                // Ignora URLs com HTTPS
                if (url.startsWith("https://")) {
                    return;
                }

                // Cria socket e se conecta ao servidor
                String host = url.split("/")[2];
                String path = "/" + url.split("/", 4)[3];
                Socket socket = new Socket(host, 80);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(("GET " + path + " HTTP/1.1\r\n" +
                        "Host: " + host + "\r\n" +
                        "Connection: close\r\n\r\n").getBytes());

                // Lê a resposta do servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                // Fecha o socket
                socket.close();

                // Grava o conteúdo em um arquivo
                String filename = url.substring(url.lastIndexOf('/') + 1);
                System.out.println("Arquivo " + filename + " gravado com sucesso.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
