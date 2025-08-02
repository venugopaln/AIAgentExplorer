package com.medplus.aiagent.dao;

import java.util.List;
import java.util.Map;

public interface AIAgentQueryDao {

    List<Map<String, Object>> executeQuery(String query);
}