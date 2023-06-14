# ODN设备端口识别-服务端程序

> 国网组长飞项目，基于已完成算法实现移动应用。


本仓库是服务端程序，包括一个基于Netty的网关Springboot应用和一个算法部署的Python应用。

网关负责与客户端通信，与算法通信，以及没有做的日志功能。

Python文件大部分是算法同学陈奥威对接时写的，此仓库仅实现一个tcp server（server.py）负责和Springboot网关通信以及在conda环境下调用python算法。

## 目录结构
```
com
    └─dian
        └─sixggroup
            │  SixggroupApplication.java
            │  
            ├─common
            │      Packet.java
            │      
            ├─config
            │      WebMvcConfig.java
            │      WebSocketConfig.java
            │      
            ├─contoller
            │      BaseController.java
            │      
            ├─netty
            │      ChannelInboundHandler.java
            │      DataBusConstant.java
            │      NettyChannelPoolHandler.java
            │      NettyClientPool.java
            │      
            ├─server
            │      JsonEncoder.java
            │      WebSocketServer.java
            │      
            ├─service
            │      BaseService.java
            │      
            └─util
                    SocketClient.java
                    Upload.java
```

## Getting Started

### 算法部署

算法部署细节请咨询国网组长飞项目组员，目前部署在团队服务器192.168.0.75的/mnt/data01/home/caw/public/DemoServer/目录下。

#### 切换至部署目录
```sh
cd /mnt/data01/home/caw/public/DemoServer/
```
#### 切换conda环境
```sh
conda activate openmmlab
```
#### 运行tcp server
```sh
python server.py
```

### Springboot应用部署

#### 打包

使用Maven进行编译打包为jar格式。

#### 部署

复制jar包到服务器终端运行，运行端口设置为8709，可在src\main\resources\application.yml中修改。

```sh
java -jar jar包名称
```