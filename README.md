# PotatoCloudDrive
“土豆云”，一个简单的java网盘程序
# 简介
业余时间开发的一个java网盘程序，后端采用Spring MVC框架编写，接口尽量使用仿RESTful风格设计，实现了前后端分离；
由于本程序开发目标是作为一个私有网盘搭建，实现个人或小组的文件管理，访问量和并发量实际不大，为了使用方便因而使用了sqlite数据库；
前端采用Bootstrap+Angular以及一些其他组件编写。
# 功能
- 文件的上传和下载
- 新建文件、文件夹
- 移动、重命名、删除、分享文件或文件夹
- MP4及FLV格式的文件在线播放
- 图片格式文件预览
- 支持http/https协议的文件链接离线下载  

# 开发及运行环境
OpenJDK 1.8 或 Oracle JDK 1.8及以上  
Apache Tomcat 7及以上  
# 配置
修改/src/main/config.properties文件
（如果是war文件，请先解压文件之后，再修改项目目录下的WEB-INF/classes/config.properties文件）
```
#数据库地址，文件需要以.db结尾，若文件不存在则自动新建
database=/usr/local/pcd.db
#用户列表，以“,”分割，例如下面的配置就分配了admin和test两个用户
userlist=admin,test
#配置用户的密码和所在目录，配置以用户的用户名开头，_password和_directory分别配置用户的密码和用户目录(linux下注意目录权限)
admin_password=123456
admin_directory=/admin
test_password=123456
test_directory=/test
```
默认运行日志输出到/var/log/pcd.log文件中，如需变更可自行修改log4j2.xml文件
# 已知问题
- linux环境下，有相当大几率，上传文件不成功，上传进度条会回溯，文件未保存至上传目录，暂不清楚是否是由于服务器内存过小（512M）或是使用了OpenJDK导致
# 后续
- 完善访问分享接口和分享页面
- 支持更多的链接离线下载，如FTP,ed2k,Magnet等（maybe）
- 支持文件在线解压（maybe）


