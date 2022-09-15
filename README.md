# Txai
编译相关
1. java openJdk 11
2. com.android.tools.build:gradle:7.1.2
3. 

模块
Common: 基础工具组件，
CommonView: 基础 UI 和res 
TxaiSdk: App 基础功能， 基础登录组件， 网络封装等
CommonBusiness:  暂时通用，尚不足作为一个单组组件的公共业务
MapBox: mapbox封装（）
MapSdk: 隔离地图， 用于后续扩展
DataBase: 所有存储数据的库， 通过包名区分模块

3月19号 Demo
业务功能
1. 登录登出
2. 打车流程（地图功能需要参考Mapbox api)
    1）地图定点
    2）路线绘制
    3）导航
3. 支付
4. 个人资料
5. 设置界面
6. APK 升级

技术功能
1. UI 组件
    1）Theme & Style， Dimen 定义（颜色字体）
    2) 单项标题栏样式
    3) 双项标题栏样式
    4) Dialog弹窗（根据UI 封装）
    5) 按钮格式
    6) 通知栏样式  -- 优先级低
2. 长连接 暂定Netty实现
3. 三方PUSH（申请Token）
4. 多线程调度
5. api请求框架
6. 数据库缓存/存储 （GreenDao）
7. UID -> 临时ID
8. Firebase

    

