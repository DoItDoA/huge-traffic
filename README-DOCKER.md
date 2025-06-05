# Docker를 이용한 마이크로서비스 실행 가이드 (AWS 환경)

## 개요
이 프로젝트는 6개의 마이크로서비스로 구성되어 있습니다:
- **article-service** (포트: 9000) - 게시글 관리
- **comment-service** (포트: 9001) - 댓글 관리
- **like-service** (포트: 9002) - 좋아요 관리
- **view-service** (포트: 9003) - 조회수 관리
- **hot-article-service** (포트: 9004) - 인기 게시글 관리
- **article-read-service** (포트: 9005) - 게시글 읽기 (종합 서비스)

## AWS 인프라 요구사항
이 설정은 다음 AWS 관리형 서비스들을 사용합니다:
- **AWS RDS (MySQL)** - 데이터베이스
- **AWS ElastiCache (Redis)** - 캐시
- **AWS MSK (Kafka)** - 메시지 큐

Spring Boot 서비스들만 Docker 컨테이너로 실행됩니다.

## 실행 방법

### 1. AWS 인프라 준비
먼저 다음 AWS 서비스들을 설정해야 합니다:

#### RDS (MySQL) 설정
```bash
# RDS MySQL 인스턴스 생성
# 엔드포인트: your-rds-endpoint.region.rds.amazonaws.com
# 포트: 3306
# 마스터 사용자명: admin
# 데이터베이스: article, comment, article_like, article_view
```

#### ElastiCache (Redis) 설정
```bash
# ElastiCache Redis 클러스터 생성
# 엔드포인트: your-elasticache-endpoint.cache.amazonaws.com
# 포트: 6379
```

#### MSK (Kafka) 설정
```bash
# MSK 클러스터 생성
# 부트스트랩 서버: your-msk-cluster.region.amazonaws.com:9092
```

### 2. 환경변수 설정
```bash
# env.example 파일을 복사하여 .env 파일 생성
cp env.example .env

# .env 파일에서 실제 AWS 엔드포인트 정보로 수정
# DB_HOST=your-actual-rds-endpoint.region.rds.amazonaws.com
# REDIS_HOST=your-actual-elasticache-endpoint.cache.amazonaws.com
# KAFKA_BOOTSTRAP_SERVERS=your-actual-msk-cluster.region.amazonaws.com:9092
```

### 3. Docker Compose로 Spring 서비스들 실행
```bash
# 모든 Spring 서비스 빌드 및 실행
docker-compose up --build

# 백그라운드에서 실행
docker-compose up --build -d
```

### 4. 개별 서비스 실행
```bash
# 특정 서비스만 실행 (예: article-service)
docker-compose up article-service

# 여러 서비스 동시 실행
docker-compose up article-service comment-service like-service
```

### 5. 로그 확인
```bash
# 전체 로그
docker-compose logs

# 특정 서비스 로그
docker-compose logs article-service

# 실시간 로그 확인
docker-compose logs -f
```

### 6. 시스템 종료
```bash
# 컨테이너 중지
docker-compose down

# 이미지까지 삭제
docker-compose down --rmi all
```

## 환경변수 설명

| 변수명 | 예시값 | 설명 |
|--------|--------|------|
| DB_HOST | your-rds-endpoint.region.rds.amazonaws.com | AWS RDS 엔드포인트 |
| DB_PORT | 3306 | MySQL 포트 |
| DB_USERNAME | admin | RDS 마스터 사용자명 |
| DB_PASSWORD | your-db-password | RDS 비밀번호 |
| DB_NAME_ARTICLE | article | Article 서비스 DB명 |
| DB_NAME_COMMENT | comment | Comment 서비스 DB명 |
| DB_NAME_LIKE | article_like | Like 서비스 DB명 |
| DB_NAME_VIEW | article_view | View 서비스 DB명 |
| REDIS_HOST | your-elasticache-endpoint.cache.amazonaws.com | ElastiCache 엔드포인트 |
| REDIS_PORT | 6379 | Redis 포트 |
| KAFKA_BOOTSTRAP_SERVERS | your-msk-cluster.region.amazonaws.com:9092 | MSK 브로커 주소 |

## 접속 정보

### 서비스 엔드포인트
- Article Service: http://localhost:9000
- Comment Service: http://localhost:9001
- Like Service: http://localhost:9002
- View Service: http://localhost:9003
- Hot Article Service: http://localhost:9004
- Article Read Service: http://localhost:9005

## AWS 보안 그룹 설정

### RDS 보안 그룹
- 인바운드 규칙: MySQL/Aurora (3306) - Docker 컨테이너가 실행되는 서버의 IP 또는 보안 그룹

### ElastiCache 보안 그룹
- 인바운드 규칙: Custom TCP (6379) - Docker 컨테이너가 실행되는 서버의 IP 또는 보안 그룹

### MSK 보안 그룹
- 인바운드 규칙: Custom TCP (9092) - Docker 컨테이너가 실행되는 서버의 IP 또는 보안 그룹

## 트러블슈팅

### 1. AWS 서비스 연결 오류
```bash
# 네트워크 연결 확인
telnet your-rds-endpoint.region.rds.amazonaws.com 3306
telnet your-elasticache-endpoint.cache.amazonaws.com 6379

# 보안 그룹 설정 확인
# - RDS, ElastiCache, MSK의 보안 그룹에 적절한 인바운드 규칙이 있는지 확인
```

### 2. 환경변수 확인
```bash
# Docker 컨테이너 내부의 환경변수 확인
docker exec article-service env | grep -E "(DB_|REDIS_|KAFKA_)"
```

### 3. 데이터베이스 연결 테스트
```bash
# MySQL 클라이언트로 직접 연결 테스트
mysql -h your-rds-endpoint.region.rds.amazonaws.com -u admin -p
```

### 4. 빌드 오류
```bash
# 캐시 없이 다시 빌드
docker-compose build --no-cache

# 특정 서비스만 다시 빌드
docker-compose build article-service
```

## 비용 최적화 팁

### 1. RDS
- 개발 환경에서는 `db.t3.micro` 인스턴스 사용
- 운영 환경에서는 Multi-AZ 배포 고려

### 2. ElastiCache
- 개발 환경에서는 `cache.t3.micro` 노드 사용
- 클러스터 모드 비활성화로 시작

### 3. MSK
- 개발 환경에서는 최소 브로커 수(3개) 사용
- `kafka.t3.small` 인스턴스 타입 사용

## 모니터링

### CloudWatch 메트릭 확인
- RDS: CPU 사용률, 연결 수, IOPS
- ElastiCache: CPU 사용률, 메모리 사용률, 캐시 히트율
- MSK: 브로커 CPU, 네트워크 처리량

### 애플리케이션 로그
```bash
# 실시간 로그 모니터링
docker-compose logs -f --tail=100
``` 