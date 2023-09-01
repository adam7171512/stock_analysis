package org.example.persistence;

import com.google.gson.Gson;
import org.example.model.PortfolioRunResult;

import java.util.List;
import java.util.stream.Collectors;

public class JsonGenerator {

    public String generateJson(PortfolioRunResult result) {
        PortfolioRunResultToDTOConverter converter = new PortfolioRunResultToDTOConverter();
        PortfolioAnalysisDTO dto = converter.convert(result);

        Gson gson = new Gson();
        return gson.toJson(dto);
    }

    public String generateJsonList(List<PortfolioRunResult> results) {
        PortfolioRunResultToDTOConverter converter = new PortfolioRunResultToDTOConverter();

        // Convert each PortfolioRunResult in the list to its corresponding DTO
        List<PortfolioAnalysisDTO> dtoList = results.stream()
                .map(converter::convert)
                .collect(Collectors.toList());

        Gson gson = new Gson();
        return gson.toJson(dtoList);
    }
}
