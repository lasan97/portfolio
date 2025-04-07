-- 초기 프로젝트 데이터 삽입
INSERT INTO project (title, description, image_url, project_url, github_url, technologies) 
VALUES 
('포트폴리오 웹사이트', '개인 포트폴리오를 전시하기 위한 웹사이트입니다. Spring Boot와 Vue.js를 사용하여 개발했습니다.', 
 'https://example.com/portfolio.jpg', 'https://portfolio.example.com', 'https://github.com/username/portfolio', 
 'Spring Boot, Vue.js, JPA, H2, Tailwind CSS'),
 
('쇼핑몰 앱', '온라인 쇼핑몰 어플리케이션입니다. 사용자 인증, 상품 탐색, 장바구니 기능을 제공합니다.', 
 'https://example.com/shop.jpg', 'https://shop.example.com', 'https://github.com/username/shop', 
 'Spring Boot, React, MySQL, AWS, Docker'),
 
('블로그 플랫폼', '다양한 주제에 대한 게시물을 작성하고 공유할 수 있는 블로그 플랫폼입니다.', 
 'https://example.com/blog.jpg', 'https://blog.example.com', 'https://github.com/username/blog', 
 'Django, PostgreSQL, Bootstrap, Heroku');
