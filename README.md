# distributed-systems-algorithms

Build:

```shell
./gradlew jibDockerBuild
```

Run:

```shell
ALGORITHM=<ALGORITHM_OPTION> docker compose up
```

Algorithm options: 
- LAMPORT 
- MUTEX
- ELECTION

Lamport clock diagram:

![image](https://sookocheff.com/post/time/lamport-clock/assets/process-events.gif)

Mutual exclusion diagram: 

![image](https://user-images.githubusercontent.com/32870665/203177378-e5fbe59d-a7b8-4f32-9319-7f3b99337ccb.png)

[Image source](https://www.ques10.com/p/2223/explain-centralized-algorithm-for-mutual-exclusi-1/#:~:text=%E2%98%85%2038k-,Centralized%20Algorithm,-)

Election algorithm diagram:

![image](https://i.imgur.com/rZ9AUXb.png)

