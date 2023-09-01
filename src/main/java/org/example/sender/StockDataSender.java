package org.example.sender;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.graphs.PlotData;
import org.example.persistence.PortfolioAnalysisDTO;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StockDataSender {

    private final String FLASK_DIVIDEND_ANALYSIS_ENDPOINT = "http://localhost:5000/plot";
    private final String FLASK_STRATEGY_ANALYSIS_ENDPOINT = "http://localhost:5000/strategy";
    private final String FLASK_STRATEGY_COMPARISON_ENDPOINT = "http://localhost:5000/strategycomparison";
    private final String FLASK_HEATMAP_ENDPOINT = "http://localhost:5000/heatmap";

    public void sendToFlask(Map<Integer, BigDecimal> avgDailyReturns, BigDecimal dividendYield) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(FLASK_DIVIDEND_ANALYSIS_ENDPOINT);

            // Convert the Map and dividendYield to JSON format
            StringBuilder jsonBuilder = new StringBuilder("{ \"average_returns\": {");
            for (Map.Entry<Integer, BigDecimal> entry : avgDailyReturns.entrySet()) {
                jsonBuilder.append("\"").append(entry.getKey()).append("\": \"")
                        .append(entry.getValue()).append("\", ");
            }
            jsonBuilder.setLength(jsonBuilder.length() - 2);  // remove the trailing comma and space
            jsonBuilder.append("}, \"dividend_yield\": \"")
                    .append(dividendYield).append("\" }");

            StringEntity entity = new StringEntity(jsonBuilder.toString());
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String result = EntityUtils.toString(responseEntity);
                    System.out.println(result);  // handle the response as required
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStrategyAnalysisDataToFlask(PortfolioAnalysisDTO portfolioData) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(FLASK_STRATEGY_ANALYSIS_ENDPOINT);

            // Convert the PortfolioAnalysisDTO object to JSON format using Gson
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(portfolioData);

            StringEntity entity = new StringEntity(jsonPayload);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String result = EntityUtils.toString(responseEntity);
                    System.out.println(result);  // handle the response as required
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMultipleStrategyAnalysisDataToFlask(List<PortfolioAnalysisDTO> portfolioDataList) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(FLASK_STRATEGY_COMPARISON_ENDPOINT);

            // Convert the List<PortfolioAnalysisDTO> object to JSON format using Gson
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(portfolioDataList);

            StringEntity entity = new StringEntity(jsonPayload);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String result = EntityUtils.toString(responseEntity);
                    System.out.println(result);  // handle the response as required
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPlotDataToflask(PlotData plotData) throws IOException {
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(plotData);

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(FLASK_HEATMAP_ENDPOINT);
        post.setEntity(new StringEntity(jsonPayload));
        post.setHeader("Content-type", "application/json");
        HttpResponse response = client.execute(post);
        String jsonResponse = EntityUtils.toString(response.getEntity());
    }

}
