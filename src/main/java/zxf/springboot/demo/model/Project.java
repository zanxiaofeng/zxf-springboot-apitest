package zxf.springboot.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private String id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}