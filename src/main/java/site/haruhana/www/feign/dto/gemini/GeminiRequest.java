package site.haruhana.www.feign.dto.gemini;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
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

    /**
     * 프롬프트로 GeminiRequest를 생성하는 팩토리 메서드
     */
    public static GeminiRequest of(String prompt) {
        return GeminiRequest.builder()
                .contents(Collections.singletonList(
                        Content.builder()
                                .parts(Collections.singletonList(
                                        Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
}
