package com.final_project.carfix.ExternalServices;

import java.util.List;

public class PostResponse {

    private List<Float> prediction;

    public Float getPrediction() {
           return prediction.get(0);

    }


}
