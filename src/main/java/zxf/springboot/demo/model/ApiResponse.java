package zxf.springboot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String task;
    private Map<String, Object> downstream;
    private Long currentTimeMillis;
    private Map<String, Object> project;
    private String timestamp;

    public static ApiResponse success(String task, Map<String, Object> downstream) {
        ApiResponse response = new ApiResponse();
        response.setTask("DEMO." + task);
        response.setDownstream(downstream);
        response.setCurrentTimeMillis(System.currentTimeMillis());
        response.setTimestamp(java.time.Instant.now().toString());
        return response;
    }

    public static ApiResponse error(String task, Map<String, Object> error) {
        ApiResponse response = new ApiResponse();
        response.setTask("DEMO." + task);
        response.setDownstream(error);
        response.setCurrentTimeMillis(System.currentTimeMillis());
        response.setTimestamp(java.time.Instant.now().toString());
        return response;
    }
}