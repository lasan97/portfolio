-- 상품 데이터 초기화
INSERT INTO products (id, name, original_price, price, description, thumbnail_image_url, category, status, created_at, updated_at)
VALUES
    (1, '프리미엄 노트북', 1499000, 1299000, '고성능 프로세서와 그래픽이 탑재된 전문가용 노트북', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/d3a06381-9ffd-446e-9094-eeb28b625088.jpeg', 'ELECTRONICS', 'ACTIVE', NOW(), NOW()),
    (2, '인체공학 사무용 의자', 429000, 349000, '장시간 앉아도 편안한 인체공학적 설계의 사무용 의자', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/fea6028c-6646-4319-b6a2-b660d5faa4a2.jpeg', 'FURNITURE', 'ACTIVE', NOW(), NOW()),
    (3, '무선 이어버드', 199000, 159000, '노이즈 캔슬링 기능이 있는 고음질 무선 이어버드', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/4b63bf76-1c8e-4ec3-96fb-61699e80d659.jpeg', 'ELECTRONICS', 'ACTIVE', NOW(), NOW()),
    (4, '스마트 워치', 329000, 279000, '건강 모니터링과 알림 기능이 있는 스마트 워치', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/0b80e79f-8a74-4994-a4c6-5bd9b0e2c559.jpeg', 'ELECTRONICS', 'ACTIVE', NOW(), NOW()),
    (5, '블루투스 스피커', 119000, 89000, '강력한 음향과 휴대성이 뛰어난 블루투스 스피커', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/205f7a10-354e-4972-b196-ab46038b43ec.jpeg', 'ELECTRONICS', 'SOLD_OUT', NOW(), NOW()),
    (6, '원목 식탁', 650000, 589000, '내추럴한 원목 소재의 6인용 식탁', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/243e1b55-e311-4b52-9383-758dfc2abc5d.jpeg', 'FURNITURE', 'ACTIVE', NOW(), NOW()),
    (7, '태블릿 PC', 529000, 459000, '슬림한 디자인의 고성능 태블릿 PC', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/ed9ab111-9d0b-4d63-b40a-51b9d38d196f.jpeg', 'ELECTRONICS', 'ACTIVE', NOW(), NOW()),
    (8, '다용도 선반', 159000, 129000, '모던한 디자인의 조립식 다용도 선반', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/3a79b7fb-a922-434c-9651-64721fd1429f.jpeg', 'FURNITURE', 'ACTIVE', NOW(), NOW()),
    (9, '로봇 청소기', 459000, 399000, '스마트 맵핑 기능이 있는 자동 로봇 청소기', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/979367ff-b245-4b92-a14e-db3b72821357.jpeg', 'ELECTRONICS', 'SOLD_OUT', NOW(), NOW()),
    (10, '게이밍 키보드', 179000, 149000, 'RGB 백라이트와 기계식 스위치의 게이밍 키보드', 'https://martin--portfolio.s3.ap-northeast-2.amazonaws.com/2025/04/20/68cf6bbd-fbe6-410d-9e93-d58becb7dbdb.jpeg', 'ELECTRONICS', 'ACTIVE', NOW(), NOW());

-- 상품 재고 데이터 초기화
INSERT INTO product_stocks (product_id, quantity, created_at, updated_at)
VALUES
    (1, 10, NOW(), NOW()),  -- 프리미엄 노트북: 10개
    (2, 15, NOW(), NOW()),  -- 인체공학 사무용 의자: 15개
    (3, 30, NOW(), NOW()),  -- 무선 이어버드: 30개
    (4, 20, NOW(), NOW()),  -- 스마트 워치: 20개
    (5, 0, NOW(), NOW()),   -- 블루투스 스피커: 0개 (품절)
    (6, 5, NOW(), NOW()),   -- 원목 식탁: 5개
    (7, 12, NOW(), NOW()),  -- 태블릿 PC: 12개
    (8, 25, NOW(), NOW()),  -- 다용도 선반: 25개
    (9, 0, NOW(), NOW()),   -- 로봇 청소기: 0개 (품절)
    (10, 18, NOW(), NOW()); -- 게이밍 키보드: 18개
