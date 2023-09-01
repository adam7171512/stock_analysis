package org.example;

import org.example.model.BackTester;
import org.example.model.ExecutionMoment;
import org.example.persistence.IDividendRepository;
import org.example.persistence.fileSystem.FilesystemDividendRepository;
import org.example.persistence.fileSystem.GpwFileSystemOhlcRepository;
import org.example.persistence.timeScale.TimescaleGpwDividendRepository;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    public static void displayImageFromURL(String urlString) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a JFrame
                JFrame frame = new JFrame("Image Display");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Fetch the image from the URL
                URL url = new URL(urlString);
                Image image = ImageIO.read(url);

                // Display the image in a JLabel
                JLabel label = new JLabel(new ImageIcon(image));
                frame.getContentPane().add(label, BorderLayout.CENTER);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws IOException {

        IDividendRepository timescaleGpwDividendRepository = new FilesystemDividendRepository();
        GpwFileSystemOhlcRepository filesystemOhlcRepository = new GpwFileSystemOhlcRepository("/home/krzyszfot/Desktop/stocks/");
        BackTester backTester = new BackTester();

        List<String> companies = timescaleGpwDividendRepository.getTickers();

        // remove all wig20 companies
        List<String> wig20Companies = filesystemOhlcRepository.getCompanies("/home/krzyszfot/Desktop/stocks/wig20.txt");
//        companies.removeAll(wig20Companies);

//        List<String> mwig40Companies = filesystemOhlcRepository.getCompanies("/home/krzyszfot/Desktop/stocks/mwig40.txt");
//        companies.removeAll(mwig40Companies);

        List<Runnable> jobs = new ArrayList<>();

        Runnable runnable = backTester.getJob(
                companies,
                -60,
                -20,
                -7,
                60,
                0.02,
                0.2,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(999999999),
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
                LocalDate.MIN,
                LocalDate.MAX
        );
//        jobs.add(runnable);

        Runnable lowYield = backTester.getJob(
                companies,
                -20,
                -20,
                0,
                30,
                0.0,
                0.03,
                BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(999999999),
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
                LocalDate.MIN,
                LocalDate.MAX
        );
//        jobs.add(lowYield);

        Runnable midYield = backTester.getJob(
                companies,
                -20,
                -20,
                0,
                30,
                0.03,
                0.06,
                BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(999999999),
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
//                LocalDate.of(2015, 1, 1),
                LocalDate.MIN,
                LocalDate.MAX
        );
//        jobs.add(midYield);

        Runnable highYield = backTester.getJob(
                companies,
                -20,
                -20,
                0,
                30,
                0.06,
                0.25,
                BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(999999999),
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
                LocalDate.MIN,
                LocalDate.MAX
        );
//        jobs.add(highYield);



        Runnable lowTurnover = backTester.getJob(
                companies,
                -60,
                -20,
                -7,
                60,
                0.02,
                0.18,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(2000000),
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
                LocalDate.MIN,
                LocalDate.MAX
        );
        jobs.add(lowTurnover);

        Runnable midTurnover = backTester.getJob(
                companies,
                -20,
                -20,
                0,
                30,
                0.0,
                2,
                BigDecimal.valueOf(2000000),
                BigDecimal.valueOf(10000000),
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
                LocalDate.MIN,
                LocalDate.MAX
        );
//        jobs.add(midTurnover);

        Runnable highTurnover = backTester.getJob(
                companies,
                -20,
                -20,
                0,
                30,
                0.0,
                2,
                BigDecimal.valueOf(10000000),
                BigDecimal.valueOf(999999999),
                ExecutionMoment.CLOSE,
                ExecutionMoment.CLOSE,
                LocalDate.MIN,
                LocalDate.MAX
        );
//        jobs.add(highTurnover);

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (Runnable job : jobs) {
            executorService.submit(job);
        }
        executorService.shutdown();





//        Gson gson = new Gson();
//        String jsonPayload = gson.toJson(plotData);
//
//        HttpClient client = HttpClients.createDefault();
//        HttpPost post = new HttpPost("http://127.0.0.1:5000/heatmap");
//        post.setEntity(new StringEntity(jsonPayload));
//        post.setHeader("Content-type", "application/json");
//        HttpResponse response = client.execute(post);
//        String jsonResponse = EntityUtils.toString(response.getEntity());
//        HeatmapResponse heatmapResponse = gson.fromJson(jsonResponse, HeatmapResponse.class);
//        displayImageFromURL(heatmapResponse.getHeatmapUrl());

    }
}

