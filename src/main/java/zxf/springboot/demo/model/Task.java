package zxf.springboot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    private String id;
    private String name;
    private String status;
    private String projectId;
    private Integer priority;
    private Map<String, Object> downstreamResponse;
}