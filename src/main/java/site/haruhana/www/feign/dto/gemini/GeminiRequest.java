package site.haruhana.www.feign.dto.gemini;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GeminiRequest {
    private List<Content> contents;

    @Data
    @Builder
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @Builder
    public static class Part {
        private String text;
    }
}
