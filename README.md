# meetingroom

# 서비스 시나리오
## 기능적 요구사항
1. 기본적으로 선착순으로 당일 사용할 회의실 예약을 선점하도록 혹은 8시 ~ 9시 50분에 대한 회의실 예약은 8시에, 10시 ~ 11시 50분에 대한 회의실 예약은 10시에(그 뒷 시간도 일정한 시간에) 진행하는 선점 회의실 예약 시나리오를 작성했다.
2. 해당 회의실이 예약 중이거나 회의 중이라면 예약을 할 수 없고, 예약 취소나 회의 종료가 일어났을 때 예약을 할 수 있도록 생각했다.(티켓팅 처럼) 
3. 회원이 회의실에 대한 예약을 한다.
4. 회원이 예약한 회의실에 대해 회의 시작을 요청한다.
5. 예약한 사람이면 해당 회의를 시작할 수 있도록 한다.
6. 예약한 사람이 아니면 회의를 시작할 수 없다.
7. 예약한 회의에 대해 예약 취소를 요청할 수 있다.
8. 시작했던 회의를 종료한다.
9. 종료한 사람이 해당 회의에 대한 리뷰를 작성한다.(premium)
10. 리뷰가 작성되면, 회의시작이 되었던 예약내역의 예약 상태가 'Ended'로 변경된다.(premium)
11. Schedule view에서 회의실에 따른 예약정보와 가장 최근에 등록된 리뷰를 볼 수 있다.(premium)

## 비기능적 요구사항
1. 트랜잭션
  - 회의 시작을 요청할 때, 회의를 예약한 사람이 아니라면 회의를 시작하지 못하게 한다.(Sync 호출)
  - 리뷰를 등록할 때, 실제로 존재하는 회의실인지 확인을 한다.(premium)(Sync 호출)
2. 장애격리
  - 회의실 시스템이 수행되지 않더라도 예약취소, 사용자 확인, 회의 종료는 365일 24시간 받을 수 있어야 한다. (Async 호출)
  - 리뷰서비스가 실행 중이 아니더라도, 회의실 서비스에서 해당 회의실의 삭제는 언제든지 가능해야한다. (premium) (Async 호출)
  - 예약서비스가 실행 중이 아니더라도, 회의실 리뷰가 등록은 언제든지 가능해야한다. (premium) (Async 호출)
3. 성능
  - 회의실 현황에 대해 예약 상황 및 가장 최근 리뷰를 별도로 확인할 수 있어야 한다.(CQRS)

# 체크포인트
https://workflowy.com/s/assessment/qJn45fBdVZn4atl3

## EventStorming 결과
### 완성된 1차 모형(기존)
<img width="1086" alt="스크린샷 2021-02-25 오후 10 59 46" src="https://user-images.githubusercontent.com/43164924/109471758-8fc9c880-7ab4-11eb-8855-5e2e7d31328b.png">

### 완성된 프리미엄 모형
<img width="1111" alt="스크린샷 2021-03-04 오후 1 00 53" src="https://user-images.githubusercontent.com/43164924/109909761-fdab0580-7ce9-11eb-936d-cf84c8951bee.png">


### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증
<img width="1111" alt="스크린샷 2021-03-04 오후 1 00 53 복사본" src="https://user-images.githubusercontent.com/43164924/109909726-ef5ce980-7ce9-11eb-8a2b-0b87ea9be408.png">

    
    - 회의실이 등록이 된다. (7)
    - 회원이 회의실을 예약을 한다. (3 -> 6)
    - 회원이 예약한 회의실에 대해 회의 시작을 요청한다.
      - 예약한 사람이면 회의를 시작한다. (1 -> 5 -> 6)
      - 예약한 사람이 아니면 회의 시작을 못한다. (1)
    - 회원이 예약한 회의실을 예약 취소한다. (4 -> 6)
    - 시작했던 회의를 종료한다. (2 -> 6)
    - schedule 메뉴에서 회의실에 대한 예약 정보를 알 수 있다.(Room Service + Reserve Service + Reivew Service) (8)
    - 회원이 회의가 끝나고 리뷰를 등록할 때, 해당 회의실이 존재하는 회의실인지 확인한다.(10)
    - 회의실 리뷰가 성공적으로 등록이 되었다면, 예약 서비스에서 해당 예약 내역(서비스가 시작하고 종료되었던)의 상태를 'Ended'로 변경.(11 -> 12)

### 헥사고날 아키텍쳐 다이어그램 도출 (Polyglot)
<img width="1374" alt="스크린샷 2021-03-04 오전 10 09 40" src="https://user-images.githubusercontent.com/43164924/109894988-d0eaf400-7cd1-11eb-9742-75abfbdc7fc1.png">


# 구현
도출해낸 헥사고날 아키텍처에 맞게, 로컬에서 SpringBoot를 이용해 Maven 빌드 하였다. 각각의 포트넘버는 8081 ~ 8084, 8088 이다.

    cd conference
    mvn spring-boot:run

    cd reserve
    mvn spring-boot:run
    
    cd schedule
    mvn spring-boot:run
    
    cd room
    mvn spring-boot:run
    
    cd review
    mvn spring-boot:run
    
    cd gateway
    mvn spring-boot:run
  
## DDD의 적용
**Review 서비스의 Review.java**

```java
package meetingroom;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Review_table")
public class Review {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long roomId;
    private String userId;
    private Integer score;
    private String comment;
    private Long reserveId;

    @PrePersist
    public void onPrePersist(){
        RoomChecked roomChecked = new RoomChecked();
        BeanUtils.copyProperties(this, roomChecked);
        roomChecked.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        meetingroom.external.Room room = new meetingroom.external.Room();
        room.setId(roomId);
        // mappings goes here
        String result=ReviewApplication.applicationContext.getBean(meetingroom.external.RoomService.class)
            .search(room);
        if(result.equals("valid")){
            System.out.println("Success!");
        }
        else{
            System.out.println("FAIL!! There is no room!!");
            Exception ex = new Exception();
            ex.notify();
        }
    }
    @PostPersist
    public void onPostPersist(){
        Registered registered = new Registered();
        BeanUtils.copyProperties(this, registered);
        registered.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getReserveId() {
        return reserveId;
    }

    public void setReserveId(Long reserveId) {
        this.reserveId = reserveId;
    }
}
```

**Review 서비스의 PolicyHandler.java**
```java
package meetingroom;

import meetingroom.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PolicyHandler{
    @Autowired
    ReviewRepository reviewRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    @Transactional
    public void wheneverEnded_UpdateConference(@Payload Deleted deleted){

        if(deleted.isMe()){
            List<Review> reviewList = reviewRepository.findAllByRoomId(deleted.getId());
            System.out.println("##### listener  : " + deleted.toJson());
            for(Review review:reviewList){
                reviewRepository.deleteById(review.getId());
            }
        }
    }

}
```


- 적용 후 REST API의 테스트를 통해 정상적으로 작동함을 알 수 있었다.
- 회의실 등록(Added)후 리뷰 등록(Registered) 결과
<img width="850" alt="스크린샷 2021-03-04 오전 10 21 07" src="https://user-images.githubusercontent.com/43164924/109895867-57ec9c00-7cd3-11eb-8f8b-18fab4dd7648.png">

- 회의실 삭제(Deleted) 후 결과 : 해당 회의실에 대한 리뷰도 삭제됨
<img width="850" alt="스크린샷 2021-03-04 오전 10 23 26" src="https://user-images.githubusercontent.com/43164924/109896077-ab5eea00-7cd3-11eb-8936-3881c4937943.png">

## Gateway 적용
API Gateway를 통해 마이크로 서비스들의 진입점을 하나로 진행하였다.
```yml
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: conference
          uri: http://localhost:8081
          predicates:
            - Path=/conferences/** 
        - id: reserve
          uri: http://localhost:8082
          predicates:
            - Path=/reserves/** 
        - id: room
          uri: http://localhost:8083
          predicates:
            - Path=/rooms/** 
        - id: schedule
          uri: http://localhost:8084
          predicates:
            - Path= /reserveTables/**
        - id: review
          uri: http://localhost:8085
          predicates:
            - Path=/reviews/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: conference
          uri: http://conference:8080
          predicates:
            - Path=/conferences/** 
        - id: reserve
          uri: http://reserve:8080
          predicates:
            - Path=/reserves/** 
        - id: room
          uri: http://room:8080
          predicates:
            - Path=/rooms/** 
        - id: schedule
          uri: http://schedule:8080
          predicates:
            - Path= /reserveTables/**
        - id: review
          uri: http://review:8080
          predicates:
            - Path=/reviews/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080

```

## Polyglot Persistence
- Review 서비스의 경우, 다른 서비스들이 h2 저장소를 이용한 것과는 다르게 hsql을 이용하였다. 
- 이 작업을 통해 서비스들이 각각 다른 데이터베이스를 사용하더라도 전체적인 기능엔 문제가 없음을, 즉 Polyglot Persistence를 충족하였다.

<img width="324" alt="스크린샷 2021-03-04 오전 10 26 35" src="https://user-images.githubusercontent.com/43164924/109896341-1c9e9d00-7cd4-11eb-8f93-98daccec6d53.png">

## 동기식 호출(Req/Res 방식)과 Fallback 처리

- review 서비스의 external/RoomService.java 내에 리뷰를 작성하려는 회의실이 실제로 존재하는지 확인하는 Service 대행 인터페이스(Proxy)를 FeignClient를 이용하여 구현하였다.

```java
@FeignClient(name="room", url="${api.room.url}")
public interface RoomService {

    @RequestMapping(method= RequestMethod.GET, path="/rooms/check")
    public String search(@RequestBody Room room);

}
```
- review 서비스의 Review.java 내에 회의실 존재 확인 후 결과에 따라 리뷰를 등록할지,등록하지 않을지 결정.(@PrePersist)
```java
@PrePersist
    public void onPrePersist(){
        RoomChecked roomChecked = new RoomChecked();
        BeanUtils.copyProperties(this, roomChecked);
        roomChecked.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        meetingroom.external.Room room = new meetingroom.external.Room();
        room.setId(roomId);
        // mappings goes here
        String result=ReviewApplication.applicationContext.getBean(meetingroom.external.RoomService.class)
            .search(room);
        if(result.equals("valid")){
            System.out.println("Success!");
        }
        else{
            System.out.println("FAIL!! There is no room!!");
            Exception ex = new Exception();
            ex.notify();
        }
    }
```
- 동기식 호출에서는 호출 시간에 따른 커플링이 발생하여, Room 시스템에 장애가 나면 리뷰 등록을 할 수가 없다. (Room 서비스에서 실제로 회의실이 존재하는지를 확인하므로)
  - Room 서비스를 중지.<img width="334" alt="스크린샷 2021-03-04 오전 10 31 30" src="https://user-images.githubusercontent.com/43164924/109896733-cc740a80-7cd4-11eb-9855-9e9df5b868b3.png">
  - Review 서비스에서 리뷰 등록 시 에러 발생. <img width="850" alt="스크린샷 2021-03-04 오전 10 32 18" src="https://user-images.githubusercontent.com/43164924/109896791-e877ac00-7cd4-11eb-97c9-00a026eadd20.png">
  - Room 서비스 재기동 후 다시 리뷰 등록 요청. <img width="850" alt="스크린샷 2021-03-04 오전 10 33 43" src="https://user-images.githubusercontent.com/43164924/109896887-1b21a480-7cd5-11eb-8298-67424954d605.png">


## 비동기식 호출 (Pub/Sub 방식)

- review 서비스 내 Review.java에서 아래와 같이 서비스 Pub 구현

```java
@Entity
@Table(name="Review_table")
public class Review {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long roomId;
    private String userId;
    private Integer score;
    private String comment;
    private Long reserveId;

    //...
    @PostPersist
    public void onPostPersist(){
        Registered registered = new Registered();
        BeanUtils.copyProperties(this, registered);
        registered.publishAfterCommit();
    }
    //...
}

```

- reserve 서비스 내 PolicyHandler.java 에서 아래와 같이 Sub 구현

```java
@Service
public class PolicyHandler{
    @Autowired
    ReserveRepository reserveRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }
    @StreamListener(KafkaProcessor.INPUT)
    @Transactional
    public void wheneverEnded_UpdateConference(@Payload Registered registered){

        if(registered.isMe()){
            System.out.println("##### listener  : " + registered.toJson());
            Optional<Reserve> reserve=reserveRepository.findById(registered.getReserveId());
            if(reserve.isPresent()){
                reserve.get().setStatus("Ended");
                reserveRepository.save(reserve.get());
            }
        }
    }
}
```
- 비동기 호출은 다른 서비스 하나가 비정상이어도 해당 메세지를 다른 메시지 큐에서 보관하고 있기에, 서비스가 다시 정상으로 돌아오게 되면 그 메시지를 처리하게 된다.
  - reserve 서비스와 review 서비스가 둘 다 정상 작동을 하고 있을 경우, 이상이 없이 잘 된다. <img width="850" alt="스크린샷 2021-03-04 오후 1 11 17" src="https://user-images.githubusercontent.com/43164924/109910321-326b8c80-7ceb-11eb-8b5c-167f51dd573f.png">
  - reserve 서비스를 내렸다. <img width="318" alt="스크린샷 2021-03-04 오전 10 58 09" src="https://user-images.githubusercontent.com/43164924/109899012-86b94100-7cd8-11eb-98f2-16af16b6cf65.png">
  - review 서비스를 이용해 리뷰 등록을 하여도 문제가 없이 동작한다. <img width="850" alt="스크린샷 2021-03-04 오전 10 58 44" src="https://user-images.githubusercontent.com/43164924/109899063-99cc1100-7cd8-11eb-9f2b-85a5583a4ddb.png">

## CQRS

viewer인 schedule 서비스를 별도로 구현하여 아래와 같이 view를 출력한다.
- Added 수행 후 schedule (회의실 등록)
<img width="850" alt="스크린샷 2021-03-04 오전 11 14 38" src="https://user-images.githubusercontent.com/43164924/109900492-d13bbd00-7cda-11eb-91e7-a9f3bf314050.png">
- Reserved 수행 후 schedule (예약 진행)
<img width="850" alt="스크린샷 2021-03-04 오전 11 15 14" src="https://user-images.githubusercontent.com/43164924/109900550-e7497d80-7cda-11eb-9275-cf9a051afce2.png">
- Ended 수행 후 schedule (회의 시작 후, 회의 종료)
<img width="850" alt="스크린샷 2021-03-04 오전 11 32 31" src="https://user-images.githubusercontent.com/43164924/109902114-50ca8b80-7cdd-11eb-89cf-2d3c10391bf4.png">
- Registered 수행 후 schedule (리뷰 등록)(premium)
<img width="850" alt="스크린샷 2021-03-04 오후 1 22 28" src="https://user-images.githubusercontent.com/43164924/109911030-ace8dc00-7cec-11eb-89d2-e0221fdc3da2.png">
- 다시 Reserved 수행 후 schedule (예약 진행)
<img width="850" alt="스크린샷 2021-03-04 오후 1 23 24" src="https://user-images.githubusercontent.com/43164924/109911105-d0ac2200-7cec-11eb-9ff2-61882995f44a.png">
- Canceled 수행 후 schedule (예약 취소)
<img width="850" alt="스크린샷 2021-03-04 오후 1 24 20" src="https://user-images.githubusercontent.com/43164924/109911180-efaab400-7cec-11eb-86aa-98265cc917d2.png">
- Deleted 수행 후 schedule (회의실 삭제)
<img width="850" alt="스크린샷 2021-03-04 오후 1 18 01" src="https://user-images.githubusercontent.com/43164924/109910694-0ef51180-7cec-11eb-9aab-a38515e982b4.png">

# 운영
## CI/CD 설정
- git에서 소스 가져오기
```
https://github.com/dngur6344/meetingroom
```
- Build 하기
```
cd /meetingroom
cd conference
mvn package

cd ..
cd gateway
mvn package

cd ..
cd reserve
mvn package

cd ..
cd room
mvn package

cd ..
cd schedule
mvn package
```
- Dockerlizing, ACR(Azure Container Registry에 Docker Image Push하기
```
cd /meetingroom
cd rental
az acr build --registry meetingroomacr --image meetingroomacr.azurecr.io/conference:latest .

cd ..
cd gateway
az acr build --registry meetingroomacr --image meetingroomacr.azurecr.io/gateway:latest .

cd ..
cd reserve
az acr build --registry meetingroomacr --image meetingroomacr.azurecr.io/reserve:latest .

cd ..
cd room
az acr build --registry meetingroomacr --image meetingroomacr.azurecr.io/room:latest .

cd ..
cd schedule
az acr build --registry meetingroomacr --image meetingroomacr.azurecr.io/schedule:latest .
```

- ACR에서 이미지 가져와서 Kubernetes에서 Deploy하기

```
kubectl create deploy gateway --image=meetingroomacr.azurecr.io/gateway:latest
kubectl create deploy conference --image=meetingroomacr.azurecr.io/conference:latest
kubectl create deploy reserve --image=meetingroomacr.azurecr.io/reserve:latest
kubectl create deploy room --image=meetingroomacr.azurecr.io/room:latest
kubectl create deploy schedule --image=meetingroomacr.azurecr.io/schedule:latest
kubectl get all
```

- Kubectl Deploy 결과 확인  

  <img width="556" alt="스크린샷 2021-02-28 오후 12 47 12" src="https://user-images.githubusercontent.com/33116855/109407331-52394280-79c3-11eb-8283-ba98b2899f69.png">

- Kubernetes에서 서비스 생성하기 (Docker 생성이기에 Port는 8080이며, Gateway는 LoadBalancer로 생성)

```
kubectl expose deploy conference --type="ClusterIP" --port=8080
kubectl expose deploy reserve --type="ClusterIP" --port=8080
kubectl expose deploy room --type="ClusterIP" --port=8080
kubectl expose deploy schedule --type="ClusterIP" --port=8080
kubectl expose deploy gateway --type="LoadBalancer" --port=8080
kubectl get all
```

- Kubectl Expose 결과 확인  

  <img width="646" alt="스크린샷 2021-02-28 오후 12 47 50" src="https://user-images.githubusercontent.com/33116855/109407339-5feec800-79c3-11eb-9f3f-18d9d2b812f0.png">


  
## 무정지 재배포
- 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함
- siege 로 배포작업 직전에 워크로드를 모니터링 함
```
siege -c10 -t60S -r10 -v --content-type "application/json" 'http://52.231.13.109:8080/reserves POST {"userId":1, "roomId":"3"}'
```
- Readiness가 설정되지 않은 yml 파일로 배포 진행  

  <img width="871" alt="스크린샷 2021-02-28 오후 1 52 52" src="https://user-images.githubusercontent.com/33116855/109408363-4b62fd80-79cc-11eb-9014-638a09b545c1.png">

```
kubectl apply -f deployment.yml
```

- 아래 그림과 같이, Kubernetes가 준비가 되지 않은 delivery pod에 요청을 보내서 siege의 Availability 가 100% 미만으로 떨어짐 

  <img width="480" alt="스크린샷 2021-02-28 오후 2 30 37" src="https://user-images.githubusercontent.com/33116855/109408933-97fd0780-79d1-11eb-8ec6-f17d44161eb5.png">

- Readiness가 설정된 yml 파일로 배포 진행  

  <img width="779" alt="스크린샷 2021-02-28 오후 2 32 51" src="https://user-images.githubusercontent.com/33116855/109408971-e4484780-79d1-11eb-8989-cd680e962eff.png">

```
kubectl apply -f deployment.yml
```
- 배포 중 pod가 2개가 뜨고, 새롭게 띄운 pod가 준비될 때까지, 기존 pod가 유지됨을 확인  
  
  <img width="764" alt="스크린샷 2021-02-28 오후 2 34 54" src="https://user-images.githubusercontent.com/33116855/109408992-2b363d00-79d2-11eb-8024-07aeade9e928.png">
  
- siege 가 중단되지 않고, Availability가 높아졌음을 확인하여 무정지 재배포가 됨을 확인함  
  
  <img width="507" alt="스크린샷 2021-02-28 오후 2 48 28" src="https://user-images.githubusercontent.com/33116855/109409209-093dba00-79d4-11eb-9793-d1a7cdbe55f0.png">


## 오토스케일 아웃
- 서킷 브레이커는 시스템을 안정되게 운영할 수 있게 해줬지만, 사용자의 요청이 급증하는 경우, 오토스케일 아웃이 필요하다.

  - 단, 부하가 제대로 걸리기 위해서, reserve 서비스의 리소스를 줄여서 재배포한다.

  <img width="703" alt="스크린샷 2021-02-28 오후 2 51 19" src="https://user-images.githubusercontent.com/33116855/109409248-7d785d80-79d4-11eb-95ce-4af79b9a7e72.png">

- reserve 시스템에 replica를 자동으로 늘려줄 수 있도록 HPA를 설정한다. 설정은 CPU 사용량이 15%를 넘어서면 replica를 10개까지 늘려준다.

```
kubectl autoscale deploy reserve --min=1 --max=10 --cpu-percent=15
```

- hpa 설정 확인  

  <img width="631" alt="스크린샷 2021-02-28 오후 2 56 50" src="https://user-images.githubusercontent.com/33116855/109409360-6a19c200-79d5-11eb-90a4-fc5c5030e92b.png">

- hpa 상세 설정 확인  

  <img width="1327" alt="스크린샷 2021-02-28 오후 2 57 37" src="https://user-images.githubusercontent.com/33116855/109409362-6ede7600-79d5-11eb-85ec-85c59bdefcaf.png>
  <img width="691" alt="스크린샷 2021-02-28 오후 2 57 53" src="https://user-images.githubusercontent.com/33116855/109409364-700fa300-79d5-11eb-8077-70d5cddf7505.png">

  
- siege를 활용해서 워크로드를 2분간 걸어준다. (Cloud 내 siege pod에서 부하줄 것)
```
kubectl exec -it (siege POD 이름) -- /bin/bash
siege -c1000 -t120S -r100 -v --content-type "application/json" 'http://20.194.45.67:8080/reserves POST {"userId":1, "roomId":"3"}'
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다.
```
watch kubectl get all
```
- 스케일 아웃이 자동으로 되었음을 확인

  <img width="656" alt="스크린샷 2021-02-28 오후 3 01 47" src="https://user-images.githubusercontent.com/33116855/109409423-eb715480-79d5-11eb-8b2c-0a0417df9718.png">

- 오토스케일링에 따라 Siege 성공률이 높은 것을 확인 할 수 있다.  

  <img width="412" alt="스크린샷 2021-02-28 오후 3 03 18" src="https://user-images.githubusercontent.com/33116855/109409445-18be0280-79d6-11eb-9c6f-4632f8a88d1d.png">

## Self-healing (Liveness Probe)
- reserve 서비스의 yml 파일에 liveness probe 설정을 바꾸어서, liveness probe 가 동작함을 확인

- liveness probe 옵션을 추가하되, 서비스 포트가 아닌 8090으로 설정, readiness probe 미적용
```
        livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8090
            initialDelaySeconds: 5
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

- reserve 서비스에 liveness가 적용된 것을 확인  

  <img width="824" alt="스크린샷 2021-02-28 오후 3 31 53" src="https://user-images.githubusercontent.com/33116855/109409951-1bbaf200-79da-11eb-9a39-a585224c3ca0.png">

- reserve 서비스에 liveness가 발동되었고, 8090 포트에 응답이 없기에 Restart가 발생함   

  <img width="643" alt="스크린샷 2021-02-28 오후 3 34 35" src="https://user-images.githubusercontent.com/33116855/109409994-7c4a2f00-79da-11eb-8ab7-e542e50fd929.png">


## ConfigMap 적용
- conference 서비스의 application.yaml에 ConfigMap 적용 대상 항목을 추가한다.

  <img width="576" alt="스크린샷 2021-03-02 오전 11 20 08" src="https://user-images.githubusercontent.com/33116855/109586783-424b6b00-7b49-11eb-8e1c-b1d23d7ef463.png">

- conference 서비스의 deployment.yaml에 ConfigMap 적용 대상 항목을 추가한다.


  <img width="546" alt="스크린샷 2021-03-02 오전 11 21 33" src="https://user-images.githubusercontent.com/33116855/109586890-73c43680-7b49-11eb-9622-46f9a8a45150.png">

- ConfigMap 생성하기
```
kubectl create configmap apiurl --from-literal=reserveapiurl=http://reserve:8080 --from-literal=roomapiurl=http://room:8080
```

- Configmap 생성 확인, url이 Configmap에 설정된 것처럼 잘 반영된 것을 확인할 수 있다.  

```
kubectl get configmap apiurl -o yaml
```

  <img width="640" alt="스크린샷 2021-03-02 오전 11 22 06" src="https://user-images.githubusercontent.com/33116855/109586918-86d70680-7b49-11eb-8429-145a47a13ca0.png">

- 아래 코드와 같이 Spring Boot 내에서 Configmap 환경 변수를 사용하면 정상 작동한다.

   <img width="604" alt="스크린샷 2021-03-02 오전 11 23 06" src="https://user-images.githubusercontent.com/33116855/109587003-ab32e300-7b49-11eb-8282-af5c5d2b7f42.png">


## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

- RestAPI 기반 Request/Response 요청이 과도할 경우 CB 를 통하여 장애격리 하도록 설정함.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 설정

- conference의 Application.yaml 설정
```
feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

- reserve에 Thread 지연 코드 삽입

  <img width="702" alt="스크린샷 2021-03-01 오후 2 40 46" src="https://user-images.githubusercontent.com/33116855/109456415-22aa3900-7a9c-11eb-9a30-4e63323312c2.png">

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
siege -c100 -t60S -r10 -v --content-type "application/json" 'http://52.141.56.203:8080/conferences POST {"roomId": "1", "userId":"1", reserveId:"1"}'
```
- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 reserve에서 처리되면서 다시 conference를 받기 시작 

  <img width="409" alt="스크린샷 2021-03-01 오후 2 32 14" src="https://user-images.githubusercontent.com/33116855/109455911-00fc8200-7a9b-11eb-8d95-f5df5ef249fd.png">

- CB 잘 적용됨을 확인


