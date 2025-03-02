package site.haruhana.www.entity.problem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    // Todo: 현재 카테고리는 임시 데이터로, 추후 수정이 필요함.

    // Operating System
    PROCESS("프로세스"),
    THREAD("스레드"),
    DEADLOCK("데드락"),
    SYNCHRONIZATION("동기화"),
    MEMORY_MANAGEMENT("메모리 관리"),
    VIRTUAL_MEMORY("가상 메모리"),
    PAGING("페이징"),
    SCHEDULING("스케줄링"),

    // Network
    OSI_MODEL("OSI 7계층"),
    TCP_IP("TCP/IP"),
    UDP("UDP"),
    HTTP("HTTP/HTTPS"),
    WEBSOCKET("웹소켓"),
    DNS("DNS"),
    LOAD_BALANCING("로드밸런싱"),
    CORS("CORS"),
    REST("REST"),
    GRAPHQL("GraphQL"),

    // Database
    ACID("ACID"),
    TRANSACTION("트랜잭션"),
    ISOLATION_LEVEL("격리수준"),
    LOCK("락"),
    INDEX("인덱스"),
    NORMALIZATION("정규화"),
    JOIN("조인"),
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    MONGODB("MongoDB"),
    REDIS("Redis"),
    ELASTICSEARCH("Elasticsearch"),

    // Backend - Java/Spring
    JAVA_COLLECTION("Java 컬렉션"),
    JAVA_STREAM("Java 스트림"),
    JAVA_CONCURRENT("Java 동시성"),
    JVM("JVM"),
    GARBAGE_COLLECTION("가비지 컬렉션"),
    SPRING_IOC("스프링 IoC"),
    SPRING_AOP("스프링 AOP"),
    SPRING_TRANSACTION("스프링 트랜잭션"),
    JPA_PERSISTENCE("JPA 영속성"),
    SPRING_SECURITY("스프링 시큐리티"),

    // Backend - Python
    PYTHON_GIL("Python GIL"),
    PYTHON_GENERATOR("Python 제너레이터"),
    PYTHON_ASYNC("Python 비동기"),
    FASTAPI("FastAPI"),
    DJANGO_ORM("Django ORM"),

    // JavaScript/TypeScript
    JAVASCRIPT_CLOSURE("클로저"),
    JAVASCRIPT_PROTOTYPE("프로토타입"),
    JAVASCRIPT_EVENT_LOOP("이벤트 루프"),
    JAVASCRIPT_PROMISE("프로미스"),
    JAVASCRIPT_ASYNC("비동기 프로그래밍"),
    TYPESCRIPT_TYPE("타입스크립트 타입 시스템"),

    // Frontend
    REACT_LIFECYCLE("React 생명주기"),
    REACT_HOOKS("React Hooks"),
    REACT_STATE("React 상태관리"),
    NEXT_JS("Next.js"),
    BROWSER_RENDERING("브라우저 렌더링"),
    WEB_PERFORMANCE("웹 성능최적화"),

    // DevOps
    DOCKER("Docker"),
    KUBERNETES("Kubernetes"),
    CI_CD("CI/CD"),
    JENKINS("Jenkins"),
    AWS_EC2("AWS EC2"),
    AWS_S3("AWS S3"),
    AWS_RDS("AWS RDS"),
    MONITORING("모니터링"),
    LOGGING("로깅"),

    // Architecture & Design
    DESIGN_PATTERN("디자인패턴"),
    SOLID("SOLID 원칙"),
    DDD("도메인 주도 설계"),
    MSA("마이크로서비스"),
    CQRS("CQRS"),
    EVENT_SOURCING("이벤트소싱"),

    // Security
    AUTHENTICATION("인증"),
    AUTHORIZATION("인가"),
    JWT("JWT"),
    OAUTH("OAuth"),
    XSS("XSS"),
    CSRF("CSRF"),
    SQL_INJECTION("SQL 인젝션"),

    // Computer Science
    DATA_STRUCTURE("자료구조"),
    SORTING_ALGORITHM("정렬 알고리즘"),
    GRAPH_ALGORITHM("그래프 알고리즘"),
    DYNAMIC_PROGRAMMING("동적 프로그래밍"),
    COMPLEXITY("시간/공간 복잡도"),
    HASH("해시"),
    TREE("트리"),
    DISTRIBUTED_SYSTEM("분산시스템");

    private final String description;
}
