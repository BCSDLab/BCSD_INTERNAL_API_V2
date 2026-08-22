-- T-20 초기 콘텐츠 시드: BCSD_HOMEPAGE_RECODE의 정적 데이터를 이관한다 (ADR-018).
-- track_page_member는 넣지 않는다 — 실 member 테이블에 아직 부원 명부가 거의 채워지지 않아
-- (2026-08-22 기준 5명) 이름 매칭이 불가능하다. 명부가 채워진 뒤 별도로 진행한다.

-- ===== tech_stack (전역 카탈로그, 트랙 간 공유) =====
INSERT INTO tech_stack (name, icon_url) VALUES
    ('After Effects', 'https://image.bcsdlab.com/tech/aftereffects.svg'),
    ('Alamofire', 'https://image.bcsdlab.com/tech/alamofile.svg'),
    ('Android', 'https://image.bcsdlab.com/tech/android.svg'),
    ('AWS', 'https://image.bcsdlab.com/tech/aws.svg'),
    ('BigQuery', 'https://image.bcsdlab.com/tech/bigquery-icon.svg'),
    ('Burp Suite', 'https://image.bcsdlab.com/tech/burpsuit.svg'),
    ('C++', 'https://image.bcsdlab.com/tech/c++.svg'),
    ('Coroutine', 'https://image.bcsdlab.com/tech/coroutine.svg'),
    ('C#', 'https://image.bcsdlab.com/tech/csharp.svg'),
    ('CSS3', 'https://image.bcsdlab.com/tech/css3.svg'),
    ('Figma', 'https://image.bcsdlab.com/tech/figma.svg'),
    ('Flyway', 'https://image.bcsdlab.com/tech/flyway.svg'),
    ('Google Analytics', 'https://image.bcsdlab.com/tech/ga-icon.svg'),
    ('Glide', 'https://image.bcsdlab.com/tech/glide.svg'),
    ('Hilt', 'https://image.bcsdlab.com/tech/hilt.svg'),
    ('HTML', 'https://image.bcsdlab.com/tech/html.svg'),
    ('Illustrator', 'https://image.bcsdlab.com/tech/illustrator.svg'),
    ('Java', 'https://image.bcsdlab.com/tech/java.svg'),
    ('JavaScript', 'https://image.bcsdlab.com/tech/javascript.svg'),
    ('Jenkins', 'https://image.bcsdlab.com/tech/jenkins.svg'),
    ('Kotlin', 'https://image.bcsdlab.com/tech/kotlin.svg'),
    ('MongoDB', 'https://image.bcsdlab.com/tech/mongodb.svg'),
    ('MySQL', 'https://image.bcsdlab.com/tech/mysql.svg'),
    ('Next.js', 'https://image.bcsdlab.com/tech/nextjs.svg'),
    ('Nmap', 'https://image.bcsdlab.com/tech/nmap.svg'),
    ('Node.js', 'https://image.bcsdlab.com/tech/nodejs.svg'),
    ('Notion', 'https://image.bcsdlab.com/tech/notion.svg'),
    ('Photoshop', 'https://image.bcsdlab.com/tech/photoshop.svg'),
    ('Python', 'https://image.bcsdlab.com/tech/python.svg'),
    ('React', 'https://image.bcsdlab.com/tech/react.svg'),
    ('Redis', 'https://image.bcsdlab.com/tech/redis.svg'),
    ('Retrofit', 'https://image.bcsdlab.com/tech/retrofit.svg'),
    ('Slack', 'https://image.bcsdlab.com/tech/slack.svg'),
    ('Spring', 'https://image.bcsdlab.com/tech/spring.svg'),
    ('Swagger', 'https://image.bcsdlab.com/tech/swagger.svg'),
    ('Swift', 'https://image.bcsdlab.com/tech/swift.svg'),
    ('Tableau', 'https://image.bcsdlab.com/tech/tableau-icon.svg'),
    ('UIKit', 'https://image.bcsdlab.com/tech/uikit.svg'),
    ('Unity', 'https://image.bcsdlab.com/tech/unity.svg'),
    ('Unreal Engine', 'https://image.bcsdlab.com/tech/unreal.svg'),
    ('Vite', 'https://image.bcsdlab.com/tech/vite.svg'),
    ('Webpack', 'https://image.bcsdlab.com/tech/webpack.svg'),
    ('Xcode', 'https://image.bcsdlab.com/tech/xcode.svg');

-- ===== track_page =====
INSERT INTO track_page (track_id, slug, display_name, tagline, display_order, is_published) VALUES
    ((SELECT id FROM track WHERE code = 'FRONTEND'), 'frontend', 'Frontend', '웹사이트와 사용자 인터페이스를 개발해요.', 0, TRUE),
    ((SELECT id FROM track WHERE code = 'BACKEND'), 'backend', 'Backend', '서비스의 서버와 데이터 관리를 담당해요.', 1, TRUE),
    ((SELECT id FROM track WHERE code = 'ANDROID'), 'android', 'Android', '안드로이드 앱을 개발하고 구현해요.', 2, TRUE),
    ((SELECT id FROM track WHERE code = 'IOS'), 'ios', 'iOS', 'iOS 앱을 개발하고 구현해요.', 3, TRUE),
    ((SELECT id FROM track WHERE code = 'DESIGN'), 'design', 'Design', '서비스의 디자인과 브랜딩 디자인을 담당해요.', 4, TRUE),
    ((SELECT id FROM track WHERE code = 'GAME'), 'game', 'Game', '게임 개발과 구현을 담당해요.', 5, TRUE),
    ((SELECT id FROM track WHERE code = 'DATA'), 'data-analyst', 'Data Analyst', '데이터 분석과 시각화를 담당해요.', 6, TRUE),
    ((SELECT id FROM track WHERE code = 'PM'), 'product-manager', 'Product Manager', '프로젝트 기획과 팀 협업을 주도해요.', 7, TRUE),
    ((SELECT id FROM track WHERE code = 'SECURITY'), 'security', 'Security', '서비스의 보안과 안전을 책임져요.', 8, TRUE);

-- ===== track_study_point =====
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'frontend'), '확장성과 재사용성을
 고려한 엔지니어링', 'React를 활용한 컴포넌트 기반의 UI 개발', 'https://image.bcsdlab.com/study/cogwheel-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), '새로운 기술 토픽 공유', '지속적으로 발전하는 웹 트렌드에 맞춰
최신 기술 도입을 논의하고, 이를 프로젝트에 적용', 'https://image.bcsdlab.com/study/talk-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), '사용자 경험 향상', '웹 접근성, 크로스 브라우징, 검색엔진 최적화,
그리고 개인화된 경험 제공', 'https://image.bcsdlab.com/study/enhance-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'backend'), '서버 개발 및 인프라 구축 경험', '백엔드의 전반적인 분야 학습', 'https://image.bcsdlab.com/study/db-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'backend'), '전공 지식의 실질적 활용', '컴퓨터공학 지식을 밀접하게 활용', 'https://image.bcsdlab.com/study/pc-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'backend'), '안정적인 서비스 운영', '현업에서 사용하는 서비스 지탱 기술', 'https://image.bcsdlab.com/study/engineer-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'android'), '구글 플레이에 등록', '애플리케이션 개발 후 실제 마켓에 런칭', 'https://image.bcsdlab.com/study/play-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'android'), '최신 트렌드에 대한 학습', '메이저 업데이트와 오픈소스에 대한 이해', 'https://image.bcsdlab.com/study/book-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'android'), '디자인 패턴의 실무적인 활용', 'VIEW 위주의 앱에서의 설계와 이해', 'https://image.bcsdlab.com/study/design-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'ios'), '사용자 인터페이스 설계', '다양한 UI 컴포넌트를 사용해 직관적이고
반응성이 뛰어난 iOS 애플리케이션 개발', 'https://image.bcsdlab.com/study/toggle-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'ios'), '확장 가능한 모듈 설계', '유연한 모듈화 구조를 통해 새로운 기능 추가 및 유지보수가 용이한 앱 개발', 'https://image.bcsdlab.com/study/design-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'ios'), 'Swift의 최신 기능 활용', 'Swift의 최신 기능을 사용해 빠르고 안전한 코드 작성', 'https://image.bcsdlab.com/study/fragment-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'design'), '상용화 프로젝트 경험', '실제 웹/앱 디자인 후 상용화하는 프로세스를
 통해 실무 능력 배양', 'https://image.bcsdlab.com/study/laptop-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'design'), 'UI/UX 커리어 설계', 'UI/UX 디자이너로 성장하기 위한
 선배들의 커리어 피드백', 'https://image.bcsdlab.com/study/flag-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'design'), '실무 노하우 학습', '학교에서 배우지 않는 실무 노하우로
 전문 인재 육성', 'https://image.bcsdlab.com/study/book-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'game'), '다각적 학습 기회', '게임 프로그래밍을 배울 수 있는 기회', 'https://image.bcsdlab.com/study/book-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'game'), '재미를 만듭니다', '원하는 게임을 직접 만들 수 있도록 지원', 'https://image.bcsdlab.com/study/controller-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'game'), '함께 만드는 게임', '상호발전과 유대감 형성', 'https://image.bcsdlab.com/study/together-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'data-analyst'), '데이터 처리 및 분석', 'Python 및 SQL을 활용한
 데이터 분석 및 시각화', 'https://image.bcsdlab.com/study/data-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'data-analyst'), 'A/B 테스트 설계 및 실행', 'A/B 테스트의 기본 원리와 실험
 설계 방법을 이해 및 인사이트 도출', 'https://image.bcsdlab.com/study/abtest-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'data-analyst'), '리텐션 분석 및 고객 행동 이해', '고객 유지율, 이탈률 등의 지표 분석을
 통한 서비스 개선 방향 제시', 'https://image.bcsdlab.com/study/uphuman-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'product-manager'), '웹/앱 기획 실습을 통한
 기획의 기본 이해 학습', '웹/앱 기획 실습을 통해 기획의
 기본 개념과 프로세스를 학습합니다.', 'https://image.bcsdlab.com/study/book-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'product-manager'), '유저 중심의 서비스 기획', '타겟 유저의 니즈를 이해하고, 이를 반영한 서비스 기획
 능력을 배양합니다.', 'https://image.bcsdlab.com/study/person-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'product-manager'), '기획 문서 작성 및 관리', '화면정의서, 기능정의서 작성 및 프로토타입 제작 등의
 기획 문서 작성 및 관리 방법을 학습합니다.', 'https://image.bcsdlab.com/study/paper-icon.svg', 2);
INSERT INTO track_study_point (track_page_id, title, description, icon_image_url, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'security'), '웹해킹 기초 및 보안 원리 학습', '네트워크 보안 개념과 함께 웹 서비스에서
 발생할 수 있는 주요 보안 취약점 학습', 'https://image.bcsdlab.com/study/book-icon.svg', 0),
    ((SELECT id FROM track_page WHERE slug = 'security'), '취약점 분석 및 공격 기법 실습', 'OWASP Top 10을 기반으로 다양한
 웹 서비스 취약점 분석 및 모의해킹 실습', 'https://image.bcsdlab.com/study/target-icon.svg', 1),
    ((SELECT id FROM track_page WHERE slug = 'security'), '보안 강화를 위한 방어 기법 적용', '프로젝트를 통해 보안 취약점에 대한 방어 기술 적용', 'https://image.bcsdlab.com/study/prevent-icon.svg', 2);

-- ===== track_page_tech_stack =====
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'frontend'), (SELECT id FROM tech_stack WHERE name = 'HTML'), 0),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), (SELECT id FROM tech_stack WHERE name = 'CSS3'), 1),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), (SELECT id FROM tech_stack WHERE name = 'JavaScript'), 2),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), (SELECT id FROM tech_stack WHERE name = 'Webpack'), 3),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), (SELECT id FROM tech_stack WHERE name = 'React'), 4),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), (SELECT id FROM tech_stack WHERE name = 'Vite'), 5),
    ((SELECT id FROM track_page WHERE slug = 'frontend'), (SELECT id FROM tech_stack WHERE name = 'Next.js'), 6);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'Java'), 0),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'Node.js'), 1),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'Spring'), 2),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'MySQL'), 3),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'Redis'), 4),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'Swagger'), 5),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'Jenkins'), 6),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'AWS'), 7),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'Flyway'), 8),
    ((SELECT id FROM track_page WHERE slug = 'backend'), (SELECT id FROM tech_stack WHERE name = 'MongoDB'), 9);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'android'), (SELECT id FROM tech_stack WHERE name = 'Kotlin'), 0),
    ((SELECT id FROM track_page WHERE slug = 'android'), (SELECT id FROM tech_stack WHERE name = 'Android'), 1),
    ((SELECT id FROM track_page WHERE slug = 'android'), (SELECT id FROM tech_stack WHERE name = 'Retrofit'), 2),
    ((SELECT id FROM track_page WHERE slug = 'android'), (SELECT id FROM tech_stack WHERE name = 'Glide'), 3),
    ((SELECT id FROM track_page WHERE slug = 'android'), (SELECT id FROM tech_stack WHERE name = 'Java'), 4),
    ((SELECT id FROM track_page WHERE slug = 'android'), (SELECT id FROM tech_stack WHERE name = 'Hilt'), 5),
    ((SELECT id FROM track_page WHERE slug = 'android'), (SELECT id FROM tech_stack WHERE name = 'Coroutine'), 6);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'ios'), (SELECT id FROM tech_stack WHERE name = 'Swift'), 0),
    ((SELECT id FROM track_page WHERE slug = 'ios'), (SELECT id FROM tech_stack WHERE name = 'UIKit'), 1),
    ((SELECT id FROM track_page WHERE slug = 'ios'), (SELECT id FROM tech_stack WHERE name = 'Alamofire'), 2),
    ((SELECT id FROM track_page WHERE slug = 'ios'), (SELECT id FROM tech_stack WHERE name = 'Xcode'), 3);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'design'), (SELECT id FROM tech_stack WHERE name = 'Photoshop'), 0),
    ((SELECT id FROM track_page WHERE slug = 'design'), (SELECT id FROM tech_stack WHERE name = 'Illustrator'), 1),
    ((SELECT id FROM track_page WHERE slug = 'design'), (SELECT id FROM tech_stack WHERE name = 'After Effects'), 2),
    ((SELECT id FROM track_page WHERE slug = 'design'), (SELECT id FROM tech_stack WHERE name = 'Figma'), 3);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'game'), (SELECT id FROM tech_stack WHERE name = 'C++'), 0),
    ((SELECT id FROM track_page WHERE slug = 'game'), (SELECT id FROM tech_stack WHERE name = 'C#'), 1),
    ((SELECT id FROM track_page WHERE slug = 'game'), (SELECT id FROM tech_stack WHERE name = 'Unity'), 2),
    ((SELECT id FROM track_page WHERE slug = 'game'), (SELECT id FROM tech_stack WHERE name = 'Unreal Engine'), 3);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'data-analyst'), (SELECT id FROM tech_stack WHERE name = 'Python'), 0),
    ((SELECT id FROM track_page WHERE slug = 'data-analyst'), (SELECT id FROM tech_stack WHERE name = 'BigQuery'), 1),
    ((SELECT id FROM track_page WHERE slug = 'data-analyst'), (SELECT id FROM tech_stack WHERE name = 'Google Analytics'), 2),
    ((SELECT id FROM track_page WHERE slug = 'data-analyst'), (SELECT id FROM tech_stack WHERE name = 'Tableau'), 3);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'product-manager'), (SELECT id FROM tech_stack WHERE name = 'Notion'), 0),
    ((SELECT id FROM track_page WHERE slug = 'product-manager'), (SELECT id FROM tech_stack WHERE name = 'Figma'), 1),
    ((SELECT id FROM track_page WHERE slug = 'product-manager'), (SELECT id FROM tech_stack WHERE name = 'Slack'), 2);
INSERT INTO track_page_tech_stack (track_page_id, tech_stack_id, display_order) VALUES
    ((SELECT id FROM track_page WHERE slug = 'security'), (SELECT id FROM tech_stack WHERE name = 'Python'), 0),
    ((SELECT id FROM track_page WHERE slug = 'security'), (SELECT id FROM tech_stack WHERE name = 'Burp Suite'), 1),
    ((SELECT id FROM track_page WHERE slug = 'security'), (SELECT id FROM tech_stack WHERE name = 'Nmap'), 2);

-- ===== curriculum =====
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'frontend'), 'Frontend', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'backend'), 'Backend', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'android'), 'Android', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'ios'), 'iOS', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'design'), 'Design', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'game'), 'Game', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'data-analyst'), 'Data Analyst', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'product-manager'), 'Product Manager', TRUE, 0);
INSERT INTO curriculum (track_page_id, name, is_published, display_order) VALUES ((SELECT id FROM track_page WHERE slug = 'security'), 'Security', TRUE, 0);

-- ===== curriculum_week / curriculum_topic / curriculum_topic_detail =====
DO $$
DECLARE
    v_curriculum_id BIGINT;
    v_week_id BIGINT;
    v_topic_id BIGINT;
BEGIN
    -- frontend
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'frontend');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'FrontEnd는 무엇을 배우는 것인가? (역할과 이해)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'HTML', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTML이 뭘까?', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Tag, Element, Attribute', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTML 문서 구조 (head, body)', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'CSS', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CSS가 뭘까? (구조, 적용방식)', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'BOX Model', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CSS Layout', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'JavaScript', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '표준 내장 객체', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '식, 연산자', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '선언문', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '함수와 화살표 함수', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'TypeScript', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'DOM', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTML 데이터 읽기, 변경', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '템플릿 리터럴', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'BOM', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '브라우저 API', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'BOM 객체', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Event', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Event binding', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '이벤트 종류와 객체', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '흐름과 제어', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '웹 동작 방식', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '웹 서버와 클라이언트', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'URL', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '동기/비동기', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Event Loop', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'AJAX, JSON', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'RESTful API', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'JS 비동기', 4) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'callback', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Promise, async/await', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Fetch API, axios', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'debounce, throttle', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'loading', 5) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'placeholder data, skeleton', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'lazy loading', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Interactive Web 실습', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 6, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'FE Framework', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'node.js', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Webpack', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'package manager', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'npm, pnpm', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'yarn, yarn berry', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '렌더링 방식', 4) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SPA, MPA', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CSR, SSR', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습 : React 프로젝트 만들기', 5) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Vite', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '프로젝트 배포 (github actions, CI/CD)', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'package.json', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '구조 학습', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 7, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Virtual DOM', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'JSX', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'html과의 차이', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '규칙과 attribute 작성', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'props 전달', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'event handling', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'key', 4);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Component(Class, Function)', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Hooks', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'State', 4) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useState', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '불변성', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Life Cycle', 5) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'react의 life cycle', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useEffect', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'ref', 6) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useRef', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'React router', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'React Router DOM', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useParams', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useLocation', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useNavigate', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'React 스타일링', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'BEM이란?', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'React 스타일링 방법', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '(CSS module, Styled Components, Emotion, tailwind)', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Component', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '조합과 상속', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '제어와 비제어', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Reusing', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HOC', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Context API', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Compound pattern', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Custom Hooks', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'side Effect', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'React Fragment', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Portal', 4) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'useContext, useReducer', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '메모이제이션', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useMemo', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useCallback', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '라이브러리', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Zustand', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Recoil', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Redux Toolkit', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Jotai', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'useEffect 활용', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'api 모듈화', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Fetching state, Server state', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'tanstack query', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useSWR', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Suspense', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Error Boundaries', 4) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, NULL, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '프로젝트에 TS 환경 추가하기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'props, element, event type', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'hook, tanstack query type', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '브라우저 저장소', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'localStorage', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'sessionStorage', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Cookie(jwt, session)', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Fetching state, Server state', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'tanstack query', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'useSWR', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Suspense', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Error Boundaries', 4) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 13, NULL, 12) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'React 실습', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '배운 내용 바탕으로 React 프로젝트 진행', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 14, 17, 13) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Frontend 개인 프로젝트', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 18, NULL, 14) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Frontend 응용 - 회고', 0) RETURNING id INTO v_topic_id;
    -- backend
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'backend');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '개발환경 세팅하기 (IntelliJ, JDK)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '인텔리제이 설치', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'JDK 설정', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: 자바 온보딩 미션
(자동차경주 게임 구현)', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'https://github.com/BCSDLab-EDU/java-racingcar', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '객체지향 프로그래밍', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SOLID 원칙', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'OOP의 4가지 특징', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: 자동차 경주 게임 - 리팩터링', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '데이터베이스 기본', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '관계형 데이터베이스', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'RDBMS', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SQL (DDL, DCL, DML)', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'NoSQL', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: SQL 쿼리 작성', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: 데이터베이스 설계', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '네트워크 기본', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'IP 주소, PORT, DNS, URL, URI', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'HTTP', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP 메시지의 구조와 기능', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP 메소드와 응답코드', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP를 이용한 웹 요청 흐름', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'REST API', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: REST API 설계하기', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Spring', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Library, Framework', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Spring Framework란?', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Spring과 SpringBoot', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: Spring Boot 프로젝트
환경설정', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 6, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Spring MVC', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'MVC란?', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Spring MVC', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Spring MVC Request LifeCycle', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: GET, POST 요청하는
API 만들기', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 7, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Spring JDBC', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'JdbcTemplate', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'H2 Database', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: JDBC를 이용한 CRUD', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Spring Bean', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'POJO Bean과 Spring Bean', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Bean Scope (Singleton/Prototype)', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Servlet과
Servlet Container', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '의존성 주입(DI)
제어의 역전(IOC)', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Spring Bean 생명주기', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '관점지향 프로그래밍(AOP)', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: AOP를 활용한 함수 실행시간 측정하기', 4) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'ORM과 JPA', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Entity, 영속화, 1차 캐시', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Spring Data JPA', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: 7주차에서 작성한 코드
JPA로 리팩터링', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'JPA 연관관계 매핑', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Spring MVC Request
Lifecycle', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '컨트롤러와 서비스의 차이', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'URL Mapping', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '비즈니스 로직', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: AOP를 활용한 로그 측정', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '보안 기초', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '보안, 암호화, 복호화', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '해싱 알고리즘', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '웹에서의 보안', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP와 HTTPS', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '쿠키, 세션, JWT', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'BCrypt', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: JWT를 활용하여 로그인
구현하기', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, NULL, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'AWS와 배포', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'EC2', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '간단한 애플리케이션 배포 흐름', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '회고 안내', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '회고 프로젝트 설명 및 일정 안내', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '프로젝트 진행 방식 설명', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 13, 16, 12) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'PROJECT', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '자유 주제로 개인 프로젝트 진행', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 17, NULL, 13) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '회고', 0) RETURNING id INTO v_topic_id;
    -- android
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'android');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Kotlin 이해하기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Android에 대한 이해', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Android란 무엇인가', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Android 버전 및 특징 (Android 1.0 ~ Android 16)', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '안드로이드 스튜디오 설치', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Git flow 학습', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Git flow cheatsheet', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Naming Convention', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'XML Naming Convention', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Kotlin Naming Convention', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Const Naming Convention', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Widget', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'View란', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Widget 종류', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Widget 사용 방법', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Widget 속성', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Layout', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'LinearLayout이란', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'RelativeLayout이란', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'FrameLayout이란', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'ConstraintLayout이란', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Resource', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Drawable', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Layout', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Mipmap', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Color', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Strings', 4);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Styling and Theming', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'themes.xml과 styles.xml', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Style 및 Custom Style', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '다양한 화면, 버전 및
다크 모드 대응하기', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Intent', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Intent extras와 bundle', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'View와 상호작용', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'onClick, onLongClick', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'text와 drawable을 동적으로 변경하기', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'textWatcher', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Activity', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Activity 생명주기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Activity 전환', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Activity Result API', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Fragment', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Fragment 특징', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Fragment 생명주기', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'FragmentManager와 FragmentTransaction', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Fragment 생성자 이슈', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Dialog 이해하기', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Dialog 특징', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Dialog 종류', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '커스텀 Dialog', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 6, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'EventListener', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'ListView', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'RecyclerView', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'RecyclerView vs ListView', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'LayoutManager', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Adapter Pattern, ViewHolder Pattern', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'RecyclerView의 동작 원리', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 7, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '권한', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Marshmallow 이전과 이후', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'shouldShowRequestPermissionRationale', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Notification', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Notification 종류', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Oreo 이전과 이후 버전의 Notification', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Broadcast Receiver', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '안드로이드의 파일 관리 방식', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Scoped Storage', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Pie 이전의 파일 관리 방식', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Content Provider', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Content Provider에 대해 이해하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'MediaStore로 미디어 파일에 접근하는 방식 이해하기', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '안드로이드 버전 별
 세분화된 파일 권한 이해하기', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '~ Android 9.0', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Android 10 ~ Android 12', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Android 13 ~', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Service', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Service 생명주기', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Foreground, Background', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Foreground와 Background란', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Oreo 이후의 Background 제한', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Thread', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Thread란', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '안드로이드 UI Thread와 Thread', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Coroutine', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Coroutine이란', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Coroutine vs Thread', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CoroutineScope, Dispatcher', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Suspend function', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '디자인 패턴', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '디자인 패턴이란', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'MVC, MVP, MVVM, MVI', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'View Binding과 Data Binding', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'View Binding', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Data Binding', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Jetpack', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'ViewModel', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Room', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'LiveData', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, NULL, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Clean architecture', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '의존성 주입', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Hilt', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '이미지 처리 Opensource', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Glide', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Coil', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Firebase', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 13, NULL, 12) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Okhttp', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Retrofit', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '난독화', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '난독화란 무엇인가', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Proguard R8', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 14, 16, 13) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '자율 상용화 프로젝트 개발', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 17, NULL, 14) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '회고', 0) RETURNING id INTO v_topic_id;
    -- ios
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'ios');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'iOS 개발의 기본 이해 및 개발
환경 준비', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'iOS 플랫폼 개요', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '앱 생태계', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Swift 언어의 특징', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Objective-C', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Swift의 장점', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Swift 기초 문법', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '변수와 상수, 데이터 타입', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '배열과 딕셔너리, 집합', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Swift 심화 문법', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '옵셔널과 옵셔널 바인딩', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '접근 제어와 초기화 메서드', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '익스텐션', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '열거형(Enum), 구조체(Struct), 클래스(Class)', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '프로퍼티와 메서드', 4);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '메모리 구조', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'iOS의 메모리 구조', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'ARC', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '인스턴스 생성 및 소멸', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '클로저', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Named Closure, Unnamed Closure', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '캡처 리스트', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Trailing Closure', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '프로토콜', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '기본 UI 컴포넌트', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UILabel', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UIButton', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UITextField', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UIImageView', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UIView', 4);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'iOS 앱의 생명 주기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Inactive, Active, Background, Suspended', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'AppDelegate, SceneDelegate', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'iOS 앱의 구조', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '스토리보드를 통한 UI 구성', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Scene과 Segue', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UIStackView', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Human Interface
Guidelines', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Auto Layout과 제약 조건', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '제약 조건(Constraints) 이해 및 활용', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CodeBase, Storyboard 방식의 차이', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UIKit vs SwiftUI', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Animation', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UIView.animate', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Core Animation', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 6, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'NavigationController', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Navigation Stack', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'ViewController
Lifecycle', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'viewDidLoad, viewWillAppear, viewDidAppear', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '객체지향 프로그래밍', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '상속, 캡슐화, 다형성, 추상화', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SOLID 원칙', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 7, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '디자인 패턴, 아키텍처', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'MVC Pattern', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'MVVM Pattern', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Apple의 MVC는 무엇이 다른가', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '프로토콜 지향 프로그래밍', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '프로토콜을 통한 다형성 구현', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Initializer Delegation', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '많은 양의 데이터를 표시하는 방법', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UICollectionView', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UITableView', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '함수형 프로그래밍', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '고차함수', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '순수 함수, 불변성', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Swift에서 데이터를 
저장하는 방법', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SQLite', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Core Data', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Realm', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'User Defaults', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Keychain', 4);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '다양한 UI 컴포넌트', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UISlider', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UISwitch', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UIProgressView', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UIScrollView', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '데이터 전달 방법', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Delegate Pattern', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Closure', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Combine', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'NotificationCenter', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '데이터 전달 시 메모리 관리', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'weak self, unowned', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'API Design Guideline', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Combine 심화', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Publisher, Subscriber', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'RxSwift', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '서드파티 라이브러리 설치 방법', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CocoaPods', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SPM', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Carthage', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '서드파티 라이브러리', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Kingfisher', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SnapKit', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Alamofire', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, NULL, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '네트워크 요청', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Alamofire', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'URLSession', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Codable', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '에러 처리 기법', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Result 타입', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '제네릭', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 13, NULL, 12) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '비동기 프로그래밍', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'GCD', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'OperationQueue', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Async/Await &
 Concurrency', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 14, NULL, 13) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Test Code 작성', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UnitTest', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'UITest', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '의존성 주입과 Mocking', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Testable한 코드란?', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Sandbox의 개념', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 15, 18, 14) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '프로젝트 개발', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 19, NULL, 15) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '회고', 0) RETURNING id INTO v_topic_id;
    -- design
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'design');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UI/UX란 무엇인가 조사해보기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사전과제 피드백', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UI/UX 디자이너로서의
커리어 설계', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '강의 진행', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '동아리 내에서 UI/UX가 하는 작업', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '멘토의 강의(OFF-LINE / ON-LINE)', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '앱의 유형별 차이와 Android / iOS 디자인 시 고려사항', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사전과제 피드백', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Android / iOS 디자인 시
고려사항', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '강의 진행', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UI/UX 디자인 트렌드', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사전과제 피드백', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '참고 사이트', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Notefolio', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Pinterest', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Behance', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '디자인 트렌드', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '강의 진행', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UI/UX 디자인 시스템,
기업사례 조사', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사전과제 피드백', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '디자인시스템 + Tool의 이해', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '강의 진행', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '현업에서 쓰이는 도구들의
종류와 쓰임새', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Adobe 계열', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'ProtoPie', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Sketch', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Zeplin', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'BCSD에서 사용하는 도구', 4);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '디자인 프로세스 조사하기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사전과제 피드백', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UI/UX 디자인 프로세스', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '강의 진행', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 6, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '와이어프레임', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사전과제 피드백', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 7, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'GUI', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사전과제 피드백', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'GUI Benchmarking
Practice', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Mobile 2개, Web 2개씩', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, 11, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'GUI Benchmarking
Practice', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Mobile 2개, Web 2개씩', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'UI 리뉴얼', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Figma 사용법', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Figma에서 기존 프로젝트 확인', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 13, 17, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '내가 만드는 상용화 서비스', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '서비스 기획', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Wireframe 제작', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'GUI 디자인', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '제안서 제출', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 18, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '자신이 만든 GUI 페이지
 업로드하기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '과제', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 19, NULL, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '회고', 0) RETURNING id INTO v_topic_id;
    -- game
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'game');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'GIT', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '버전 관리 시스템과 Git', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'C#', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '자료형, 문자 입출력, 배열', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '선택문과 반복문, 상수', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'C#', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '함수와 모듈화', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '클래스', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '객체지향 프로그래밍', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '객체지향 프로그래밍(OOP)이란?', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '자료구조와 알고리즘', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'C#', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'COLLECTION', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 2D', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Unity Interface, GameObject, Component', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Transform, Event Function', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 2D RPG 제작실습 1', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 2D', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Input, Physics, Prefab, Sprite', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Layer, UGUI, Animation', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 2D RPG 제작실습 2', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 6, 7, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 2D 모작 프로젝트 기획 및 구현', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '회고 문서화', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '프로젝트 회고 및 프로젝트 진행 내역 문서화', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Vector에 대한 이해', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '3D 공간에서의 Point와 Vector의 차이점', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '월드 좌표계, 로컬 좌표계', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '벡터의 내적', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '벡터의 외적', 3);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Quaternion에 대한 이해', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Euler Angle', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Gimbal Lock', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Quaternion', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 3D 튜토리얼', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'John Lemon''s Haunted Jaunt', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 3D 게임 제작의 이해', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 3D FPS 실습', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Unity 3D FPS 실습', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '개인 프로젝트 구상 및
 기획서 작성', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 13, 16, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '개인 프로젝트', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '프로젝트 진행사항 공유 및 피드백', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 17, NULL, 12) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '개인 프로젝트 시연 및 회고 진행', 0) RETURNING id INTO v_topic_id;
    -- data-analyst
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'data-analyst');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'PYTHON (정제)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '파이썬과 R의 차이', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '데이터 가져오기와 데이터 프레임', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '데이터의 타입이란?', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'SQL (SELECT, FROM, WHERE)?', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SQL이란?', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '데이터를 탐색하는 방법', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'PYTHON (요약)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '데이터 요약하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '데이터 그룹화하기', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'SQL (GROUP BY, HAVING, SUM/COUNT)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '집계를 활용한 데이터 탐색', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, 7, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'PYTHON (시각화)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '시각화 라이브러리 소개', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '다양한 Plot 소개 및 실습', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'PYTHON (검증)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '귀무가설과 대립가설', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '정규성 검증', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '등분산성 검증', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'SQL (JOIN, UNNEST)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'JOIN이란?', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '각 조인의 방법', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '빅쿼리(BigQuery)의 UNNEST', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'PYTHON (예측)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'ERD를 활용한 데이터 탐색', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '모델 생성', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'SQL (WITH)', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'WITH문과 파티션', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '데이터 결과 검증', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, 13, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'A/B TEST', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'A/B Test 소개', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'A/B Test 시스템 구성 이해', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'A/B Test 통계 이해', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'A/B Test 분석', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'A/B Test 시각화', 4);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 14, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '리텐션(RETENTION) 분석', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '리텐션 분석 소개', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '코호트(Cohort) 분석', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '실제 데이터를 통한 분석', 2);
    -- product-manager
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'product-manager');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Product Manager란?', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '서비스 기획이란?', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '기획자는 어떤 일을 할까', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '웹 기획', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '웹 기획 종류와 UI 특징 알아보기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '웹 기획 실습', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '앱 기획', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'iOS와 AOS의 UI/UX 특징 알아보기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '앱 기획 실습', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, 6, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '유저 중심의 서비스 기획하기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '서비스 기획의 원리 학습하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '타겟 유저 설정 방법 배우기', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '페르소나 정립하기', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '설문조사 및 질문 방법', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 7, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '타겟 고객의 이해', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '설문조사 데이터 활용', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '페르소나 추가 작성', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '유저의 Flowchart 작성', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '서비스 기획의 4대 요소 정리', 3);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '비즈니스란 무엇일까?', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '비즈니스의 이해 및 비즈니스 모델 제작', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '성과 측정 지표 작성', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사업기획안 작성', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '기획의 기초 문서 작성해보기', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'PRD 작성하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'WBS란?', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'IA란?', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '화면정의서란?', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'PPT 버전으로 작성하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Figma 버전으로 작성하기', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '기능정의서란?', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Menu Tree 작성하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '정책안 작성하기', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '기능정의서 작성하기', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, 13, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '프로토타입 제작', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Figma 활용해서 제작하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'QA & Test Case 정리', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 14, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '역기획', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '역기획이란?', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '역기획 실습', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 15, NULL, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '프로젝트 운영 방법에는 어떤 것들이 있을까', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '워터폴', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '디자인 씽킹', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '애자일', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 16, NULL, 12) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '기획 A to Z 실습', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '주제 및 니즈 파악하기', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '유저의 니즈 및 솔루션 도출하기', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '문서화 작성하기', 2);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '프로토타입 만들기', 3);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '발표 및 피드백', 4);
    -- security
    SELECT id INTO v_curriculum_id FROM curriculum WHERE track_page_id = (SELECT id FROM track_page WHERE slug = 'security');
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 1, NULL, 0) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '보안의 정의와 중요성', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '정보보안의 기본 개념(기밀성, 무결성, 가용성, 인증)', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '사회 공학 공격', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '보안 세부분야 소개', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '해킹', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '시스템해킹', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '리버싱 엔지니어링', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '로드맵(해킹/취약점 분석) 및
일정 소개', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '보안 주요 개념 소개', 3) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'OWASP Top 10', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '워게임, CTF', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '버그바운티, 제로데이', 2);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 2, NULL, 1) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '네트워크 계층', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'OSI 7 Layer', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'TCP/IP 4 Layer', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'HTTP 프로토콜', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP Request, Response', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP Method', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'HTTPS', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTTP와의 차이점 비교', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 3, NULL, 2) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '기초 프론트엔드', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'HTML, CSS, JavaScript 기초', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '개발자 도구(F12) 활용법', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '/LOGIN 페이지 제작', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 4, NULL, 3) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '암호화', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '단방향 암호화(해시 함수, 솔트)', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '대칭키와 공개키 암호화', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '패스워드 관리와 인증서의 활용', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '해시 함수를 통한 비밀번호 유효성 검사', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 5, NULL, 4) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '프록시', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '웹해킹 툴 사용', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '버프스위트의 주요 기능', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '관련 취약점 소개', 1);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 6, NULL, 5) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '세션과 쿠키 기반 인증', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '세션 하이재킹', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'JWT 기반 인증', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'JWT 취약점(토큰 변조, 탈취, 키 관리 문제)', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: JWT를 활용하여 로그인하는 프로젝트의 취약점 방어', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 7, NULL, 6) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'SOP', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SOP의 개념', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'CORS', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CORS 동작 방식', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: /CROSS-ORIGIN 페이지 제작 후 취약점 방어', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SOP, CORS', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 8, NULL, 7) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '웹해킹 분석', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Server-Side, Client-Side 취약점 분석 소개', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'XSS, CSRF, SSRF 공격 
시나리오 및 방어 기법', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'XSS', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'CSRF', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SSRF', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: /MEMO 페이지 제작 후 
취약점 방어', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'XSS, CSRF, SSRF', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 9, NULL, 8) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Injection이란?', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'SQL Injection', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SQL Injection 유형 분석', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SQL 인젝션 방어 기법', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'NoSQL 인젝션 방어 기법', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: /LOGIN 페이지 DB 
연결 후 취약점 방어', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'SQL Injection', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 10, NULL, 9) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '서버 사이드 Injection 분석', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Command Injection', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Shell Injection', 1);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'XXE', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'XML External Entity', 0);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: /LOGIN, /MEMO
페이지에서 취약점 방어', 2) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Injection', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 11, NULL, 10) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '파일 경로 조작 취약점', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'LFI', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Path Traversal', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'RPO', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: /FILE 페이지 제작 후
취약점 방어', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'LFI, Path Traversal, RPO', 0);
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 12, NULL, 11) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'DevSecOps', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'DevOps', 0);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'Jenkins로 CI/CD 파이프라인 구성 실습', 1);
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, 'DevSecOps 구조', 2);
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, '실습: 기존 프로젝트의 취약점
방어 대책 적용', 1) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_week (curriculum_id, week_from, week_to, display_order) VALUES (v_curriculum_id, 13, 16, 12) RETURNING id INTO v_week_id;
    INSERT INTO curriculum_topic (week_id, title, display_order) VALUES (v_week_id, 'Project', 0) RETURNING id INTO v_topic_id;
    INSERT INTO curriculum_topic_detail (topic_id, content, display_order) VALUES (v_topic_id, '동료 프로젝트 취약점 분석 및 보완', 0);
END $$;
-- ===== activity_category / activity / activity_image =====
INSERT INTO activity_category (slug, name, display_order) VALUES
    ('event', '행사', 0),
    ('game', '게임', 1),
    ('koin', '코인', 2);

DO $$
DECLARE
    v_activity_id BIGINT;
BEGIN
    -- event / 2019
    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'event'), 2019, 5, 'BCSD Lab 컨퍼런스', '제 1회 BCSD Lab Conference를 개최하였습니다.', 0)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/a5bc.png', 0),
        (v_activity_id, 'https://static.koreatech.in/upload/a6bc.jpg', 1),
        (v_activity_id, 'https://static.koreatech.in/upload/a7bc.png', 2),
        (v_activity_id, 'https://static.koreatech.in/upload/a8bc.jpg', 3),
        (v_activity_id, 'https://static.koreatech.in/upload/a9bc.jpg', 4),
        (v_activity_id, 'https://static.koreatech.in/upload/a10bc.jpg', 5),
        (v_activity_id, 'https://static.koreatech.in/upload/a11bc.jpg', 6);

    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'event'), 2019, 1, '안드로이드 앱 출시', '코인 안드로이드 앱을 출시하였습니다.', 1)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/a20ki.png', 0),
        (v_activity_id, 'https://static.koreatech.in/upload/a21ki.png', 1);

    -- event / 2018
    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'event'), 2018, 9, 'BCSD/KAP 통합', 'BCSD와 KAP가 통합되었습니다.', 2)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/a26brbr.png', 0);

    -- game / 2019
    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'game'), 2019, 3, 'Zombie Terminator', 'Game 트랙에서 게임을 배포하였습니다.', 0)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/a12zt.png', 0),
        (v_activity_id, 'https://static.koreatech.in/upload/a13zt.png', 1);

    -- koin / 2024
    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'koin'), 2024, 12, '코인 버스 UI 개편 업데이트', '버스 노선 및 시간표 조회 화면을 개선하여 한눈에 보기 쉽게 정보 접근성을 향상.', 0)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/bus-UI-update-1.png', 0),
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/bus-UI-update-2.png', 1);

    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'koin'), 2024, 10, E'코인 시간표 UI 개편 &\n 커스텀 시간표 업데이트', '시간표 화면 UI를 전면 개편하고, 사용자가 직접 커스텀 시간표를 구성할 수 있는 기능을 추가.', 1)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/timetable-UI-update-custom-timetable-1.png', 0),
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/timetable-UI-update-custom-timetable-2.png', 1),
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/timetable-UI-update-custom-timetable-3.png', 2);

    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'koin'), 2024, 8, '코인 주변 상점 리뷰하기 업데이트', '상점 리뷰 작성 기능을 도입하여 사용자 피드백 공유와 상호 소통 활성화.', 2)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/review-nearby-store-1.png', 0),
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/review-nearby-store-2.png', 1);

    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'koin'), 2024, 6, '코인 식단 사진 보기 업데이트', '식단 메뉴에 실제 사진을 추가해 학생들이 식사를 선택하기 더 쉽게 개선.', 3)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/view-meal-photos.png', 0);

    INSERT INTO activity (category_id, year, month, title, summary, display_order)
        VALUES ((SELECT id FROM activity_category WHERE slug = 'koin'), 2024, 5, '코인 주변 상점 UI 개편 업데이트', '주변 상점 기능의 디자인을 새롭게 개선하여 더 직관적인 탐색 경험을 제공.', 4)
        RETURNING id INTO v_activity_id;
    INSERT INTO activity_image (activity_id, image_url, display_order) VALUES
        (v_activity_id, 'https://static.koreatech.in/upload/activity/2024/nearby-store-UI-update.png', 0);
END $$;
