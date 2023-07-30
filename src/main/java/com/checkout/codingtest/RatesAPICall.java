package com.checkout.codingtest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;

public class RatesAPICall {
    public static void main(String[] args) throws Exception {
        var httpClient = HttpClient.newBuilder().build();

        HashMap<String, String> params = new HashMap<>();
        params.put("product", "card_payouts");
        params.put("source", "visa");
        params.put("currency_pairs", "GBPEUR,USDNOK,JPNCAD");
        params.put("processing_channel_id", "pc_vxt6yftthv4e5flqak6w2i7rim");


        var query = params.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        var host = "https://api.sandbox.checkout.com";
        var pathname = "/forex/rates";
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(host + pathname + '?' + query))
                .header("Authorization", "sk_test_0b9b5db6-f223-49d0-b68f-f6643dd4f808")
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
}