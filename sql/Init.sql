/* v0.1.0 table 생성 sql */

/* Base Tables */
create table problems
(
    id                     bigint auto_increment primary key,
    created_at             datetime(6)                                               not null,
    updated_at             datetime(6)                                               not null,
    title                  varchar(255)                                              not null,
    question               text                                                      not null,
    category               enum ('ACID', 'AUTHENTICATION', 'AUTHORIZATION', 'AWS_EC2', 'AWS_RDS', 'AWS_S3', 'BROWSER_RENDERING',
        'CI_CD', 'COMPLEXITY', 'CORS', 'CQRS', 'CSRF', 'DATA_STRUCTURE', 'DDD', 'DEADLOCK', 'DESIGN_PATTERN',
        'DISTRIBUTED_SYSTEM', 'DJANGO_ORM', 'DNS', 'DOCKER', 'DYNAMIC_PROGRAMMING', 'ELASTICSEARCH',
        'EVENT_SOURCING', 'FASTAPI', 'GARBAGE_COLLECTION', 'GRAPHQL', 'GRAPH_ALGORITHM', 'HASH', 'HTTP',
        'INDEX', 'ISOLATION_LEVEL', 'JAVASCRIPT_ASYNC', 'JAVASCRIPT_CLOSURE', 'JAVASCRIPT_EVENT_LOOP',
        'JAVASCRIPT_PROMISE', 'JAVASCRIPT_PROTOTYPE', 'JAVA_COLLECTION', 'JAVA_CONCURRENT', 'JAVA_STREAM',
        'JENKINS', 'JOIN', 'JPA_PERSISTENCE', 'JVM', 'JWT', 'KUBERNETES', 'LOAD_BALANCING', 'LOCK', 'LOGGING',
        'MEMORY_MANAGEMENT', 'MONGODB', 'MONITORING', 'MSA', 'MYSQL', 'NEXT_JS', 'NORMALIZATION', 'OAUTH',
        'OSI_MODEL', 'PAGING', 'POSTGRESQL', 'PROCESS', 'PYTHON_ASYNC', 'PYTHON_GENERATOR', 'PYTHON_GIL',
        'REACT_HOOKS', 'REACT_LIFECYCLE', 'REACT_STATE', 'REDIS', 'REST', 'SCHEDULING', 'SOLID',
        'SORTING_ALGORITHM', 'SPRING_AOP', 'SPRING_IOC', 'SPRING_SECURITY', 'SPRING_TRANSACTION',
        'SQL_INJECTION', 'SYNCHRONIZATION', 'TCP_IP', 'THREAD', 'TRANSACTION', 'TREE', 'TYPESCRIPT_TYPE',
        'UDP', 'VIRTUAL_MEMORY', 'WEBSOCKET', 'WEB_PERFORMANCE', 'XSS')              not null,
    difficulty             enum ('EASY', 'MEDIUM', 'HARD')                           not null,
    type                   enum ('MULTIPLE_CHOICE', 'SUBJECTIVE')                    not null,
    problem_provider       enum ('AI', 'USER')                                       not null,
    status                 enum ('ACTIVE', 'INACTIVE', 'UNDER_REVIEW', 'DEPRECATED') not null,
    solved_count           bigint                                                    not null default 0,
    expected_answer_length varchar(255)                                              null,
    sample_answer          text                                                      null
);

create table users
(
    id                bigint auto_increment primary key,
    created_at        datetime(6)                       not null,
    updated_at        datetime(6)                       not null,
    name              varchar(255)                      not null,
    email             varchar(255)                      not null,
    provider          enum ('GOOGLE', 'KAKAO', 'NAVER') not null,
    profile_image_url varchar(255)                      null,
    role              enum ('USER', 'ADMIN')            not null
);

/* Dependent Tables */
create table problem_options
(
    id         bigint auto_increment primary key,
    problem_id bigint not null,
    content    text   not null,
    is_correct bit    not null,

    constraint fk_problem_options_problem
        foreign key (problem_id) references problems (id)
);

create table subjective_gradings
(
    id         bigint auto_increment primary key,
    problem_id bigint not null,
    content    text   not null,

    constraint fk_subjective_gradings_problem
        foreign key (problem_id) references problems (id)
);

create table problem_feedbacks
(
    id          bigint auto_increment primary key,
    created_at  datetime(6)                                             not null,
    updated_at  datetime(6)                                             not null,
    problem_id  bigint                                                  not null,
    reporter_id bigint                                                  not null,
    content     text                                                    not null,
    type        enum ('TYPO', 'INCORRECT_CONTENT', 'AMBIGUOUS_EXPRESSION', 'INCORRECT_ANSWER',
        'DUPLICATE_QUESTION', 'INAPPROPRIATE_DIFFICULTY', 'OTHER')      not null,
    status      enum ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED') not null,

    constraint fk_problem_feedbacks_problem
        foreign key (problem_id) references problems (id),
    constraint fk_problem_feedbacks_user
        foreign key (reporter_id) references users (id)
);

create table submissions
(
    id                   bigint auto_increment primary key,
    user_id              bigint      not null,
    problem_id           bigint      not null,
    submitted_at         datetime(6) not null,
    duration             int         not null,
    submitted_answer     text        not null,
    is_correct           bit         null,
    score                int         null,
    feedback             text        null,
    feedback_provided_at datetime(6) null,

    constraint fk_submissions_user
        foreign key (user_id) references users (id),
    constraint fk_submissions_problem
        foreign key (problem_id) references problems (id)
);
