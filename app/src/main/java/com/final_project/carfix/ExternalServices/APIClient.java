package com.final_project.carfix.ExternalServices;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by etioko on 12/01/2019.
 */

public interface APIClient {

    @POST("/api/predict")
    Call<PostResponse> getPostRequest(@Body PostRequest  body);

}
