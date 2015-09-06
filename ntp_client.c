#include <stdio.h>
#include <stdlib.h>

#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/time.h>

int out_port=6789;
int in_port = 6788;

int my_difftime(struct timeval  *t1,struct timeval  *t2);
void parse(char *str,double *lt1,double *rt1,double *rt2);

int main(int argc, char** argv) {

	if(argc<=1)
	{
		printf("Please input server IP\n");
		return 0;
	}
	printf("Try to connect Server:%s\n",argv[1]);

    	int socket_descriptor; //套接口描述字
	int iter=0;
    	char buf[80];
    	struct sockaddr_in address;//处理网络通信的地址

    	bzero(&address,sizeof(address));
    	address.sin_family=AF_INET;
    	address.sin_addr.s_addr=inet_addr(argv[1]);//这里不一样
    	address.sin_port=htons(out_port);


    //创建一个 UDP socket

    	socket_descriptor=socket(AF_INET,SOCK_DGRAM,0);//IPV4  SOCK_DGRAM 数据报套接字（UDP协议）

//get
	char message[256];
	int sin_len;
    struct sockaddr_in sin;
    bzero(&sin,sizeof(sin));
    sin.sin_family=AF_INET;
    sin.sin_addr.s_addr=htonl(INADDR_ANY);
    sin.sin_port=htons(in_port);
    sin_len=sizeof(sin);
    bind(socket_descriptor,(struct sockaddr *)&sin,sizeof(sin));


    int send_max = 512;

    for(iter=0;iter<=send_max;iter++)
    {
#ifdef DEBUG
	printf("Send Query %d\n",iter);
#endif
	struct timeval  *send_time = (struct timeval  *)malloc(sizeof(struct timeval));
	
         /*
         * sprintf(s, "%8d%8d", 123, 4567); //产生：" 123 4567" 
         * 将格式化后到 字符串存放到s当中
         */

	if(gettimeofday(send_time,NULL)==-1)//error
	{
		free(send_time);
		continue;
		
	}
	else{
		unsigned int second = send_time->tv_sec;
		unsigned int usecond = send_time->tv_usec;
		double utime = (double)second+ usecond*1.0/100000;
		sprintf(buf,"%f",utime);
		//sprintf(buf,"%s ",send_time);
	}
        //sprintf(buf,"data packet with ID %d\n",iter);
       
        /*int PASCAL FAR sendto( SOCKET s, const char FAR* buf, int len, int flags,const struct sockaddr FAR* to, int tolen);　　
         * s：一个标识套接口的描述字。　
         * buf：包含待发送数据的缓冲区。　　
         * len：buf缓冲区中数据的长度。　
         * flags：调用方式标志位。　　
         * to：（可选）指针，指向目的套接口的地址。　
         * tolen：to所指地址的长度。  
　　      */
        sendto(socket_descriptor,buf,sizeof(buf),0,(struct sockaddr *)&address,sizeof(address));
	
	free(send_time);

	recvfrom(socket_descriptor,message,sizeof(message),0,(struct sockaddr *)&sin,&sin_len);

	struct timeval  *recv_time = (struct timeval  *)malloc(sizeof(struct timeval));
	gettimeofday(recv_time,NULL);

	double lt2 = (double)recv_time->tv_sec + recv_time->tv_usec *1.0/1000000;
	double rt1,rt2,lt1;
	parse(message,&lt1,&rt1,&rt2);

#ifdef DEBUG
	printf("%s\n",message);
	printf("%f %f %f %f\n",lt1,lt2,rt1,rt2);
#endif

	
	
	double offset = ((rt1-lt1)+(rt2-lt2))/2;
	struct timeval  *new_time = (struct timeval  *)malloc(sizeof(struct timeval));
	gettimeofday(new_time,NULL);
	double nt = (double)new_time->tv_sec + new_time->tv_usec *1.0/1000000;
	
	//set new time
	
	nt = nt + offset;
	long i_nt = (unsigned int)nt;
	
	new_time->tv_sec = i_nt;
	
	new_time->tv_usec = (nt-i_nt) * 1000000;

	settimeofday(new_time, NULL);

	gettimeofday(new_time,NULL);

#ifdef DEBUG
	printf("%f %f %d %d\n",offset,nt,(long)new_time->tv_sec,(long)new_time->tv_usec);
#endif
	
	free(new_time);

	free(recv_time);
	
	sleep(8); 
    }

    sprintf(buf,"stop\n");
    sendto(socket_descriptor,buf,sizeof(buf),0,(struct sockaddr *)&address,sizeof(address));//发送stop 命令
    close(socket_descriptor);
    printf("Messages Sent,terminating\n");

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
/*
void toTimeval(char* time,struct timeval* tv)
{
	int i = 0;
	char sec[20];
	char usec[20];
	for(i=0;time[i]!='.';i++)
	{
		sec[i] = time[i];
	}
	sec[i] = '\n';
	i++;
	int j;
	for(j=i;time[j]!='\n';j++)
	{
		usec[j-i] = time[j];
	}

	usec[j-i] = '\n';

	struct timeval tmp;
	tmp.tv_sec = atoi(sec);
	tmp.tv_usec = atoi(usec);
}

void newTime(struct timeval  *lt1 , struct timeval  *lt2 , struct timeval  *rt1 , struct timeval  *rt2, struct timeval  *newtime)
{
	

//Delay=(lt2-lt1)-(rt2-rt1)
//offset=((rt1-lt1)+(rt2-lt2))/2

	long sec_offset =( (rt1->tv_sec - lt1->tv_sec) + (rt2->tv_sec - lt2->tv_sec) )/2;
	long sec_offset =( (rt1->tv_usec - lt1->tv_usec) + (rt2->tv_usec - lt2->tv_usec) )/2;
	
	if(sec_offset < 0)
	{
		
	}
	
	newtime->sec = lt2->sec;
	newtime->usec = lt2->usec
	
	
	
	
	
}
*/
/*
struct timeval* my_difftime(struct timeval  *t1 , struct timeval  *t2)
{
	unsigned int usec = 
	return;
}
*/

