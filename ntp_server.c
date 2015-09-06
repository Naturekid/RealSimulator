#include <stdio.h>
#include <stdlib.h>

#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/time.h>

int in_port=6789;
int out_port= 6788;

int main(int argc, char** argv) {

    int sin_len;
    char message[256];
    char buf[256];

    int socket_descriptor;
    struct sockaddr_in sin;
    printf("Waiting for data form sender \n");

    bzero(&sin,sizeof(sin));
    sin.sin_family=AF_INET;
    sin.sin_addr.s_addr=htonl(INADDR_ANY);
    sin.sin_port=htons(in_port);
    sin_len=sizeof(sin);

    socket_descriptor=socket(AF_INET,SOCK_DGRAM,0);
    bind(socket_descriptor,(struct sockaddr *)&sin,sizeof(sin));

    while(1)
    {
	struct timeval  *recv_time = (struct timeval  *)malloc(sizeof(struct timeval));
	struct timeval  *send_time = (struct timeval  *)malloc(sizeof(struct timeval));

        recvfrom(socket_descriptor,message,sizeof(message),0,(struct sockaddr *)&sin,&sin_len);
	
	if(gettimeofday(recv_time,NULL)==-1)//error
	{
		free(send_time);
		free(recv_time);
		continue;
		
	}
	
        printf("Response from server:%s\n",message);
        if(strncmp(message,"stop",4) == 0)//接受到的消息为 “stop”
        {

            printf("Sender has told me to end the connection\n");
            break;
        }

	struct sockaddr_in address;//处理网络通信的地址

   	bzero(&address,sizeof(address));
    	address.sin_family=AF_INET;
   	address.sin_addr.s_addr=sin.sin_addr.s_addr;//这里不一样
   	address.sin_port=htons(out_port);

	if(gettimeofday(send_time,NULL)==-1)//error
	{
		free(send_time);
		free(recv_time);
		continue;
	}

	unsigned int r_sec = recv_time->tv_sec;
	unsigned int r_usec = recv_time->tv_usec;
	unsigned int s_sec = send_time->tv_sec;
	unsigned int s_usec = send_time->tv_usec;
	double r_utime = (double)r_sec + r_usec*1.0/1000000;
	double s_utime = (double)s_sec + s_usec*1.0/1000000;

	sprintf(buf,"%s %f %f\n",message,r_utime,s_utime);

	printf("%s",buf);

sleep(1);

	sendto(socket_descriptor,buf,sizeof(buf),0,(struct sockaddr *)&address,sizeof(address));


	printf("*********%d**********\n",address.sin_addr.s_addr);
	
	
	
    }

    close(socket_descriptor);
    exit(0);

    return (EXIT_SUCCESS);
}
