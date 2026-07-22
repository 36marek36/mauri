package com.example.mauri.service.volley;

import com.example.mauri.model.VolleyMatch;
import com.example.mauri.model.VolleyMatchResult;

public interface VolleyMatchResultService {
    VolleyMatchResult processResult(VolleyMatch volleyMatch, VolleyMatchResult result);
}
