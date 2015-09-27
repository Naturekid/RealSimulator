#include <stdio.h>

#include <stdlib.h>



#include <string.h>

#include <sys/socket.h>

#include <netinet/in.h>

#include <arpa/inet.h>

#include <netdb.h>

#include <sys/time.h>



//#define DEBUG
//若记录同步过程
//#define RECORD

#define WSAECONNABORTED 10053



int out_port=6789;//发送端口

int in_port = 6788;//接收端口

int sleep_time = 8;


struct timeval tv_out;//接收超时的时间变量

#ifdef RECORD
FILE *log_file;//log文件
#endif

//计算两个时间之差
//int my_difftime(struct timeval  *t1,struct timeval  *t2);
//将从服务器端收到的时间序列拆分为本地发送时间，远程接收时间，远程发送时间
void parse(char *str,double *lt1,double *rt1,double *rt2);



int main(int argc, char** argv) {


	//如果未输入服务器IP则不执行
	if(argc<=1)
	{

		printf("Please input server IP\n");

		return 0;

	}

	printf("Try to connect Server:%s\n",argv[1]);
	
	if(argc==3)
	{
		
		sleep_time =atoi(argv[2]);
		printf("Sleep Time is %d\n",sleep_time);
	}	

#ifdef RECORD
	//打开log文件
	log_file = fopen("./log","a+");
#endif



	int socket_descriptor; //套接口描述字

	int iter=0;//计数器

    char buf[80];//缓存区

	//发送的网络地址
    struct sockaddr_in address;//处理网络通信的地址

	bzero(&address,sizeof(address));
	address.sin_family=AF_INET;//指定协议族
	address.sin_addr.s_addr=inet_addr(argv[1]);//这里不一样，指定服务器端
	address.sin_port=htons(out_port);//指定出口端口

    //创建一个 UDP socket
    socket_descriptor=socket(AF_INET,SOCK_DGRAM,0);//IPV4  SOCK_DGRAM 数据报套接字（UDP协议）
	
	char message[256];//缓存从服务器端接收到的信息

	int sin_len;
	//接收地址
	struct sockaddr_in sin;
	bzero(&sin,sizeof(sin));
	sin.sin_family=AF_INET;
	sin.sin_addr.s_addr=htonl(INADDR_ANY);
	sin.sin_port=htons(in_port);//接收地址
    sin_len=sizeof(sin);
	
	//为socket设定超时时间

	//超时时间为6s 
    tv_out.tv_sec = 2;//等待6秒
    tv_out.tv_usec = 0;

int nTimeout=1000;
	//setsockopt(socket_descriptor,SOL_SOCKET,SO_RCVTIMEO,(char *)&nTimeout, sizeof(int));//第二个参数为指定选项代码，为IPV4

	setsockopt(socket_descriptor,SOL_SOCKET,SO_RCVTIMEO,&tv_out, sizeof(struct timeval));//第二个参数为指定选项代码，为IPV4
	//绑定
    bind(socket_descriptor,(struct sockaddr *)&sin,sizeof(sin));

	
	//发送次数
    int send_max = 30;




    for(iter=0;iter<=send_max;iter++)

    {

#ifdef DEBUG
	printf("Send Query %d\n",iter);
#endif
	
	//发送时间
	struct timeval  *send_time = (struct timeval  *)malloc(sizeof(struct timeval));
	struct timeval  *recv_time = (struct timeval  *)malloc(sizeof(struct timeval));
	struct timeval  *new_time = (struct timeval  *)malloc(sizeof(struct timeval));

	//获得发送时间，精确到微秒
	if(gettimeofday(send_time,NULL)==-1)//error

	{
		free(send_time);
		continue;
	}

	else{

		//将timeval结构的时间转换为字符串发送
		unsigned int second = send_time->tv_sec;

		unsigned int usecond = send_time->tv_usec;

		double utime = (double)second+ usecond*1.0/1000000;

//		printf("%f\n",utime);

		sprintf(buf,"%f",utime);

	}

    /*int PASCAL FAR sendto( SOCKET s, const char FAR* buf, int len, int flags,const struct sockaddr FAR* to, int tolen);　　
    	* s：一个标识套接口的描述字。　
		* buf：包含待发送数据的缓冲区。
        * len：buf缓冲区中数据的长度。
        * flags：调用方式标志位。
        * to：（可选）指针，指向目的套接口的地址。
        * tolen：to所指地址的长度。  
	*/

	//发送给客户端
    sendto(socket_descriptor,buf,sizeof(buf),0,(struct sockaddr *)&address,sizeof(address));
	
	free(send_time);

	
	//监听来自服务器的消息
	int result = recvfrom(socket_descriptor,message,sizeof(message),0,(struct sockaddr *)&sin,&sin_len);
	if(result == -1){
		continue ;
	}
	
	//获得接收时间
	gettimeofday(recv_time,NULL);

	//分别为服务器的接收时间，服务器发送时间，本地发送时间
	double rt1,rt2,lt1;
	//处理为double型数据
	parse(message,&lt1,&rt1,&rt2);
	


	//将接收时间转换为double型
	double lt2 = (double)recv_time->tv_sec + recv_time->tv_usec *1.0/1000000;
//	printf("%f\n",lt2);

#ifdef DEBUG
	printf("%s\n",message);
	printf("%f %f %f %f\n",lt1,lt2,rt1,rt2);
#endif

	//计算本机和服务器的时间差
	double delay = (lt2-lt1) - (rt2-rt1);
//	printf("%f\n",delay);
	double offset = ((rt1-lt1)+(rt2-lt2))/2;

	//获得当前时间

	gettimeofday(new_time,NULL);

	double nt = (double)new_time->tv_sec + new_time->tv_usec *1.0/1000000;

	//set new time
	//调整到新时间
	nt = nt + offset;
	long i_nt = (unsigned int)nt;
	new_time->tv_sec = i_nt;//秒
	new_time->tv_usec = (nt-i_nt) * 1000000;//微秒

	//设置为新时间
	settimeofday(new_time, NULL);
	
//	gettimeofday(new_time,NULL);

//#ifdef DEBUG
//	printf("%f %f %f %f %f %f\n",offset,delay,lt1,rt1,rt2,lt2);
	//printf("%f %f %d %d\n",offset,nt,(long)new_time->tv_sec,(long)new_time->tv_usec);
//	printf("%d	new_time:%lf	diff_time:%lf\n",iter,nt,offset);
	printf("%lf\n",offset);
//#endif

#ifdef RECORD
	char log[128];
	memset(log,0,strlen(log));
	
	//sprintf("send_time:%lf	server_recv:%lf	server_send:%lf	recv_time:%lf	new_time:%lf\n",lt1,rt1,rt2,lt2,nt);
	sprintf(log,"new_time:%lf	diff_time:%lf\n",nt,offset);
	fputs(log,log_file);
	
#endif

	//释放
	free(new_time);
	free(recv_time);
	
	//每8秒运行一次
	sleep(sleep_time); 
    }


    sprintf(buf,"stop\n");
    sendto(socket_descriptor,buf,sizeof(buf),0,(struct sockaddr *)&address,sizeof(address));//发送stop 命令
    //关闭socket
	close(socket_descriptor);
    printf("Messages Sent,terminating\n");

	//关闭文件
#ifdef RECORD
	fclose(log_file);
#endif

    exit(0);
	
    return (EXIT_SUCCESS);

}



void parse(char *str,double *lt1,double *rt1,double *rt2)
{
	int i = 0;

	char tmp[30];

	char sec[20];

	char usec[20];

	//lt1
	for(i=0;str[i]!=' ';i++)
	{
		tmp[i] = str[i];
	}
	tmp[i] = '\n';

	//转为double型
	*lt1 = atof(tmp);
	
	//rt1
	i++;
	int j;
	for(j=0;str[i]!=' ';)
	{
		tmp[j] = str[i];
		i++;
		j++;
	}

	tmp[j] = '\n';
	*rt1 = atof(tmp);

	//rt2
	i++;
	int k;
	for(k=0;str[i]!='\n';)
	{
		tmp[k] = str[i];
		i++;
		k++;
	}

	tmp[k] = '\n';
	*rt2 = atof(tmp);
}
