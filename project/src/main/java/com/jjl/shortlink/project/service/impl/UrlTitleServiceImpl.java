package com.jjl.shortlink.project.service.impl;

import com.jjl.shortlink.project.service.UrlTitleService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class UrlTitleServiceImpl implements UrlTitleService {
    public  String getTitleByUrl(String url) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            Document doc = Jsoup.parse(response.getEntity().getContent(), null, url);
            return doc.title();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching URL";
        }
    }
}