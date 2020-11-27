package org.techtown.blackbox;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class CollisionLocationRequest extends StringRequest {
    final static private String URL = "http://34.64.132.117/accident.php";
    private Map<String, String> map;

    public CollisionLocationRequest(String collision_long, String collision_lat, String UserId,
                               Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("accident_latitude", collision_lat);
        map.put("accident_longitude", collision_long);
        map.put("id", UserId);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
