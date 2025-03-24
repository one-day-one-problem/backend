package site.haruhana.www.dto.problem;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@JsonPropertyOrder({"problems", "currentPage", "pageSize", "totalPages", "totalElements", "numberOfElements", "hasNext", "hasPrevious", "empty"})
public class ProblemPage<T> {

    @JsonProperty("problems")
    private final List<T> problems;            // 문제 목록

    // 페이지 정보
    private final int currentPage;             // 현재 페이지
    private final int pageSize;                // 페이지 크기
    private final int totalPages;              // 전체 페이지 수
    private final long totalElements;          // 전체 요소 수
    private final int numberOfElements;        // 현재 페이지의 요소 수

    // 페이지 상태 정보
    private final boolean hasNext;             // 다음 페이지 존재 여부
    private final boolean hasPrevious;         // 이전 페이지 존재 여부
    private final boolean empty;               // 현재 페이지가 비어있는지 여부

    public ProblemPage(Page<T> page) {
        this.problems = page.getContent();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.numberOfElements = page.getNumberOfElements();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
        this.empty = page.isEmpty();
    }
}
