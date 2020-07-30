:: Test of environment variable length
@echo off & setlocal EnableDelayedExpansion
REM 将要添加的域名都写在这里，用;号隔开
set strp=10.10.10.225 dev.db.java110.com;10.10.10.225 api.java110.com;10.10.10.225 dev.java110.com;94.191.126.71 dev.zk.java110.com;94.191.126.71 dev.kafka.java110.com;94.191.126.71 dev.redis.java110.com;

set hostsfile="%SystemRoot%\system32\drivers\etc\hosts"


:for
for /F "delims=; tokens=1,*" %%A in ("!strp!") do (


REM 取得第一个Host
set stHosts=%%A
REM echo A = !stHosts!


REM 取得剩余的Host
set strp=%%B
REM echo B = !strp!


REM 设置插入标记true false
set ins=true


FOR /F "eol=# tokens=1 usebackq delims=" %%i in (%hostsfile%) do if "!stHosts!"=="%%i" set ins=flase
if "!ins!"=="true" echo !stHosts!>> %hostsfile%


)


REM echo B-EOF: = !strp!
REM 判断变量是否为空，不为空就循环提前。 
if not "!strp!"=="" goto :for


@echo  ########################################
echo "右击“以管理员身份运行”"
echo "如杀毒软件提示，点击允许。"
@echo  ########################################


echo   "hosts文件修改完成"
@ipconfig /flushdns
@echo   "刷新DNS完成"
echo  按任意键退出
@echo
@pause > nul
@exit