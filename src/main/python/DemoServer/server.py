import datetime
import socket
import sys

import select
import threading
import os
import time

from concurrent.futures import ThreadPoolExecutor

from log import run_log as logger

from BackEndTools.demo import ImageDemoSample

UploadImageStoreRootPath = "sources/username/uploadImage"
ResultImageStoreRootPath = "sources/username/resImage"

ImageProcess = ImageDemoSample("BackEndTools\\configs\\demoConfigs.yaml".replace("\\", "/"))
host = "127.0.0.1"
port = 8701


def server():
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serversocket.bind((host, port))
    serversocket.listen()
    # serversocket.settimeout(-1)
    logger.info(f"server started, listen on {host}:{port}")
    while True:
        clientsocket, addr = serversocket.accept()
        logger.info(f"tcp established from {addr}")
        try:
            thread = ServerThread(clientsocket)
            thread.start()
        except Exception as identifier:
            logger.error(identifier)
    # serversocket.close()


class ServerThread(threading.Thread):
    def __init__(self, clientsocket: socket.socket, encoding='utf-8'):
        threading.Thread.__init__(self)
        self._socket = clientsocket
        self._encoding = encoding

    def run(self):
        textio = self._socket.makefile(encoding=self._encoding)
        msg = textio.readline()
        while msg != '':
            res = handleMsg(msg.rstrip())
            if res != '':
                self._socket.send(res.encode())
            msg = textio.readline()
        textio.close()


def handleMsg(msg):
    try:
        if "ping" == msg or '' == msg:
            return ''
        upload_image_path = os.path.join(UploadImageStoreRootPath, msg)
        logger.info(f"原始图片路径：{upload_image_path}")
        td = time.strftime("%Y%m%d", time.localtime())
        ResultImageStorePath = os.path.join(ResultImageStoreRootPath, td)
        if not os.path.exists(ResultImageStorePath):
            os.makedirs(ResultImageStorePath)

        ResultImageStorePath = os.path.join(ResultImageStoreRootPath, msg)
        # 开始处理
        ImageProcess(upload_image_path, ResultImageStorePath)
        logger.info(f"结果图片路径：{msg}")
        return msg + "\n"
    except Exception as e:
        logger.error("something wrong when socket io")
        print(e)


def handlePackage(target_socket, msg, epoll, textio, buffers):
    res = handleMsg(target_socket, msg)
    if res != '':
        buffers[target_socket] = res
        epoll.modify(target_socket, select.EPOLLOUT)
    textio.close()


def epoll_server():
    procPool = ThreadPoolExecutor(15)
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((host, port))
    s.listen()
    s.setblocking(False)
    epoll = select.epoll()
    epoll.register(s.fileno(), select.EPOLLIN | select.EPOLLOUT | select.EPOLLHUP)
    connections = {}
    addresses = {}
    buffers = {}
    logger.info(f"epoll_server started, listen on {host}:{port}")
    while True:
        epoll_list = epoll.poll()
        for fd, events in epoll_list:
            if fd == s.fileno():
                new_socket, new_addr = s.accept()
                connections[new_socket.fileno()] = new_socket
                addresses[new_socket.fileno()] = new_addr
                epoll.register(new_socket.fileno(), select.EPOLLIN | select.EPOLLET)
                logger.info(f"tcp established from {new_addr}, there are {len(connections)} connections now")
            elif events & select.EPOLLIN:
                textio = connections[fd].makefile(encoding='utf-8')
                msg = textio.readline().rstrip()
                if msg != '':
                    procPool.submit(handlePackage, connections[fd], msg, epoll, textio, buffers)
            elif events & select.EPOLLOUT:
                if buffers[fd] != '':
                    connections[fd].send(buffers[fd].encode())
                    buffers[fd] = ''
                epoll.modify(fd, select.EPOLLIN)
            elif events & select.EPOLLHUP:
                epoll.unregister(fd)
                connections[fd].close()
                logger.info(f"{addresses[fd]}---offline---")
                del connections[fd]
                del addresses[fd]


if __name__ == '__main__':
    if 'epoll' == sys.argv[1]:
        epoll_server()
    else:
        server()
